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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_points_log")
public class Points implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 变动金额(正数为增加，负数为减少)
     */
    private Long amount;

    /**
     * 变动类型：1-注册 2-签到 3-充值 4-退款 101-AI消耗
     */
    private Integer type;

    /**
     * 变动后的剩余积分(快照)
     */
    private Long currentPoints;

    /**
     * 业务唯一流水号(去重用)
     */
    private String bizNo;

    /**
     * 关联id (若是邀请奖励，填被邀请人id；若是消耗，填生成任务id)
     */
    private Long refId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
}
