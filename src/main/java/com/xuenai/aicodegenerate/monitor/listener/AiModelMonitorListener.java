package com.xuenai.aicodegenerate.monitor.listener;

import com.xuenai.aicodegenerate.ai.utils.AiPriceCalculatorUtil;
import com.xuenai.aicodegenerate.model.dto.ai.log.AiGenerateLogDTO;
import com.xuenai.aicodegenerate.model.enums.AiModelTypeEnum;
import com.xuenai.aicodegenerate.monitor.MonitorContext;
import com.xuenai.aicodegenerate.monitor.MonitorContextHolder;
import com.xuenai.aicodegenerate.monitor.metrics.AiModelMetricsCollector;
import com.xuenai.aicodegenerate.service.AiGenerationLogService;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static com.xuenai.aicodegenerate.model.constant.AiConstant.AI_LISTENER_MONITOR_CONTEXT_KEY;
import static com.xuenai.aicodegenerate.model.constant.AiConstant.AI_LISTENER_REQUEST_START_TIME_KET;

/**
 * AI模型监控监听器
 */
@Slf4j
@Component
public class AiModelMonitorListener implements ChatModelListener {

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;
    @Resource
    private AiGenerationLogService aiGenerationLogService;
    @Resource
    private AiPriceCalculatorUtil aiPriceCalculatorUtil;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestContext.attributes().put(AI_LISTENER_REQUEST_START_TIME_KET, Instant.now());
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        if (monitorContext == null) {
            log.warn("MonitorContext is null in onRequest, using default context");
            monitorContext = MonitorContext.builder()
                    .userId("-1")
                    .appId("-1")
                    .build();
        }
        requestContext.attributes().put(AI_LISTENER_MONITOR_CONTEXT_KEY, monitorContext);

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = requestContext.chatRequest().modelName();

        // 记录请求开始
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Map<Object, Object> attributes = responseContext.attributes();

        // 从 attributes 获取上下文
        MonitorContext monitorContext = (MonitorContext) attributes.get(AI_LISTENER_MONITOR_CONTEXT_KEY);
        if (monitorContext == null) {
            log.error(
                    "MonitorContext is null in onResponse, skipping monitoring. This may indicate context was not set in onRequest.");
            return;
        }

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String taskType = monitorContext.getTaskType() == null ? "CODE_GENERATE" : monitorContext.getTaskType();
        String modelName = responseContext.chatResponse().modelName();

        // 记录请求成功
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        // 记录响应时间
        Duration duration = recordResponseTime(attributes, userId, appId, modelName);
        // 记录token使用量l
        Map<String, Integer> tokenUsageMap = recordTokenUsage(responseContext, userId, appId, modelName);
        AiGenerateLogDTO log = buildLog(duration, tokenUsageMap, userId, modelName,taskType);
        log.setStatus(1);
        log.setTaskType(taskType);
        aiGenerationLogService.saveAiGenerationLog(log);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        Map<Object, Object> attributes = errorContext.attributes();

        // 从 attributes 获取上下文，而非 ThreadLocal
        MonitorContext monitorContext = (MonitorContext) attributes.get(AI_LISTENER_MONITOR_CONTEXT_KEY);
        if (monitorContext == null) {
            log.error(
                    "MonitorContext is null in onError, skipping monitoring. This may indicate context was not set in onRequest.");
            return;
        }

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = errorContext.chatRequest().modelName();
        String message = errorContext.error().getMessage();
        String taskType = monitorContext.getTaskType() == null ? AiModelTypeEnum.CODE_GEN.getValue() : monitorContext.getTaskType();

        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, message);

        Duration duration = recordResponseTime(attributes, userId, appId, modelName);
        AiGenerateLogDTO log = buildLog(duration, new HashMap<>(), userId, modelName,null);
        log.setTaskType(taskType);
        log.setTotalCost(BigDecimal.valueOf(errorContext.chatRequest().maxOutputTokens()));
        log.setStatus(2);
        log.setErrorMsg(message);
        aiGenerationLogService.saveAiGenerationLog(log);
    }

    /**
     * 记录响应时间
     *
     * @param attributes 上下文属性集合
     * @param userId     用户ID
     * @param appId      应用ID
     * @param modelName  模型名称
     */
    private Duration recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(AI_LISTENER_REQUEST_START_TIME_KET);
        Duration responseTime = Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
        return responseTime;
    }

    /**
     * 记录 Token 使用情况
     *
     * @param responseContext 响应上下文
     * @param userId          用户ID
     * @param appId           应用ID
     * @param modelName       模型名称
     */
    private Map<String, Integer> recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId,
                                                  String modelName) {
        Map<String, Integer> map = new HashMap<>();
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
            map.put("input", tokenUsage.inputTokenCount());
            map.put("output", tokenUsage.outputTokenCount());
        }
        return map;
    }

    private AiGenerateLogDTO buildLog(Duration duration, Map<String, Integer> tokenUsageMap, String userId,
                                      String modelName,String modelType) {
        
        Integer inputTokens = tokenUsageMap.getOrDefault("input", 0);
        Integer outputTokens = tokenUsageMap.getOrDefault("output", 0);
        BigDecimal totalCost = new BigDecimal(0);
        if (inputTokens != 0 && outputTokens != 0) {
            totalCost = aiPriceCalculatorUtil.calculateCost(modelName, modelType, inputTokens, outputTokens);
        }
        

        return AiGenerateLogDTO.builder()
                .userId(Long.parseLong(userId))
                .modelName(modelName)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalCost(totalCost)
                .duration(duration.get(ChronoUnit.SECONDS))
                .build();
    }
}
