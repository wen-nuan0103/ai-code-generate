package com.xuenai.aicodegenerate.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AppDeployStatusEnum {
    
    UN_DEPLOY("未部署", 0),
    DEPLOYED("已部署", 1),
    DEPLOY_FAIL("部署失败", 2);

    private final String text;
    private final Integer value;
    
    AppDeployStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static AppDeployStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AppDeployStatusEnum anEnum : AppDeployStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
