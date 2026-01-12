package com.xuenai.aicodegenerate.model.dto.ai.key;

import com.xuenai.aicodegenerate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class AiKeyQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String provider;

    private String key;
}
