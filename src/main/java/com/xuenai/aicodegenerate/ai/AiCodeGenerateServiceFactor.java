package com.xuenai.aicodegenerate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xuenai.aicodegenerate.ai.tools.FileWriteTool;
import com.xuenai.aicodegenerate.custom.CustomRedisChatMemoryStore;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 代码生成服务工厂
 */
@Slf4j
@Configuration
public class AiCodeGenerateServiceFactor {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private CustomRedisChatMemoryStore customRedisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存策略
     * -    最大缓存 1000 个实例
     * -    写入后 30 分钟过期
     * -    访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGenerateService> serviceCache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(30)).expireAfterAccess(Duration.ofMinutes(10)).removalListener((key, value, cause) -> {
        log.debug("AI 服务实例被移除,应用ID为: {},原因: {}", key, cause);
    }).build();

    /**
     * 为每一个应用做单独会话隔离（保留仅仅应用兼容历史编辑）
     *
     * @param appId 应用 ID
     * @return ai生成服务类
     */
    public AiCodeGenerateService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenerateTypeEnum.HTML);
    }

    /**
     * 为每一个应用做单独会话隔离
     *
     * @param appId 应用 ID
     * @return ai生成服务类
     */
    public AiCodeGenerateService getAiCodeGeneratorService(long appId, CodeGenerateTypeEnum generateType) {
        String cacheKey = buildCacheKey(appId, generateType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, generateType));
    }

    /**
     * 构建缓存建
     *
     * @param appId        应用 ID
     * @param generateType 生成代码类型
     * @return key
     */
    private String buildCacheKey(long appId, CodeGenerateTypeEnum generateType) {
        return appId + "_" + generateType.getValue();
    }

    /**
     * 根据应用 ID 创建单独创建 AI 服务实例
     *
     * @param appId 应用 ID
     * @return ai 生成服务类
     */
    private AiCodeGenerateService createAiCodeGeneratorService(long appId, CodeGenerateTypeEnum generateType) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().id(appId).chatMemoryStore(customRedisChatMemoryStore).maxMessages(30).build();
        // 加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 30);
        return switch (generateType) {
            case VUE_PROJECT ->
                    AiServices.builder(AiCodeGenerateService.class).streamingChatModel(reasoningStreamingChatModel).chatMemoryProvider(memoryId -> chatMemory).tools(new FileWriteTool()).hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(toolExecutionRequest, "Error: there is no tol called " + toolExecutionRequest.name())).build();
            case HTML, MULTI_FILE ->
                    AiServices.builder(AiCodeGenerateService.class).chatModel(chatModel).streamingChatModel(openAiStreamingChatModel).chatMemory(chatMemory).build();
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + generateType.getValue());
        };
    }

}
