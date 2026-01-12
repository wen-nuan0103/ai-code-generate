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
 * API密钥管理表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_api_key")
public class AiApiKey  implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 对应供应商 
     */
    private String provider;

    /**
     * 秘钥
     */
    private String accessKey;

    /**
     * 剩余额度(可选)
     */
    private BigDecimal balance;

    /**
     * 状态: 1-正常 0-停用 -1-余额耗尽
     */
    private Integer status;

    /**
     * 上次使用时间(用于轮询算法)
     */
    private LocalDateTime lastUsedTime;

    /**
     * 连续报错次数
     */
    private Integer errorCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
}
