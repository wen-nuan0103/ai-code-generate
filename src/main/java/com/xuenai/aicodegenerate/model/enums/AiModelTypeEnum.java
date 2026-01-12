package com.xuenai.aicodegenerate.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiModelTypeEnum {
    
    CHAT("CHAT", "通用对话"),
    SIMPLE("SIMPLE","简单任务"),
    CODE_GEN("CODE_GEN", "代码生成"),
    CODE_AUDIT("CODE_AUDIT", "代码审计"),
    IMAGE_COLLECT("IMAGE_COLLECT", "图片收集"),
    EMBEDDING("EMBEDDING", "知识库向量");

    private final String value;
    private final String text;

    public static AiModelTypeEnum getByValue(String value) {
        for (AiModelTypeEnum anEnum : AiModelTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
