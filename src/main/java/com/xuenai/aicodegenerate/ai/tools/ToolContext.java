package com.xuenai.aicodegenerate.ai.tools;

import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工具执行上下文
 * 用于在工具调用时传递项目类型等信息
 */
@Data
@AllArgsConstructor
public class ToolContext {
    /**
     * 应用 ID
     */
    private Long appId;
    
    /**
     * 代码生成类型
     */
    private CodeGenerateTypeEnum codeGenerateType;
    
    /**
     * 获取项目目录名称
     * 例如：html_123, multi_file_123, vue_project_123
     */
    public String getProjectDirName() {
        return codeGenerateType.getValue() + "_" + appId;
    }
}
