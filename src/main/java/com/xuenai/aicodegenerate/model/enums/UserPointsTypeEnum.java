package com.xuenai.aicodegenerate.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分变动类型枚举
 */
@Getter
@AllArgsConstructor
public enum UserPointsTypeEnum {

    // --- 收入类 (增加积分) ---
    REGISTER(1, "注册赠送", 10000L ,true),
    INVITE(2, "邀请奖励", 5000L, true),
    SIGN_IN(3, "每日签到", 0, true),
    RECHARGE(4, "充值购买", 0, true),
    REFUND(5, "失败退款", 0,true), 
    SYSTEM_ADD(6, "系统/人工补偿",0, true),

    // --- 支出类 (减少积分) ---
    SYSTEM_DEDUCT(7, "系统/人工扣除", 0,false),
    AI_USE(101, "AI生成消耗", 0,false),
    APP_PUBLISH(102, "应用发布消耗", 0,false);

    private final int value;
    private final String text;
    private final long points;
    /**
     * 是否为收入 (true=增加积分/显示绿色, false=消耗积分/显示红色)
     */
    private final boolean isIncome;

    /**
     * 根据 value 获取枚举
     */
    public static UserPointsTypeEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserPointsTypeEnum anEnum : UserPointsTypeEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 辅助方法：判断是否为增加类型
     */
    public static boolean isAddType(Integer value) {
        UserPointsTypeEnum typeEnum = getEnumByValue(value);
        return typeEnum != null && typeEnum.isIncome;
    }
}