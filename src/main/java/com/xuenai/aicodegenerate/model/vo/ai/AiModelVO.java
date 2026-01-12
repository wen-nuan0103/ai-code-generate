package com.xuenai.aicodegenerate.model.vo.ai;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiModelVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    
    private String modelName;
    
    private String modelCode;
    
    private String modelType;
    
    private String provider;
}
