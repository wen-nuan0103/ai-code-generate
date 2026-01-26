package com.xuenai.aicodegenerate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xuenai.aicodegenerate.ai.guardrail.PromptSafetyInputGuardrail;
import com.xuenai.aicodegenerate.ai.service.AiCodeGenerateService;
import com.xuenai.aicodegenerate.ai.tools.ToolFactory;
import com.xuenai.aicodegenerate.custom.CustomRedisChatMemoryStore;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.model.enums.AiModelTypeEnum;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
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
    private DynamicAiModelFactory dynamicAiModelFactory;

    @Resource
    private CustomRedisChatMemoryStore customRedisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolFactory toolFactory;



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
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(customRedisChatMemoryStore)
                .maxMessages(100).build();
        // 加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 100);
        StreamingChatModel streamingChatModel = dynamicAiModelFactory.getStreamingChatModel(AiModelTypeEnum.CODE_GEN.getValue());
        return switch (generateType) {
            case VUE_PROJECT, HTML, MULTI_FILE -> {
                // 使用工具工厂创建带有上下文的工具
                Object[] toolsWithContext = toolFactory.createToolsWithContext(appId, generateType);
                yield AiServices.builder(AiCodeGenerateService.class)
                        .streamingChatModel(streamingChatModel)
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .tools(toolsWithContext)
                        .maxSequentialToolsInvocations(30) //连续最多调用30次
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> 
                                ToolExecutionResultMessage.from(toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name())
                        )
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        // 使用输出护轨可能会导致流式输出响应不及时，等到 AI 输出结束一起放回
//                        .outputGuardrails(new RetryOutputGuardrail())
                        .build();
            }
            default ->
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + generateType.getValue());
        };
    }

}
