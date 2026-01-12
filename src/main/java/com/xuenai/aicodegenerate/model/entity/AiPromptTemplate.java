package com.xuenai.aicodegenerate.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Prompt提示词模板
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_prompt_template")
public class AiPromptTemplate implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 默认建议模型ID
     */
    private Long modelId;

    /**
     * 系统预设 (System Prompt)
     */
    private String systemMessage;

    /**
     * 用户输入模板 (含 {{variable}})
     */
    private String userMessage;

    /**
     * 默认参数 (温度、TopP等)
     * 使用 TypeHandler 自动转为 Map
     */
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> parameters;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
}
