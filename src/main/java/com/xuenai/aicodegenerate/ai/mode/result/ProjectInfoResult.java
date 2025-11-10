package com.xuenai.aicodegenerate.ai.mode.result;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.util.List;

/**
 * 项目信息
 */
@Description("生成项目信息的结果")
@Data
public class ProjectInfoResult {
    
    @Description("项目名称")
    private String name;

    @Description("项目标签（例如 [\"Java\", \"Vue\", \"餐饮\"]）")
    private List<String> tags;
    
}
