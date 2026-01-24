package com.xuenai.aicodegenerate.ai.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工作流专用流处理器
 * 实时透传所有数据给前端
 * 识别并提取工作流进度 JSON -> 存入 thinking_content
 * 识别并提取 DeepSeek 思考标签 -> 存入 thinking_content
 * 提取纯净代码 -> 存入 message
 */
@Slf4j
@Component
public class WorkflowStreamHandler {

    // 正则匹配 DeepSeek 的 <think> 标签内容
    private static final Pattern THINK_PATTERN = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL);

    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        StringBuilder finalContentBuilder = new StringBuilder();
        List<Map<String, Object>> thinkingSteps = new CopyOnWriteArrayList<>();
        StringBuilder tempBuffer = new StringBuilder();
        // 记录已处理过的工具调用 ID
        Set<String> seenToolIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        //  缓存 tool_executed 的 ID，防止结果也重复刷
        Set<String> seenExecutedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        return originFlux.flatMap(chunk -> {
            if (isToolJson(chunk)) {
                try {
                    JSONObject toolJson = JSONUtil.parseObj(chunk);
                    String type = toolJson.getStr("type");
                    String id = toolJson.getStr("id");
                    String displayContent = null;

                    if ("tool_request".equals(type)) {
                        // 去重逻辑
                        if (StrUtil.isNotBlank(id) && !seenToolIds.contains(id)) {
                            seenToolIds.add(id);
                            String name = toolJson.getStr("name");
                            // 解析参数，提取文件名
                            String fileInfo = extractFileInfo(toolJson.getStr("arguments"));
                            displayContent = "调用工具: " + name + (StrUtil.isNotBlank(fileInfo) ? " (" + fileInfo + ")" : "");
                        }
                    } else if ("tool_executed".equals(type)) {
                        // 结果去重
                        String callId = toolJson.getStr("tool_call_id");
                        String uniqueKey = StrUtil.isNotBlank(callId) ? callId : String.valueOf(chunk.hashCode());
                        if (!seenExecutedIds.contains(uniqueKey)) {
                            seenExecutedIds.add(uniqueKey);
                            String result = toolJson.getStr("result");
                            // 截断过长的结果
                            if (result != null && result.length() > 50) result = result.substring(0, 50) + "...";
                            displayContent = "工具执行完成: " + result;
                        }
                    }

                    // 如果生成了显示内容，则记录步骤并发送前端
                    if (displayContent != null) {
                        Map<String, Object> step = Map.of(
                                "type", "processing",
                                "content", displayContent,
                                "step", 999
                        );
                        thinkingSteps.add(step);

                        Map<String, Object> frontendMessage = new HashMap<>();
                        frontendMessage.put("type", type);
                        frontendMessage.put("tool_name", toolJson.getStr("name"));

                        if ("tool_request".equals(type)) {
                            frontendMessage.put("id", id);
                            // 解析 parameters
                            try {
                                String argsStr = toolJson.getStr("arguments");
                                if (StrUtil.isNotBlank(argsStr)) {
                                    JSONObject params = JSONUtil.parseObj(argsStr);
                                    frontendMessage.put("parameters", params);
                                } else {
                                    frontendMessage.put("parameters", new HashMap<>());
                                }
                            } catch (Exception e) {
                                log.error("解析 tool_request 参数失败", e);
                                frontendMessage.put("parameters", new HashMap<>());
                            }
                        } else if ("tool_executed".equals(type)) {
                            frontendMessage.put("tool_call_id", toolJson.getStr("tool_call_id"));
                            frontendMessage.put("result", toolJson.getStr("result"));
                            // 解析 parameters
                            try {
                                String argsStr = toolJson.getStr("arguments");
                                if (StrUtil.isNotBlank(argsStr)) {
                                    JSONObject params = JSONUtil.parseObj(argsStr);
                                    frontendMessage.put("parameters", params);
                                } else {
                                    frontendMessage.put("parameters", new HashMap<>());
                                }
                            } catch (Exception e) {
                                log.error("解析 tool_executed 参数失败", e);
                                frontendMessage.put("parameters", new HashMap<>());
                            }
                        }
                        String frontendJson = JSONUtil.toJsonStr(frontendMessage);
                        // 发送 JSON 给前端更新状态
                        return Flux.just(frontendJson, JSONUtil.toJsonStr(step));
                    } else {
                        return Flux.empty();
                    }

                } catch (Exception e) {
                    log.error("解析工具JSON失败: {}", chunk, e);
                    // 解析失败也不要抛出异常中断流，当作普通文本处理或者忽略
                    return Flux.empty();
                }
            }

            // 拦截 AI 响应 JSON (ai_response)
            if (isAiResponseJson(chunk)) {
                try {
                    JSONObject aiJson = JSONUtil.parseObj(chunk);
                    String data = aiJson.getStr("data");
                    if (data != null) {
                        tempBuffer.append(data);
                        return Flux.just(data);
                    }
                } catch (Exception e) {
                    // 解析失败按普通文本处理
                }
            }

            // 工作流进度 JSON 
            if (isWorkflowProgressJson(chunk)) {
                try {
                    Map<String, Object> step = JSONUtil.toBean(chunk, Map.class);
                    thinkingSteps.add(step);
                } catch (Exception e) {}
                return Flux.just(chunk);
            }
            
            tempBuffer.append(chunk);
            return Flux.just(chunk);
        }).doOnComplete(() -> {
            String fullText = tempBuffer.toString();
            String finalMessage = fullText;

            // 提取 DeepSeek 思考
            Matcher matcher = THINK_PATTERN.matcher(fullText);
            if (matcher.find()) {
                String thinkContent = matcher.group(1);
                thinkingSteps.add(Map.of(
                        "type", "reasoning",
                        "content", "深度思考中...",
                        "step", 999,
                        "extendedContent", thinkContent
                ));
                finalMessage = matcher.replaceAll("").trim();
            }
            finalContentBuilder.append(finalMessage);

            String finalContent = finalContentBuilder.toString();
            if (StrUtil.isBlank(finalContent)) {
                if (!thinkingSteps.isEmpty()) {
                    finalContent = "（任务已执行，详细过程请查看上方思考步骤）";
                } else {
                    finalContent = "Generated Code"; // 最后的保底
                }
            }
            chatHistoryService.createChatHistoryWithThinking(
                    appId,
                    loginUser.getId(),
                    finalContent,
                    JSONUtil.toJsonStr(thinkingSteps),
                    ChatHistoryMessageTypeEnum.AI.getValue()
            );
        }).doOnError(error -> {
            String msg = "生成异常: " + error.getMessage();
            chatHistoryService.createChatHistory(appId, loginUser.getId(), msg, ChatHistoryMessageTypeEnum.AI.getValue());
        });
    }

    private String extractFileInfo(String argsStr) {
        try {
            if (StrUtil.isNotBlank(argsStr) && argsStr.trim().startsWith("{")) {
                JSONObject argsObj = JSONUtil.parseObj(argsStr);
                if (argsObj.containsKey("relativePath")) return argsObj.getStr("relativePath");
                if (argsObj.containsKey("relativeFilePath")) return argsObj.getStr("relativeFilePath");
            }
        } catch (Exception e) {}
        return null;
    }
    
    private boolean isAiResponseJson(String chunk) {
        if (StrUtil.isBlank(chunk)) return false;
        String trimmed = chunk.trim();
        return trimmed.startsWith("{") && trimmed.contains("\"type\"") && trimmed.contains("\"ai_response\"");
    }
    
    private boolean isToolJson(String chunk) {
        if (StrUtil.isBlank(chunk)) return false;
        String trimmed = chunk.trim();
        return trimmed.startsWith("{") && trimmed.contains("\"type\"")
                && (trimmed.contains("\"tool_request\"") || trimmed.contains("\"tool_executed\""));
    }

    private boolean isWorkflowProgressJson(String chunk) {
        if (StrUtil.isBlank(chunk)) return false;
        String trimmed = chunk.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}")
                && trimmed.contains("\"type\"")
                && !trimmed.contains("\"tool_request\"")
                && !trimmed.contains("\"tool_executed\"")
                && !trimmed.contains("\"ai_response\"");
    }
}
