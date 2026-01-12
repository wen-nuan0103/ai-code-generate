package com.xuenai.aicodegenerate.model.dto.ai.key;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiKeyAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private String provider;
    
    private String keys;
}
