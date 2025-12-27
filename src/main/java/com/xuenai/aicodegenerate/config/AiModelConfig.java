package com.xuenai.aicodegenerate.config;

import com.xuenai.aicodegenerate.config.properties.AiModelProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模型 Bean 注册中心
 */
@Configuration
public class AiModelConfig {

    @Resource
    private AiModelProperties properties;
    
    @Bean("routingChatModel")
    public ChatModel routingChatModel() {
        return buildChatModel(properties.getRouting());
    }

    @Bean("reasoningChatModel")
    public ChatModel reasoningChatModel() {
        return buildChatModel(properties.getReasoning());
    }
    
    @Bean("streamingChatModel")
    public StreamingChatModel streamingChatModel() {
        return buildStreamingChatModel(properties.getStreaming());
    }
    

    @Bean("reasoningStreamingChatModel")
    public StreamingChatModel reasoningStreamingChatModel() {
    // 使用配置属性中的推理参数构建并返回流式聊天模型
        return buildStreamingChatModel(properties.getReasoning());
    }

    /**
     * 构建OpenAI聊天模型的方法
     * 根据提供的模型配置信息创建并返回一个OpenAiChatModel实例
     * 
     * @param config AI模型的配置信息，包含baseUrl、apiKey等参数
     * @return 配置好的OpenAiChatModel实例
     */
    private OpenAiChatModel buildChatModel(AiModelProperties.ModelConfig config) {
        return OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .logRequests(config.getLogRequests())
                .logResponses(config.getLogResponses())
                .build();
    }

    /**
     * 构建OpenAI流式聊天模型的方法
     * 根据提供的模型配置信息创建并返回一个OpenAiChatModel实例
     *
     * @param config AI模型的配置信息，包含baseUrl、apiKey等参数
     * @return 配置好的OpenAiChatModel实例
     */
    private OpenAiStreamingChatModel buildStreamingChatModel(AiModelProperties.ModelConfig config) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .logRequests(config.getLogRequests())
                .logResponses(config.getLogResponses())
                .build();
    }
    
}
