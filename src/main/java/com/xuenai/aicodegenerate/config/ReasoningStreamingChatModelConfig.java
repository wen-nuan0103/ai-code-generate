package com.xuenai.aicodegenerate.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Configuration
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 推理流模型（ Vue 项目生成）
     *
     * @return 模型
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
//        final String modelName = "deepseek-chat";
//        final int maxTokens = 8129;
        final String modelName = "deepseek-reasoner";
        final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder().apiKey(apiKey).baseUrl(baseUrl).modelName(modelName).maxTokens(maxTokens)
//                .logRequests(true).logResponses(true)
                .build();
    }
}
