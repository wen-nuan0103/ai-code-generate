package com.xuenai.aicodegenerate.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xuenai.aicodegenerate.custom.CustomRedisChatMemoryStore;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.exception.ThrowUtils;
import com.xuenai.aicodegenerate.mapper.ChatHistoryMapper;
import com.xuenai.aicodegenerate.model.dto.chat.history.ChatHistoryQueryRequest;
import com.xuenai.aicodegenerate.model.entity.App;
import com.xuenai.aicodegenerate.model.entity.ChatHistory;
import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.AppChatScopeStatusEnum;
import com.xuenai.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.xuenai.aicodegenerate.model.enums.UserRoleEnum;
import com.xuenai.aicodegenerate.service.AppService;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author 小菜
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Lazy
    @Resource
    private AppService appService;

    @Lazy
    @Resource
    private CustomRedisChatMemoryStore chatMemoryStore;


    @Override
    public boolean createChatHistory(Long appId, Long userId, String message, String messageType) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");

        ChatHistoryMessageTypeEnum typeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);

        // TODO 关联条件 PARTNER_ID
        ChatHistory chatHistory = ChatHistory.builder().appId(appId).userId(userId).message(message).messageType(typeEnum.getValue()).build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create().eq("app_id", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest historyQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (historyQueryRequest == null) {
            return queryWrapper;
        }
        Long id = historyQueryRequest.getId();
        String message = historyQueryRequest.getMessage();
        String messageType = historyQueryRequest.getMessageType();
        Long appId = historyQueryRequest.getAppId();
        Long userId = historyQueryRequest.getUserId();
        LocalDateTime lastCreateTime = historyQueryRequest.getLastCreateTime();
        String sortField = historyQueryRequest.getSortField();
        String sortOrder = historyQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id).like("message", message).eq("message_type", messageType).eq("app_id", appId).eq("user_id", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            QueryWrapper queryWrapper = QueryWrapper.create().eq("app_id", appId).orderBy("create_time", false)
                    // 注意: 需要排除最新的一条用户信息
                    .limit(1, maxCount);
            List<ChatHistory> chatHistories = this.list(queryWrapper);
            if (CollectionUtils.isEmpty(chatHistories)) return 0;
            chatHistories = chatHistories.reversed();
            int loadedCount = 0;
            chatMemory.clear();
            for (ChatHistory chatHistory : chatHistories) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(UserMessage.from(chatHistory.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(chatHistory.getMessageType())) {
                    chatMemory.add(AiMessage.from(chatHistory.getMessage()));
                    loadedCount++;
                }
            }
            log.info("应用 {} 加载 {} 条历史记录到内存中", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载应用 {} 的历史记录到内存失败,原因: {}", appId, e.getMessage());
            return 0;
//            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<ChatHistory> listChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTine, User LoginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 20, ErrorCode.PARAMS_ERROR, "页面大小必须在1-20之间");
        ThrowUtils.throwIf(LoginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTine);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);

        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public String exportChatToMarkdown(Long appId, User loginUser) {

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (AppChatScopeStatusEnum.PRIVATE.getValue().equals(app.getChatScopeStatus())) {
            boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(loginUser.getRole());
            boolean isOwner = loginUser.getId().equals(app.getUserId());
            ThrowUtils.throwIf(!isAdmin && !isOwner, ErrorCode.NO_AUTH_ERROR, "没有权限导出该应用的聊天记录");
        }

        List<ChatMessage> history = chatMemoryStore.getMessages(appId);
        if (CollectionUtils.isEmpty(history)) {

            QueryWrapper queryWrapper = QueryWrapper.create().eq("app_id", appId).orderBy("create_time", false);

            List<ChatHistory> chatHistories = this.list(queryWrapper);
            ThrowUtils.throwIf(CollectionUtils.isEmpty(chatHistories), ErrorCode.OPERATION_ERROR, "暂无聊天记录可以导出");

            chatHistories = chatHistories.reversed();

            history = chatHistories.stream().map(it -> {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(it.getMessageType())) {
                    return UserMessage.from(it.getMessage());
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(it.getMessageType())) {
                    return AiMessage.from(it.getMessage());
                }
                return null;
            }).filter(Objects::nonNull).toList();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Chat History (App ID: ").append(appId).append(")\n\n");

        for (ChatMessage msg : history) {

            //  用户消息
            if (msg.type() == ChatMessageType.USER) {
                sb.append("---\n\n");
                sb.append("## 🗣️ User\n\n");
                sb.append(quote(((UserMessage) msg).singleText())).append("\n\n");
                continue;
            }

            //  AI 消息
            if (msg.type() == ChatMessageType.AI) {
                sb.append("---\n\n");
                sb.append("## 🤖 Assistant\n\n");

                AiMessage aiMsg = (AiMessage) msg;
                String content = aiMsg.text();

                if (StrUtil.isNotBlank(content)) {
                    appendAiContent(sb, content);
                    continue;
                }

                if (aiMsg.toolExecutionRequests() != null && !aiMsg.toolExecutionRequests().isEmpty()) {
                    for (var toolReq : aiMsg.toolExecutionRequests()) {
                        String args = toolReq.arguments();
                        appendToolResult(sb, args);
                    }
                    continue;
                }
            }
        }

        return sb.toString();
    }


    /**
     * 把 AI 的所有内容改成缩进格式
     *
     * @param text 内容
     * @return 缩进后的内容
     */
    private String quote(String text) {
        if (text == null) return "";
        return Arrays.stream(text.split("\n")).map(line -> "> " + line).collect(Collectors.joining("\n"));
    }


    /**
     * 处理 AI 普通自然语言或包含代码块的内容
     *
     * @param sb      字符串构建器
     * @param content 内容
     */
    private void appendAiContent(StringBuilder sb, String content) {
        if (content.contains("```")) {
            sb.append("### 📄 AI 输出内容\n");
            sb.append("<details>\n<summary>点击展开内容</summary>\n\n");

            sb.append("```markdown\n");
            sb.append(content.replace("```", "\\```"));
            sb.append("\n```\n");

            sb.append("</details>\n\n");
        } else {
            sb.append(quote(content)).append("\n\n");
        }
    }

    /**
     * 处理工具返回 JSON：{"relativePath": "...", "content": "..."}
     *
     * @param sb   字符串构建器
     * @param json JSON 字符串
     */
    private void appendToolResult(StringBuilder sb, String json) {
        if (StrUtil.isBlank(json)) {
            sb.append("> [工具调用结果为空]\n\n");
            return;
        }
        JSONObject obj = null;
        try {
            obj = JSONUtil.parseObj(json);
        } catch (Exception e) {
            sb.append("> 工具返回内容无法解析为 JSON：\n");
            sb.append(quote(json)).append("\n\n");
            return;
        }

        String path = obj.getStr("relativePath");
        String content = obj.getStr("content");

        sb.append("### 🛠 工具生成文件\n\n");

        if (StrUtil.isNotBlank(path)) {
            sb.append("**📁 文件：** ").append(path).append("\n\n");
        }

        String lang = detectLang(path);

        sb.append("```").append(lang).append("\n");
        sb.append(StrUtil.nullToEmpty(content));
        sb.append("\n```\n\n");
    }


    /**
     * 获取代码的语言
     *
     * @param path 文件路径
     * @return 语言
     */
    private String detectLang(String path) {
        if (StrUtil.isBlank(path)) return "";
        path = path.toLowerCase();
        if (path.endsWith(".js")) return "javascript";
        if (path.endsWith(".ts")) return "typescript";
        if (path.endsWith(".json")) return "json";
        if (path.endsWith(".html")) return "html";
        if (path.endsWith(".css")) return "css";
        if (path.endsWith(".vue")) return "vue";
        if (path.endsWith(".java")) return "java";
        if (path.endsWith(".py")) return "python";
        if (path.endsWith(".sql")) return "sql";
        if (path.endsWith(".md")) return "markdown";
        return "";
    }


}
