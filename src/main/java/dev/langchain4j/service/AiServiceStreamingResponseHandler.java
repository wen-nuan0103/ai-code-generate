package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Handles response from a language model for AI Service that is streamed token-by-token.
 * Handles both regular (text) responses and responses with tool execution requests.
 */
@Internal
class AiServiceStreamingResponseHandler implements StreamingChatResponseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AiServiceStreamingResponseHandler.class);

    private final ChatExecutor chatExecutor;
    private final AiServiceContext context;
    private final Object memoryId;
    private final GuardrailRequestParams commonGuardrailParams;
    private final Object methodKey;

    private final Consumer<String> partialResponseHandler;
    private final BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler;
    private final BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler;
    private final Consumer<ToolExecution> toolExecutionHandler;
    private final Consumer<ChatResponse> completeResponseHandler;
    private final Consumer<Throwable> errorHandler;

    private final ChatMemory temporaryMemory;
    private final TokenUsage tokenUsage;
    private final List<ToolSpecification> toolSpecifications;
    private final Map<String, ToolExecutor> toolExecutors;
    private final List<String> responseBuffer = new ArrayList<>();
    private final boolean hasOutputGuardrails;

    AiServiceStreamingResponseHandler(
            ChatExecutor chatExecutor,
            AiServiceContext context,
            Object memoryId,
            Consumer<String> partialResponseHandler,
            BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler,
            BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler,
            Consumer<ToolExecution> toolExecutionHandler,
            Consumer<ChatResponse> completeResponseHandler,
            Consumer<Throwable> errorHandler,
            ChatMemory temporaryMemory,
            TokenUsage tokenUsage,
            List<ToolSpecification> toolSpecifications,
            Map<String, ToolExecutor> toolExecutors,
            GuardrailRequestParams commonGuardrailParams,
            Object methodKey) {
        this.chatExecutor = ensureNotNull(chatExecutor, "chatExecutor");
        this.context = ensureNotNull(context, "context");
        this.memoryId = ensureNotNull(memoryId, "memoryId");
        this.methodKey = methodKey;
        this.partialResponseHandler = ensureNotNull(partialResponseHandler, "partialResponseHandler");
        this.partialToolExecutionRequestHandler = partialToolExecutionRequestHandler;
        this.completeToolExecutionRequestHandler = completeToolExecutionRequestHandler;
        this.toolExecutionHandler = toolExecutionHandler;
        this.completeResponseHandler = completeResponseHandler;
        this.errorHandler = errorHandler;
        this.temporaryMemory = temporaryMemory;
        this.tokenUsage = ensureNotNull(tokenUsage, "tokenUsage");
        this.toolSpecifications = copy(toolSpecifications);
        this.toolExecutors = toolExecutors;
        this.commonGuardrailParams = commonGuardrailParams;
        this.hasOutputGuardrails = context.guardrailService().hasOutputGuardrails(methodKey);
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        if (hasOutputGuardrails) {
            responseBuffer.add(partialResponse);
        } else {
            partialResponseHandler.accept(partialResponse);
        }
    }

    @Override
    public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
        if (partialToolExecutionRequestHandler != null) {
            partialToolExecutionRequestHandler.accept(index, partialToolExecutionRequest);
        }
    }

    @Override
    public void onCompleteToolExecutionRequest(int index, ToolExecutionRequest completeToolExecutionRequest) {
        if (completeToolExecutionRequestHandler != null) {
            completeToolExecutionRequestHandler.accept(index, completeToolExecutionRequest);
        }
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        AiMessage aiMessage = completeResponse.aiMessage();
        addToMemory(aiMessage);

        if (aiMessage.hasToolExecutionRequests()) {
            for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                String toolExecutionResult = executeTool(toolExecutionRequest);
                ToolExecutionResultMessage toolExecutionResultMessage =
                        ToolExecutionResultMessage.from(toolExecutionRequest, toolExecutionResult);
                addToMemory(toolExecutionResultMessage);

                if (toolExecutionHandler != null) {
                    ToolExecution toolExecution = ToolExecution.builder()
                            .request(toolExecutionRequest)
                            .result(toolExecutionResult)
                            .build();
                    toolExecutionHandler.accept(toolExecution);
                }
            }

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messagesToSend(memoryId))
                    .toolSpecifications(toolSpecifications)
                    .build();

            var handler = new AiServiceStreamingResponseHandler(
                    chatExecutor,
                    context,
                    memoryId,
                    partialResponseHandler,
                    partialToolExecutionRequestHandler,
                    completeToolExecutionRequestHandler,
                    toolExecutionHandler,
                    completeResponseHandler,
                    errorHandler,
                    temporaryMemory,
                    TokenUsage.sum(tokenUsage, completeResponse.metadata().tokenUsage()),
                    toolSpecifications,
                    toolExecutors,
                    commonGuardrailParams,
                    methodKey);

            context.streamingChatModel.chat(chatRequest, handler);
            return;
        }

        if (completeResponseHandler == null) {
            return;
        }

        ChatResponse finalChatResponse = ChatResponse.builder()
                .aiMessage(aiMessage)
                .metadata(completeResponse.metadata().toBuilder()
                        .tokenUsage(tokenUsage.add(completeResponse.metadata().tokenUsage()))
                        .build())
                .build();

        if (hasOutputGuardrails) {
            if (commonGuardrailParams != null) {
                var newCommonParams = GuardrailRequestParams.builder()
                        .chatMemory(getMemory())
                        .augmentationResult(commonGuardrailParams.augmentationResult())
                        .userMessageTemplate(commonGuardrailParams.userMessageTemplate())
                        .variables(commonGuardrailParams.variables())
                        .build();

                var outputGuardrailParams = OutputGuardrailRequest.builder()
                        .responseFromLLM(finalChatResponse)
                        .chatExecutor(chatExecutor)
                        .requestParams(newCommonParams)
                        .build();

                finalChatResponse =
                        context.guardrailService().executeGuardrails(methodKey, outputGuardrailParams);
            }

            responseBuffer.forEach(partialResponseHandler::accept);
            responseBuffer.clear();
        }

        completeResponseHandler.accept(finalChatResponse);
    }

    private String executeTool(ToolExecutionRequest toolExecutionRequest) {
        String toolName = toolExecutionRequest.name();
        if (toolName == null || toolName.isBlank()) {
            LOG.warn("Ignoring tool execution request without a name: id={}, arguments={}",
                    toolExecutionRequest.id(), toolExecutionRequest.arguments());
            return "Error: tool name is missing in the model response";
        }

        ToolExecutor toolExecutor = toolExecutors == null ? null : toolExecutors.get(toolName);
        if (toolExecutor == null) {
            LOG.warn("Tool executor not found for '{}', available tools: {}",
                    toolName, toolExecutors == null ? List.of() : toolExecutors.keySet());
            return "Error: there is no tool called " + toolName;
        }

        return toolExecutor.execute(toolExecutionRequest, memoryId);
    }

    private ChatMemory getMemory() {
        return getMemory(memoryId);
    }

    private ChatMemory getMemory(Object memId) {
        return context.hasChatMemory() ? context.chatMemoryService.getOrCreateChatMemory(memId) : temporaryMemory;
    }

    private void addToMemory(ChatMessage chatMessage) {
        getMemory().add(chatMessage);
    }

    private List<ChatMessage> messagesToSend(Object memId) {
        return getMemory(memId).messages();
    }

    @Override
    public void onError(Throwable error) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(error);
            } catch (Exception e) {
                LOG.error("While handling the following error...", error);
                LOG.error("...the following error happened", e);
            }
        } else {
            LOG.warn("Ignored error", error);
        }
    }
}
