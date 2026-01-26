package com.xuenai.aicodegenerate.langgraph.graph;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.json.JSONUtil;
import com.xuenai.aicodegenerate.exception.BusinessException;
import com.xuenai.aicodegenerate.exception.ErrorCode;
import com.xuenai.aicodegenerate.langgraph.helper.WorkflowStreamHelper;
import com.xuenai.aicodegenerate.langgraph.model.dto.QualityResult;
import com.xuenai.aicodegenerate.langgraph.node.*;
import com.xuenai.aicodegenerate.langgraph.node.concurrent.*;
import com.xuenai.aicodegenerate.langgraph.state.WorkflowContext;
import com.xuenai.aicodegenerate.model.enums.CodeGenerateTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
@Component
public class CodeGenerateConcurrentWorkflow {

    @Resource
    private WorkflowStreamHelper workflowStreamHelper;
    
    /**
     * 创建并发工作流
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加节点
                    .addNode("image_plan", ImagePlanNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addNode("content_image_collector", ContentImageCollectorNode.create())
                    .addNode("illustration_collector", IllustrationCollectorNode.create())
                    .addNode("diagram_collector", DiagramCollectorNode.create())
                    .addNode("logo_collector", LogoCollectorNode.create())
                    .addNode("image_aggregator", ImageAggregatorNode.create())

                    .addEdge(START, "image_plan")
                    .addEdge("image_plan", "content_image_collector")
                    .addEdge("image_plan", "illustration_collector")
                    .addEdge("image_plan", "diagram_collector")
                    .addEdge("image_plan", "logo_collector")
                    .addEdge("content_image_collector", "image_aggregator")
                    .addEdge("illustration_collector", "image_aggregator")
                    .addEdge("diagram_collector", "image_aggregator")
                    .addEdge("logo_collector", "image_aggregator")
                    .addEdge("image_aggregator", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",
                                    "skip_build", END,
                                    "fail", "code_generator"
                            ))
                    .addEdge("project_builder", END)
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "并发工作流创建失败");
        }
    }

    /**
     * 执行并发工作流 (Flux 流式返回)
     *
     * @param appId          应用 ID
     * @param originalPrompt 原始提示词
     * @return 包含进度信息的流
     */
    public Flux<String> executeWorkflowFlux(Long userId,Long appId, String originalPrompt) {
        return Flux.create(sink -> {
             workflowStreamHelper.register(appId,sink);
             Thread.startVirtualThread(() -> {
                try {
                    CompiledGraph<MessagesState<String>> workflow = createWorkflow();
                    WorkflowContext initialContext = WorkflowContext.builder()
                            .userId(userId)
                            .appId(appId)
                            .originalPrompt(originalPrompt)
                            .currentStep("初始化工作流")
                            .build();

                    sink.next(formatProgress("start", "开始分析需求并规划工作流...", 0));

                    // 配置并发执行器
                    ExecutorService pool = ExecutorBuilder.create()
                            .setCorePoolSize(10)
                            .setMaxPoolSize(20)
                            .setWorkQueue(new LinkedBlockingQueue<>(100))
                            .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("Parallel-Image-Collect").build())
                            .build();
                    RunnableConfig runnableConfig = RunnableConfig.builder()
                            .addParallelNodeExecutor("image_plan", pool)
                            .build();

                    int stepCounter = 1;
                    for (NodeOutput<MessagesState<String>> step : workflow.stream(
                            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext),
                            runnableConfig)) {
                        WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                        String nodeName = step.node();
                        log.info("--- 第 {} 步完成: {} ---", stepCounter, nodeName);
                        
                        if (!"code_generator".equals(nodeName)) {
                            String displayMsg = String.format("步骤 [%s] 执行完成", nodeName);
                            if (currentContext != null && currentContext.getCurrentStep() != null) {
                                displayMsg = currentContext.getCurrentStep();
                            }
                            sink.next(formatProgress("processing", displayMsg, stepCounter));
                        } else {
                            sink.next(formatProgress("processing", "代码生成完毕，进行质检...", stepCounter));
                        }
                        stepCounter++;
                    }
                    sink.next(formatProgress("finish", "工作流执行完毕！", stepCounter));
                    log.info("并发代码生成工作流执行完成！");
                    sink.complete();
                } catch (Exception e) {
                    log.error("工作流执行失败: {}", e.getMessage(), e);
                    sink.next(formatProgress("error", "执行失败: " + e.getMessage(), -1));
                    sink.error(e);
                } finally {
                    workflowStreamHelper.remove(appId);
                }
             });
        });
    }

    /**
     * 格式化进度消息为 JSON 字符串
     * 前端接收逻辑: response.d -> JSON.parse -> { type, content, step }
     */
    private String formatProgress(String type,String content,int step) {
        Map<String,Object> map = Map.of(
                "type",type,
                "content",content,
                "step",step
        );
        return JSONUtil.toJsonStr(map);
    }

    /**
     * 是否符合代码检测规范
     *
     * @param state 当前工作流的状态（数据）
     * @return 下一个节点
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();

        int currentRetry = context.getRetryCount() == null ? 0 : context.getRetryCount();
        int maxRetries = 3;

        if (qualityResult == null || !qualityResult.getIsValid()) {
            if (currentRetry >= maxRetries) {
                log.warn("代码质检失败且达到最大重试次数 ({})，强制结束或降级处理", maxRetries);
                return routeBuildOrSkip(state);
            }

            log.error("代码质检失败，正在进行第 {} 次重试", currentRetry + 1);
            context.setRetryCount(currentRetry + 1);
            return "fail";
        }

        log.info("代码质检通过，继续后续流程");
        context.setRetryCount(0);
        return routeBuildOrSkip(state);
    }

    /**
     * 根据生成类型决定是否需要构建项目
     *
     * @param state 当前工作流的状态（数据）
     * @return 下一个节点
     */
    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenerateTypeEnum generationType = context.getGenerationType();
        // HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenerateTypeEnum.HTML || generationType == CodeGenerateTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        // VUE_PROJECT 需要构建
        return "build";
    }
}

