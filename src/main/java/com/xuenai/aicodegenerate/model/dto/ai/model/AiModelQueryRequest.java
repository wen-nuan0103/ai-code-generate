package com.xuenai.aicodegenerate.model.dto.ai.model;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AiModelQueryRequest extends PageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型类型
     */
    private String modelType;
    
}
