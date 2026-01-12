package com.xuenai.aicodegenerate.model.dto.ai.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiModelAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private String modelName;
    
    private String modelCode;
    
    private String modelType;
    
    private String provider;
    
    private String baseUrl;
    
    private Integer maxTokens;
    
}
