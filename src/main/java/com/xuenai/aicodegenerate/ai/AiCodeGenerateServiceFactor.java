package com.xuenai.aicodegenerate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xuenai.aicodegenerate.custom.CustomRedisChatMemoryStore;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
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
    private StreamingChatModel streamingChatModel;

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
    private final Cache<Long, AiCodeGenerateService> serviceCache = Caffeine.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(30)).expireAfterAccess(Duration.ofMinutes(10)).removalListener((key, value, cause) -> {
        log.debug("AI 服务实例被移除,应用ID为: {},原因: {}", key, cause);
    }).build();

    /**
     * 为每一个应用做单独会话隔离
     *
     * @param appId 应用 ID
     * @return ai生成服务类
     */
    public AiCodeGenerateService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 根据应用 ID 创建单独创建 AI 服务实例
     * @param appId 应用 ID
     * @return ai 生成服务类
     */
    private AiCodeGenerateService createAiCodeGeneratorService(long appId) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().id(appId).chatMemoryStore(customRedisChatMemoryStore).maxMessages(20).build();
        // 加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory,20);
        
        return AiServices.builder(AiCodeGenerateService.class).chatModel(chatModel).streamingChatModel(streamingChatModel).chatMemory(chatMemory).build();
    }

}
