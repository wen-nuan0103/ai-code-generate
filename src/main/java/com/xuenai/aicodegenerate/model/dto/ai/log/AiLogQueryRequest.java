package com.xuenai.aicodegenerate.model.dto.ai.log;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AiLogQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private String traceId;
    
    private Integer status;
}
