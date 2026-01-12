package com.xuenai.aicodegenerate.model.dto.points;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员人工充值/扣款请求
 */
@Data
public class AdminChargeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID (必填)
     */
    private Long userId;

    /**
     * 变动金额 (必填)
     * 正数 = 充值/奖励 (如: 100)
     * 负数 = 扣款/冲正 (如: -50)
     */
    private Long amount;

    /**
     * 备注/操作原因 (必填)
     * 必须填写，以便后续审计（例如："用户投诉生成失败，人工补发"）
     */
    private String remark;
}
