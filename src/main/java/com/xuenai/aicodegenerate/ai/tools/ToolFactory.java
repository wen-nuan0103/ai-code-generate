package com.xuenai.aicodegenerate.ai.tools;

import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 工具工厂
 * 为每个应用创建带有上下文的工具实例
 */
@Slf4j
@Component
public class ToolFactory {
    
    private final ToolManage toolManage;
    
    public ToolFactory(ToolManage toolManage) {
        this.toolManage = toolManage;
    }
    
    /**
     * 创建带有上下文的工具数组
     * 
     * @param appId 应用 ID
     * @param codeGenerateType 代码生成类型
     * @return 工具数组
     */
    public Object[] createToolsWithContext(Long appId, CodeGenerateTypeEnum codeGenerateType) {
        BaseTool[] originalTools = toolManage.getTools();
        ToolContext context = new ToolContext(appId, codeGenerateType);
        return Arrays.stream(originalTools)
                .peek(tool -> {
                    tool.setToolContext(context);
                    log.debug("为工具 {} 设置上下文: appId={}, type={}", 
                            tool.getToolName(), appId, codeGenerateType.getValue());
                })
                .toArray(BaseTool[]::new);
    }
}
