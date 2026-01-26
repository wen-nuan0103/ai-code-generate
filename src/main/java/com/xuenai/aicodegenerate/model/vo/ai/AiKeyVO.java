package com.xuenai.aicodegenerate.model.vo.ai;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiKeyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String provider;
    private String accessKey;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime lastUsedTime;
    private Integer errorCount;
}
