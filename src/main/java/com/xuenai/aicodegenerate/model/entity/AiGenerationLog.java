package com.xuenai.aicodegenerate.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI生成日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_generation_log")
public class AiGenerationLog implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当时使用的模型
     */
    private String modelName;

    /**
     * 输入的token数量
     */
    private Integer inputTokens;

    /**
     * 输出的token数量
     */
    private Integer outputTokens;

    /**
     * 计算出的成本
     */
    private BigDecimal totalCost;

    /**
     * 1-成功 2-失败
     */
    private Integer status;

    /**
     * 报错信息
     */
    private String errorMsg;

    /**
     * 耗时(ms)
     */
    private Long duration;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
