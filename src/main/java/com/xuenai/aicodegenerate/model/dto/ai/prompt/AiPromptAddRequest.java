package com.xuenai.aicodegenerate.model.dto.ai.prompt;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class AiPromptAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private String code;
    
    private String name;
    
    private String modelId;
    
    private String systemMessage;
    
    private String userMessage;
    
    private Map<String,Object> parameters;
}
