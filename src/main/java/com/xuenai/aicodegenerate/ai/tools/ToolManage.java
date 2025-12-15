package com.xuenai.aicodegenerate.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolManage {

    private final Map<String, BaseTool> toolMap = new HashMap<>();

    @Getter
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
    }

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具名称（英文）
     * @return 工具实例
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

}
