package com.xuenai.aicodegenerate.ai;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServicesConfiguration {


    @Resource
    private ChatModel chatModel;
    
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    

    @Bean
    public AiCodeGenerateTypeRoutingService aiCodeGenerateTypeRoutingService() {
        return AiServices.builder(AiCodeGenerateTypeRoutingService.class).chatModel(chatModel).build();
    }


    @Bean
    public AiProjectInfoService aiProjectInfoService() {
        return AiServices.builder(AiProjectInfoService.class).chatModel(chatModel).chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder().id(memoryId).chatMemoryStore(redisChatMemoryStore).maxMessages(10).build()).build();
    }


}
