package com.xuenai.aicodegenerate.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j.gemini")
public class GeminiModelProperties {
    
    private GeminiModelProperties.ModelConfig reasoning;  // 推理模型

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
    
}
