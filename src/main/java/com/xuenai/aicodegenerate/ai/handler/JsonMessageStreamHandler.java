package com.xuenai.aicodegenerate.ai.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xuenai.aicodegenerate.ai.builder.VueProjectBuilder;
import com.xuenai.aicodegenerate.ai.mode.message.AiResponseMessage;
import com.xuenai.aicodegenerate.ai.mode.message.StreamMessage;
import com.xuenai.aicodegenerate.ai.mode.message.ToolExecutedMessage;
import com.xuenai.aicodegenerate.ai.mode.message.ToolRequestMessage;
import com.xuenai.aicodegenerate.ai.tools.BaseTool;
import com.xuenai.aicodegenerate.ai.tools.ToolManage;
import com.xuenai.aicodegenerate.constant.AppConstant;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.xuenai.aicodegenerate.model.enums.StreamMessageTypeEnum;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应，包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {
    
    @Resource
    private ToolManage toolManage;
    
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪是否已发送元数据
        AtomicBoolean metadataSent = new AtomicBoolean(false);
        // 用于跟踪是否有工具调用
        AtomicBoolean hasToolCalls = new AtomicBoolean(false);
        Set<String> seenToolIds = new HashSet<>();
        return originFlux.flatMap(chunk -> {
                    if (isToolJson(chunk)){
                        hasToolCalls.set(true);
                        if (!metadataSent.getAndSet(true)) {
                            String metadata = createCodeGenerationMetadata();
                            log.debug("发生元数据: isCodeGeneration=true");
                            return Flux.just(metadata, chunk);
                        }
                    }
                    
                    if (isAiResponseJson(chunk) && !metadataSent.getAndSet(true)) {
                        String metadata = createChatMetadata();
                        log.debug("发生元数据: isCodeGeneration=false");
                        return Flux.just(metadata, chunk);
                    }
                    return Flux.just(chunk);
                })
                .flatMap(chunk -> {
                    try {
                        JSONObject testObj = JSONUtil.parseObj(chunk);
                        if ("metadata".equals(testObj.getStr("type"))) {
                            log.info("🔄 元数据消息直接透传: {}", chunk);
                            return Flux.just(chunk); 
                        }
                    } catch (Exception e) {
                        // 不是 JSON，继续正常处理
                    }
    
                    // 其他消息正常处理
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .doOnComplete(() -> {
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.createChatHistory(appId, loginUser.getId(), aiResponse, ChatHistoryMessageTypeEnum.AI.getValue());
                    
                    // 只有 Vue 项目才需要构建
                    String vueProjectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    if (new java.io.File(vueProjectPath).exists()) {
                        vueProjectBuilder.buildProjectAsync(vueProjectPath);
                    }
                }).doOnError(error -> {
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.createChatHistory(appId, loginUser.getId(), errorMessage, ChatHistoryMessageTypeEnum.AI.getValue());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private Flux<String> handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        if (typeEnum == null) {
            log.warn("未知的消息类型: {}", streamMessage.getType());
            return Flux.empty();
        }
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                chatHistoryStringBuilder.append(data);
                
                return Flux.just(data);
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    seenToolIds.add(toolId);
                    BaseTool tool = toolManage.getTool(toolName);
                    String textDescription = tool.generateToolRequestResponse();
                    Map<String, Object> frontendMessage = new HashMap<>(Map.of(
                            "type", "tool_request",
                            "tool_name", toolRequestMessage.getName(),
                            "id", toolId
                    ));
                    try {
                        JSONObject params = JSONUtil.parseObj(toolRequestMessage.getArguments());
                        frontendMessage.put("parameters", params);
                    } catch (Exception e) {
                        log.error("解析工具参数失败: {}", toolRequestMessage.getArguments(), e);
                        frontendMessage.put("parameters", new HashMap<>());
                    }
                    String frontendJson = JSONUtil.toJsonStr(frontendMessage);
                    return Flux.just(frontendJson, textDescription);
                } else {
                    return Flux.empty();
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String toolName = toolExecutedMessage.getName();
                BaseTool tool = toolManage.getTool(toolName);
                String result = tool.generateToolExecutedResult(jsonObject);
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                Map<String, Object> frontendMessage = new HashMap<>(Map.of(
                    "type","tool_executed",
                    "tool_name", toolName,
                    "id", toolExecutedMessage.getId(),
                    "result", toolExecutedMessage.getResult()
                ));
                try {
                    JSONObject params = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                    frontendMessage.put("parameters", params);
                } catch (Exception e) {
                    log.error("解析工具参数失败: {}", toolExecutedMessage.getArguments(), e);
                    frontendMessage.put("parameters", new HashMap<>());
                }
                String frontendJson = JSONUtil.toJsonStr(frontendMessage);
                return Flux.just(frontendJson, output);
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return Flux.empty();
            }
        }
    }

    /**
     * 判断是否是 AI 响应 JSON
     */
    private boolean isAiResponseJson(String chunk) {
        if (StrUtil.isBlank(chunk)) return false;
        String trimmed = chunk.trim();
        return trimmed.startsWith("{") &&
                trimmed.contains("\"type\"") &&
                trimmed.contains("\"ai_response\"");
    }

    /**
     * 判断是否是工具调用 JSON
     */
    private boolean isToolJson(String chunk) {
        if (StrUtil.isBlank(chunk)) return false;
        String trimmed = chunk.trim();
        return trimmed.startsWith("{") &&
                trimmed.contains("\"type\"") &&
                (trimmed.contains("\"tool_request\"") ||
                        trimmed.contains("\"tool_executed\""));
    }

    /**
     * 元数据消息构建器
     * 用于构建不同类型的元数据消息
     */
    private static class MetadataBuilder {
        private final Map<String, Object> metadata = new HashMap<>();

        private MetadataBuilder() {
            metadata.put("type", "metadata");
        }

        public static MetadataBuilder create() {
            return new MetadataBuilder();
        }

        /**
         * 设置是否是代码生成
         */
        public MetadataBuilder isCodeGeneration(boolean isCodeGeneration) {
            metadata.put("isCodeGeneration", isCodeGeneration);
            return this;
        }

        /**
         * 设置预估时间（秒）
         */
        public MetadataBuilder estimatedTime(int seconds) {
            metadata.put("estimatedTime", seconds);
            return this;
        }

        /**
         * 设置复杂度
         */
        public MetadataBuilder complexity(String complexity) {
            metadata.put("complexity", complexity);
            return this;
        }

        /**
         * 设置进度百分比
         */
        public MetadataBuilder progress(int percentage) {
            metadata.put("progress", percentage);
            return this;
        }

        /**
         * 添加自定义字段
         */
        public MetadataBuilder addField(String key, Object value) {
            metadata.put(key, value);
            return this;
        }

        /**
         * 构建 JSON 字符串
         */
        public String build() {
            return JSONUtil.toJsonStr(metadata);
        }
    }

    /**
     * 创建代码生成元数据
     */
    private String createCodeGenerationMetadata() {
        return MetadataBuilder.create()
                .isCodeGeneration(true)
                .build();
    }

    /**
     * 创建普通聊天元数据
     */
    private String createChatMetadata() {
        return MetadataBuilder.create()
                .isCodeGeneration(false)
                .build();
    }
}

