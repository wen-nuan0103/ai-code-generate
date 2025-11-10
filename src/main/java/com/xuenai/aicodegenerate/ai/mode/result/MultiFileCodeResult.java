package com.xuenai.aicodegenerate.ai.mode.result;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件 代码生成结果
 */
@Description("生成多文件代码文件的结果")
@Data
public class MultiFileCodeResult {
    
    @Description("HTML 代码")
    private String html;
    
    @Description("CSS 代码")
    private String css;
    
    @Description("JavaScript 代码")
    private String javaScript;
    
    @Description("生成代码的描述")
    private String description;
    
}
