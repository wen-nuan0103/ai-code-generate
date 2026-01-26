package com.xuenai.aicodegenerate.model.dto.ai.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String taskType;
    private String modelName;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal totalCost;
    private Integer status;
    private String errorMsg;
    private Long duration;
    
    
}
