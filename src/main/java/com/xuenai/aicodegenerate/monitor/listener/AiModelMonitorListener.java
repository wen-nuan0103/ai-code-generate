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
import java.util.HashMap;
import java.util.Map;

import static com.xuenai.aicodegenerate.model.constant.AiConstant.AI_LISTENER_MONITOR_CONTEXT_KEY;
import static com.xuenai.aicodegenerate.model.constant.AiConstant.AI_LISTENER_REQUEST_START_TIME_KET;

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

        aiModelMetricsCollector.recordRequest(
                monitorContext.getUserId(),
                monitorContext.getAppId(),
                requestContext.chatRequest().modelName(),
                "started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Map<Object, Object> attributes = responseContext.attributes();
        MonitorContext monitorContext = (MonitorContext) attributes.get(AI_LISTENER_MONITOR_CONTEXT_KEY);
        if (monitorContext == null) {
            log.error("MonitorContext is null in onResponse, skipping monitoring.");
            return;
        }

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String taskType = monitorContext.getTaskType() == null ? "CODE_GENERATE" : monitorContext.getTaskType();
        String modelName = responseContext.chatResponse().modelName();

        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        Duration duration = recordResponseTime(attributes, userId, appId, modelName);
        Map<String, Integer> tokenUsageMap = recordTokenUsage(responseContext, userId, appId, modelName);

        AiGenerateLogDTO logDTO = buildLog(duration, tokenUsageMap, userId, modelName, taskType);
        logDTO.setStatus(1);
        logDTO.setTaskType(taskType);
        saveLogQuietly(logDTO);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        Map<Object, Object> attributes = errorContext.attributes();
        MonitorContext monitorContext = (MonitorContext) attributes.get(AI_LISTENER_MONITOR_CONTEXT_KEY);
        if (monitorContext == null) {
            log.error("MonitorContext is null in onError, skipping monitoring.");
            return;
        }

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = errorContext.chatRequest().modelName();
        String message = errorContext.error() == null ? "unknown error" : errorContext.error().getMessage();
        String taskType = monitorContext.getTaskType() == null
                ? AiModelTypeEnum.CODE_GEN.getValue()
                : monitorContext.getTaskType();

        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, message);

        Duration duration = recordResponseTime(attributes, userId, appId, modelName);
        AiGenerateLogDTO logDTO = buildLog(duration, new HashMap<>(), userId, modelName, taskType);
        logDTO.setTaskType(taskType);
        logDTO.setTotalCost(BigDecimal.ZERO);
        logDTO.setStatus(2);
        logDTO.setErrorMsg(message);
        saveLogQuietly(logDTO);
    }

    private Duration recordResponseTime(
            Map<Object, Object> attributes,
            String userId,
            String appId,
            String modelName) {
        Instant startTime = (Instant) attributes.get(AI_LISTENER_REQUEST_START_TIME_KET);
        Duration responseTime = startTime == null ? Duration.ZERO : Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
        return responseTime;
    }

    private Map<String, Integer> recordTokenUsage(
            ChatModelResponseContext responseContext,
            String userId,
            String appId,
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

    private AiGenerateLogDTO buildLog(
            Duration duration,
            Map<String, Integer> tokenUsageMap,
            String userId,
            String modelName,
            String modelType) {
        Integer inputTokens = tokenUsageMap.getOrDefault("input", 0);
        Integer outputTokens = tokenUsageMap.getOrDefault("output", 0);

        BigDecimal totalCost = BigDecimal.ZERO;
        if (inputTokens != 0 || outputTokens != 0) {
            totalCost = aiPriceCalculatorUtil.calculateCost(modelName, modelType, inputTokens, outputTokens);
        }

        return AiGenerateLogDTO.builder()
                .userId(Long.parseLong(userId))
                .modelName(modelName)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .totalCost(totalCost)
                .duration(duration.toMillis())
                .build();
    }

    private void saveLogQuietly(AiGenerateLogDTO logDTO) {
        try {
            aiGenerationLogService.saveAiGenerationLog(logDTO);
        } catch (Exception e) {
            log.warn("Failed to save ai generation log, modelName={}, taskType={}",
                    logDTO.getModelName(), logDTO.getTaskType(), e);
        }
    }
}
