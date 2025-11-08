package com.xuenai.aicodegenerate.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AppChatScopeStatusEnum {

    PRIVATE("私有", 0),
    PUBLIC("公开", 1);

    private final String text;
    private final Integer value;

    AppChatScopeStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AppChatScopeStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AppChatScopeStatusEnum anEnum : AppChatScopeStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
    
}
