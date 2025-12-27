package com.xuenai.aicodegenerate.langgraph.helper;

import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流流式通信助手
 * 用于在 LangGraph 节点内部将数据推送到外层的 SSE 流中
 */
@Component
public class WorkflowStreamHelper {

    // 存储 appId -> FluxSink 的映射
    private final Map<Long, FluxSink<String>> sinkMap = new ConcurrentHashMap<>();

    /**
     * 注册流通道 (在工作流启动时调用)
     */
    public void register(Long appId, FluxSink<String> sink) {
        sinkMap.put(appId, sink);
    }

    /**
     * 发送数据块 (在节点内部调用)
     * 这里发送的 chunk 就是纯文本 token，Controller 会自动封装成 {"d": chunk}
     */
    public void sendChunk(Long appId, String chunk) {
        FluxSink<String> sink = sinkMap.get(appId);
        if (sink != null) {
            sink.next(chunk);
        }
    }

    /**
     * 移除流通道 (工作流结束时调用)
     */
    public void remove(Long appId) {
        sinkMap.remove(appId);
    }
}