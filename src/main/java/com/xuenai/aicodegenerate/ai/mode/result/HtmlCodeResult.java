package com.xuenai.aicodegenerate.ai.mode.result;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * HTML 代码生成结果
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {
    
    @Description("HTML 代码")
    private String html;
    
    @Description("生成代码的描述")
    private String description;
    
}
