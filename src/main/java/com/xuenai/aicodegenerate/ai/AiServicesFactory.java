package com.xuenai.aicodegenerate.ai;

import com.xuenai.aicodegenerate.ai.service.AiCodeGenerateTypeRoutingService;
import com.xuenai.aicodegenerate.ai.service.AiProjectInfoService;
import com.xuenai.aicodegenerate.ai.service.CodeQualityCheckService;
import com.xuenai.aicodegenerate.ai.service.ImageCollectionPlanService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServicesFactory {
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    
    @Bean
    public AiCodeGenerateTypeRoutingService aiCodeGenerateTypeRoutingService(
            @Qualifier("routingChatModel") ChatModel chatModel) {
        return AiServices.builder(AiCodeGenerateTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
    
    @Bean
    public AiProjectInfoService aiProjectInfoService(
            @Qualifier("openAiChatModel") ChatModel chatModel) {
        return AiServices.builder(AiProjectInfoService.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .maxMessages(10)
                        .build())
                .build();
    }
    
    @Bean
    public CodeQualityCheckService codeQualityCheckService(
            @Qualifier("reasoningChatModel") ChatModel chatModel) {
        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(chatModel)
                .build();
    }
    
    @Bean
    public ImageCollectionPlanService imageCollectionPlanService(
            @Qualifier("openAiChatModel") ChatModel chatModel) {
        return AiServices.builder(ImageCollectionPlanService.class)
                .chatModel(chatModel)
                .build();
    }

}
