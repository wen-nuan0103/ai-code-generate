package com.xuenai.aicodegenerate.ai;

import com.xuenai.aicodegenerate.ai.core.DynamicChatModelDelegate;
import com.xuenai.aicodegenerate.ai.service.AiCodeGenerateTypeRoutingService;
import com.xuenai.aicodegenerate.ai.service.AiProjectInfoService;
import com.xuenai.aicodegenerate.ai.service.CodeQualityCheckService;
import com.xuenai.aicodegenerate.ai.service.ImageCollectionPlanService;
import com.xuenai.aicodegenerate.model.enums.AiModelTypeEnum;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServicesFactory {
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    
    @Resource
    private DynamicAiModelFactory dynamicAiModelFactory;
    
    @Bean
    public AiCodeGenerateTypeRoutingService aiCodeGenerateTypeRoutingService() {
        DynamicChatModelDelegate routingDelegate = 
                new DynamicChatModelDelegate(AiModelTypeEnum.CHAT.getValue(), dynamicAiModelFactory);
        return AiServices.builder(AiCodeGenerateTypeRoutingService.class)
                .chatModel(routingDelegate)
                .build();
    }
    
    @Bean
    public AiProjectInfoService aiProjectInfoService() {
        DynamicChatModelDelegate chatDelegate =
                new DynamicChatModelDelegate(AiModelTypeEnum.CHAT.getValue(), dynamicAiModelFactory);
        
        return AiServices.builder(AiProjectInfoService.class)
                .chatModel(chatDelegate)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .maxMessages(10)
                        .build())
                .build();
    }
    
    @Bean
    public CodeQualityCheckService codeQualityCheckService() {
        DynamicChatModelDelegate auditDelegate = 
                new DynamicChatModelDelegate(AiModelTypeEnum.CODE_AUDIT.getValue(), dynamicAiModelFactory);

        return AiServices.builder(CodeQualityCheckService.class)
                .chatModel(auditDelegate)
                .build();
    }
    
    @Bean
    public ImageCollectionPlanService imageCollectionPlanService() {
        DynamicChatModelDelegate planDelegate = 
                new DynamicChatModelDelegate(AiModelTypeEnum.IMAGE_COLLECT.getValue(), dynamicAiModelFactory);
        
        return AiServices.builder(ImageCollectionPlanService.class)
                .chatModel(planDelegate)
                .build();
    }

}
