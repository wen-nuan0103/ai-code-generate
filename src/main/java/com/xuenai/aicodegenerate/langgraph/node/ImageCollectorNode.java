package com.xuenai.aicodegenerate.langgraph.node;

import com.xuenai.aicodegenerate.ai.service.ImageCollectionPlanService;
import com.xuenai.aicodegenerate.langgraph.model.dto.ImageCollectionPlan;
import com.xuenai.aicodegenerate.langgraph.model.dto.ImageResource;
import com.xuenai.aicodegenerate.langgraph.state.WorkflowContext;
import com.xuenai.aicodegenerate.langgraph.tools.ImageSearchTool;
import com.xuenai.aicodegenerate.langgraph.tools.MermaidDiagramTool;
import com.xuenai.aicodegenerate.langgraph.tools.PixabayIllustrationTool;
import com.xuenai.aicodegenerate.monitor.MonitorContext;
import com.xuenai.aicodegenerate.monitor.MonitorContextHolder;
import com.xuenai.aicodegenerate.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 * 使用 AI 进行工具调用，收集不同类型的图片
 */
@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            List<ImageResource> collectedImages = new ArrayList<>();

            try {
                MonitorContextHolder.setContext(MonitorContext.builder()
                        .userId(String.valueOf(context.getUserId()))
                        .taskType("IMAGE_COLLECT")
                        .appId(String.valueOf(context.getAppId()))
                        .build());
                // 获取图片收集计划
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 并发执行各种图片收集任务
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                // 并发执行内容图片搜索
                if (plan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() -> imageSearchTool.searchContentImages(task.queryEn(), task.queryZh())));
                    }
                }
                // 并发执行插画图片搜索
                if (plan.getIllustrationTasks() != null) {
                    PixabayIllustrationTool illustrationTool = SpringContextUtil.getBean(PixabayIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() -> illustrationTool.searchIllustrations(task.queryEn(), task.queryZh())));
                    }
                }
                // 并发执行架构图生成
                if (plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() -> diagramTool.generateArchitectureDiagram(task.mermaidCode(), task.description())));
                    }
                }
//                // 并发执行Logo生成
//                if (plan.getLogoTasks() != null) {
//                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
//                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
//                        futures.add(CompletableFuture.supplyAsync(() -> logoTool.generateLogo(task.description())));
//                    }
//                }

                // 等待所有任务完成并收集结果
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allTasks.join();
                // 收集所有结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }
                log.info("并发图片收集完成，共收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);
            return WorkflowContext.saveContext(context);
        });
    }
}


