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
 * AI模型配置表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_model_info")
public class AiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 展示名称
     */
    private String modelName;

    /**
     * 调用传参值 
     */
    private String modelCode;
    
    /**
     * 模型类型 
     */
    private String modelType;

    /**
     * 供应商 
     */
    private String provider;

    /**
     * 接口地址 (支持反代地址)
     */
    private String baseUrl;

    /**
     * 最大上下文窗口
     */
    private Integer maxTokens;

    /**
     * 输入价格/1k token
     */
    private BigDecimal inputPrice;

    /**
     * 输出价格/1k token
     */
    private BigDecimal outputPrice;

    /**
     * 状态: 1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
}
