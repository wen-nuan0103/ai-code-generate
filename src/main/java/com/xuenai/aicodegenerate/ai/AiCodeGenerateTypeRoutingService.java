package com.xuenai.aicodegenerate.ai;

import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI 代码类型生成服务
 */
public interface AiCodeGenerateTypeRoutingService {
    
    /**
     * 生成路由代码类型
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-routing-system-prompt.txt")
    CodeGenerateTypeEnum generateRouteCodeType(String userMessage);
    
}
