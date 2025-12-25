package com.xuenai.aicodegenerate.ai.service;

import com.xuenai.aicodegenerate.ai.mode.result.ProjectInfoResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI 项目信息生成服务
 */
public interface AiProjectInfoService {

    /**
     * 生成项目信息
     *
     * @param memoryId    记忆 ID (用于隔离上下文)
     * @param userMessage 用户消息
     * @return 生成的项目信息
     */
    @SystemMessage(fromResource = "prompt/generate-app-info-system-prompt.txt")
    ProjectInfoResult generateProjectInfo(@MemoryId String memoryId, @UserMessage String userMessage);
}
