package com.xuenai.aicodegenerate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xuenai.aicodegenerate.ai.strategy.AiModelStrategyService;
import com.xuenai.aicodegenerate.model.entity.AiApiKey;
import com.xuenai.aicodegenerate.model.entity.AiModel;
import com.xuenai.aicodegenerate.monitor.listener.AiModelMonitorListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicAiModelFactory {

    private final AiModelStrategyService aiModelStrategyService;

    private final AiModelMonitorListener aiModelMonitorListener;

    /**
     * 构建流式模型缓存
     * key: provider_modelCode_accessKey
     */
    private final Cache<String, StreamingChatModel> modelCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofMinutes(30))
            .build();

    /**
     * 构建阻塞模型缓存
     * key: provider_modelCode_accessKey
     */
    private final Cache<String, ChatModel> blockingModelCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofMinutes(60))
            .build();

    /**
     * 对外构建流式模型
     *
     * @param type 模型信息
     * @return 流式模型
     */
    public StreamingChatModel getStreamingChatModel(String type) {
        AiModelStrategyService.AiModelContext aiModelContext = aiModelStrategyService.pickModelByType(type);
        String cacheKey = buildCacheKey(aiModelContext.model(), aiModelContext.apiKey());
        return modelCache.get(cacheKey, k -> buildModel(aiModelContext));
    }

    /**
     * 对外构建阻塞式模型
     *
     * @param type 模型信息
     * @return 阻塞式模型
     */
    public ChatModel getChatModel(String type) {
        AiModelStrategyService.AiModelContext context = aiModelStrategyService.pickModelByType(type);
        String cacheKey = "BLOCKING:" + buildCacheKey(context.model(), context.apiKey());
        return blockingModelCache.get(cacheKey, k -> createOpenAiChatModel(context));
    }

    /**
     * 构建缓存 key
     *
     * @param info   模型
     * @param apiKey key
     * @return 缓存key
     */
    private String buildCacheKey(AiModel info, AiApiKey apiKey) {
        return info.getProvider() + "_" + info.getModelCode() + "_" + apiKey.getAccessKey();
    }

    /**
     * 构建流式模型
     *
     * @param context 上下文
     * @return 流式模型
     */
    private StreamingChatModel buildModel(AiModelStrategyService.AiModelContext context) {
        AiModel info = context.model();
        AiApiKey key = context.apiKey();

        log.info("动态构建模型实例 -> Type: {}, Model: {}, Provider: {}",
                info.getModelType(), info.getModelName(), info.getProvider());

        return OpenAiStreamingChatModel.builder()
                .baseUrl(info.getBaseUrl())
                .apiKey(key.getAccessKey())
                .modelName(info.getModelCode())
                .maxTokens(info.getMaxTokens())
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }

    /**
     * 构建阻塞式模型
     *
     * @param context 上下文
     * @return 阻塞式模型
     */
    private ChatModel createOpenAiChatModel(AiModelStrategyService.AiModelContext context) {
        AiModel info = context.model();
        AiApiKey key = context.apiKey();

        return OpenAiChatModel.builder()
                .baseUrl(info.getBaseUrl())
                .apiKey(key.getAccessKey())
                .modelName(info.getModelCode())
                .maxTokens(info.getMaxTokens())
                .temperature(0.2)
                .logRequests(true)
                .logResponses(true)
                .listeners(List.of(aiModelMonitorListener))
                .build();
    }

}
