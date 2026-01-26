package com.xuenai.aicodegenerate.config.properties;

import com.xuenai.aicodegenerate.config.AiModelConfig;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
//@ConditionalOnProperty(
//        prefix = "langchain4j.gemini"
//)
public class GeminiModelProperties {
    
    private AiModelConfig.ModelConfig reasoning;  // 推理模型


    
}
