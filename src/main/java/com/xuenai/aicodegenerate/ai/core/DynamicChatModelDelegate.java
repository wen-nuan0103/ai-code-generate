package com.xuenai.aicodegenerate.ai.core;

import com.xuenai.aicodegenerate.ai.DynamicAiModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 动态模型代理
 * 作用：让单例 Bean 也能每次请求都动态选择最新的模型
 */
@AllArgsConstructor
public class DynamicChatModelDelegate implements ChatModel {

    private final String targetModelType;

    private final DynamicAiModelFactory dynamicAiModelFactory;

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        ChatModel realModel = dynamicAiModelFactory.getChatModel(targetModelType);
        return realModel.chat(chatRequest);
    }
    
    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        ChatModel realModel = dynamicAiModelFactory.getChatModel(targetModelType);
        return realModel.chat(messages);
    }
}
