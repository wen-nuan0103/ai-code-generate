package com.xuenai.aicodegenerate.config.properties;

import com.xuenai.aicodegenerate.config.AiModelConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 统一 AI 模型配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai")
public class AiModelProperties {

    private AiModelConfig.ModelConfig routing;    // 路由/简单任务模型
    private AiModelConfig.ModelConfig streaming;  // 普通流式生成模型
    private AiModelConfig.ModelConfig reasoning;  // 推理模型


}