package com.xuenai.aicodegenerate.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    /**
     * 工具上下文（可选）
     * 用于传递项目类型等信息
     */
    protected ToolContext toolContext;

    /**
     * 设置工具上下文
     */
    public void setToolContext(ToolContext context) {
        this.toolContext = context;
    }

    /**
     * 获取项目目录名称
     * 优先使用上下文中的信息，如果没有则使用默认的 vue_project
     */
    protected String getProjectDirName(Long appId) {
        if (toolContext != null) {
            return toolContext.getProjectDirName();
        }
        // 兼容旧逻辑，默认返回 vue_project
        return "vue_project_" + appId;
    }

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);
}

