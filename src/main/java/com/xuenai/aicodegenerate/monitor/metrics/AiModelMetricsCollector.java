package com.xuenai.aicodegenerate.monitor.metrics;

import com.xuenai.aicodegenerate.service.AiGenerationLogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型指标收集器
 */
@Slf4j
@Component
public class AiModelMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;


    // 指标缓存器
    private final ConcurrentHashMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> responseTimerCache = new ConcurrentHashMap<>();


    /**
     * 记录请求次数
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     * @param status 状态
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_request_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tags("app_id", appId)
                        .tags("model_name", modelName)
                        .tags("status", status)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录错误次数
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     * @param errorMessage 错误信息
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_error_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tags("app_id", appId)
                        .tags("model_name", modelName)
                        .tags("error_message", errorMessage)
                        .register(meterRegistry)
        );
        counter.increment();
    }


    /**
     * 记录 Token 消耗
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     * @param tokenType token类型（输入、输出、总数）
     * @param tokenCount 消耗数量
     */
    public void recordTokenUsage(String userId, String appId, String modelName, String tokenType,long tokenCount) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")
                        .description("AI模型 Token 消耗总数")
                        .tag("user_id", userId)
                        .tags("app_id", appId)
                        .tags("model_name", modelName)
                        .tags("token_type", tokenType)
                        .register(meterRegistry)
        );
        counter.increment(tokenCount);
    }

    /**
     * 记录响应时间
     * 
     * @param userId 用户ID
     * @param appId 应用ID
     * @param modelName 模型名称
     * @param duration 响应时间
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        String key = String.format("%s_%s_%s", userId, appId, modelName);
        Timer timer = responseTimerCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_time")
                        .description("AI模型响应时间")
                        .tag("user_id", userId)
                        .tags("app_id", appId)
                        .tags("model_name", modelName)
                        .register(meterRegistry)
        );
        timer.record(duration);
    }
    
}
