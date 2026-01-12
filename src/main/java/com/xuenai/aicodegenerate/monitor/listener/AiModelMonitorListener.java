package com.xuenai.aicodegenerate.monitor.listener;

import com.xuenai.aicodegenerate.monitor.MonitorContext;
import com.xuenai.aicodegenerate.monitor.MonitorContextHolder;
import com.xuenai.aicodegenerate.monitor.metrics.AiModelMetricsCollector;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
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

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestContext.attributes().put(AI_LISTENER_REQUEST_START_TIME_KET, Instant.now());
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        requestContext.attributes().put(AI_LISTENER_MONITOR_CONTEXT_KEY, monitorContext);
        String modelName = requestContext.chatRequest().modelName();
//        记录请求开始
        aiModelMetricsCollector.recordRequest(userId, appId, modelName,"started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Map<Object, Object> attributes = responseContext.attributes();
        MonitorContext monitorContext = (MonitorContext) attributes.get(AI_LISTENER_MONITOR_CONTEXT_KEY);
        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = responseContext.chatResponse().modelName();
//        记录请求成功
        aiModelMetricsCollector.recordRequest(userId, appId, modelName,"success");
//        记录响应时间
        recordResponseTime(attributes,userId,appId,modelName);
//        记录token使用量
        recordTokenUsage(responseContext,userId,appId,modelName);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = errorContext.chatRequest().modelName();
        String message = errorContext.error().getMessage();
//        记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName,"error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, message);
        Map<Object, Object> attributes = errorContext.attributes();
        recordResponseTime(attributes,userId,appId,modelName);
    }

    /**
     * 记录响应时间
     * @param attributes 上下文属性集合
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     */
    private void recordResponseTime(Map<Object,Object> attributes,String userId,String appId,String modelName){
        Instant startTime = (Instant) attributes.get(AI_LISTENER_REQUEST_START_TIME_KET);
        Duration responseTime = Duration.between(startTime, Instant.now());
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }

    /**
     * 记录 Token 使用情况
     * @param responseContext 响应上下文
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext,String userId,String appId
            ,String modelName){
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName,"input",tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName,"output",tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName,"total",tokenUsage.totalTokenCount());
        }
    }
}
