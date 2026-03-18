package dev.langchain4j.internal;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNotNullOrEmpty;

@Internal
public class ToolExecutionRequestBuilder {

    private final AtomicReference<Integer> index;
    private final AtomicReference<String> id = new AtomicReference<>();
    private final AtomicReference<String> name = new AtomicReference<>();
    private final StringBuffer arguments = new StringBuffer();
    private final List<ToolExecutionRequest> allToolExecutionRequests = new ArrayList<>();

    public ToolExecutionRequestBuilder() {
        this(0);
    }

    public ToolExecutionRequestBuilder(int index) {
        this.index = new AtomicReference<>(index);
    }

    public int index() {
        return index.get();
    }

    public int updateIndex(Integer index) {
        if (index != null) {
            this.index.set(index);
        }
        return this.index.get();
    }

    public String id() {
        return id.get();
    }

    public String updateId(String id) {
        if (isNotNullOrBlank(id)) {
            this.id.set(id);
        }
        return this.id.get();
    }

    public String name() {
        return name.get();
    }

    public String updateName(String name) {
        if (isNotNullOrBlank(name)) {
            this.name.set(name);
        }
        return this.name.get();
    }

    public void appendArguments(String partialArguments) {
        if (isNotNullOrEmpty(partialArguments)) {
            arguments.append(partialArguments);
        }
    }

    public ToolExecutionRequest build() {
        String finalArguments = this.arguments.toString();
        ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                .id(id.get())
                .name(name.get())
                .arguments(finalArguments.isEmpty() ? "{}" : finalArguments)
                .build();
        allToolExecutionRequests.add(toolExecutionRequest);
        reset();
        return toolExecutionRequest;
    }

    private void reset() {
        id.set(null);
        name.set(null);
        arguments.setLength(0);
    }

    public boolean hasToolExecutionRequests() {
        return !allToolExecutionRequests.isEmpty()
                || isNotNullOrBlank(name.get())
                || isNotNullOrEmpty(arguments.toString());
    }

    public List<ToolExecutionRequest> allToolExecutionRequests() {
        return allToolExecutionRequests;
    }
}
