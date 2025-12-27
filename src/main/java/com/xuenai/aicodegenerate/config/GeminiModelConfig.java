package com.xuenai.aicodegenerate.config;

import com.xuenai.aicodegenerate.config.properties.GeminiModelProperties;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiModelConfig {

    @Resource
    private GeminiModelProperties properties;

    @Bean("geminiReasoningStreamingChatModel")
    public StreamingChatModel geminiReasoningStreamingChatModel() {
        return buildStreamingChatModel(properties.getReasoning());
    }

    private OpenAiStreamingChatModel buildStreamingChatModel(GeminiModelProperties.ModelConfig config) {
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
