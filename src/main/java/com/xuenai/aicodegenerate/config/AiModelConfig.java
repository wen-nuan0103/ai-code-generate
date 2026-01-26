package com.xuenai.aicodegenerate.config;


import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模型 Bean 注册中心
 */
@Configuration
public class AiModelConfig {
    
    /**
     * 内部通用配置模型
     */
    @Data
    public static class ModelConfig {
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private Integer maxTokens = 4096;
        private Double temperature = 0.7;
        private Boolean logRequests = false;
        private Boolean logResponses = false;
    }

//    @Resource
//    private AiModelProperties aiModelProperties;
//    @Resource
//    private GeminiModelProperties geminiModelProperties;
//    @Resource
//    private AiModelMonitorListener aiModelMonitorListener;
//    
//    
//    @Bean("routingChatModel")
//    public ChatModel routingChatModel() {
//        return buildChatModel(aiModelProperties.getRouting());
//    }
//
//    @Bean("reasoningChatModel")
//    public ChatModel reasoningChatModel() {
//        return buildChatModel(aiModelProperties.getReasoning());
//    }
//    
//    @Bean("streamingChatModel")
//    public StreamingChatModel streamingChatModel() {
//        return buildStreamingChatModel(aiModelProperties.getStreaming());
//    }
//    
//
//    @Bean("reasoningStreamingChatModel")
//    public StreamingChatModel reasoningStreamingChatModel() {
//        return buildStreamingChatModel(aiModelProperties.getReasoning());
//    }
//
//    @Bean("geminiReasoningStreamingChatModel")
//    public StreamingChatModel geminiReasoningStreamingChatModel() {
//        return buildStreamingChatModel(geminiModelProperties.getReasoning());
//    }
//
//    /**
//     * 构建OpenAI聊天模型的方法
//     * 根据提供的模型配置信息创建并返回一个OpenAiChatModel实例
//     * 
//     * @param config AI模型的配置信息，包含baseUrl、apiKey等参数
//     * @return 配置好的OpenAiChatModel实例
//     */
//    private OpenAiChatModel buildChatModel(ModelConfig config) {
//        return OpenAiChatModel.builder()
//                .baseUrl(config.getBaseUrl())
//                .apiKey(config.getApiKey())
//                .modelName(config.getModelName())
//                .maxTokens(config.getMaxTokens())
//                .temperature(config.getTemperature())
//                .logRequests(config.getLogRequests())
//                .logResponses(config.getLogResponses())
//                .listeners(List.of(aiModelMonitorListener))
//                .build();
//    }
//
//    /**
//     * 构建OpenAI流式聊天模型的方法
//     * 根据提供的模型配置信息创建并返回一个OpenAiChatModel实例
//     *
//     * @param config AI模型的配置信息，包含baseUrl、apiKey等参数
//     * @return 配置好的OpenAiChatModel实例
//     */
//    private OpenAiStreamingChatModel buildStreamingChatModel(ModelConfig config) {
//        return OpenAiStreamingChatModel.builder()
//                .baseUrl(config.getBaseUrl())
//                .apiKey(config.getApiKey())
//                .modelName(config.getModelName())
//                .maxTokens(config.getMaxTokens())
//                .temperature(config.getTemperature())
//                .logRequests(config.getLogRequests())
//                .logResponses(config.getLogResponses())
//                .listeners(List.of(aiModelMonitorListener))
//                .build();
//    }

    
}
