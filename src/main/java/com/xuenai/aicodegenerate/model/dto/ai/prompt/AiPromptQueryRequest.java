package com.xuenai.aicodegenerate.model.dto.ai.prompt;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class AiPromptQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long modelId;      
    
    private String systemMessage; 
    
    private String userMessage; 
    
    private Map<String, Object> parameters; 
}
