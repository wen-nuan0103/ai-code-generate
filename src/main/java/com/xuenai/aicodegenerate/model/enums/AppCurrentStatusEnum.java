package com.xuenai.aicodegenerate.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AppCurrentStatusEnum {
    
    GENERATE("生成中", 0),
    FINISH("生成完成", 1),
    DISRUPT("生成中断", 2);

    private final String text;
    private final Integer value;

    AppCurrentStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AppCurrentStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AppCurrentStatusEnum anEnum : AppCurrentStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
    
}
