package com.xuenai.aicodegenerate.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String appId;

    private String taskType;

    private String requestId;

    private String chatHistoryId;

}
