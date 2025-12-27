package com.xuenai.aicodegenerate.ai.handler;

import com.xuenai.aicodegenerate.model.entity.User;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import com.xuenai.aicodegenerate.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器：
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    @Resource
    private SimpleTextStreamHandler simpleTextStreamHandler;

    @Resource
    private WorkflowStreamHandler workflowStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param generateTypeEnum   代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux, ChatHistoryService chatHistoryService, long appId, User loginUser, CodeGenerateTypeEnum generateTypeEnum) {
        return switch (generateTypeEnum) {
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
            case HTML, MULTI_FILE -> simpleTextStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }

    /**
     * 专门处理 LangGraph 工作流的输出
     * * @param workflowFlux       工作流产生的流
     *
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> doExecuteWorkflow(Flux<String> workflowFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        return workflowStreamHandler.handle(workflowFlux, chatHistoryService, appId, loginUser);
    }
}

