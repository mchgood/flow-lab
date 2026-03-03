package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.graph.ExecutableEdge;
import com.gaibu.flowlab.engine.graph.ExecutableGraph;
import com.gaibu.flowlab.engine.graph.ExecutableNode;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.store.VariableStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认执行上下文实现。
 */
public class DefaultExecutionContext implements ExecutionContext {

    /**
     * 流程实例。
     */
    private final ProcessInstance instance;

    /**
     * 当前 Token。
     */
    private final Token token;

    /**
     * 可执行图。
     */
    private final ExecutableGraph graph;

    /**
     * 事件收集器。
     */
    private final List<Object> events = new ArrayList<>();

    public DefaultExecutionContext(ProcessInstance instance, Token token, ExecutableGraph graph) {
        this.instance = instance;
        this.token = token;
        this.graph = graph;
    }

    @Override
    public ProcessInstance instance() {
        return instance;
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public VariableStore variables() {
        return instance.getVariables();
    }

    @Override
    public ExecutableGraph graph() {
        return graph;
    }

    @Override
    public ExecutableNode node() {
        return graph.getNode(token.getCurrentNode());
    }

    @Override
    public List<ExecutableEdge> outgoing() {
        return graph.outgoing(token.getCurrentNode());
    }

    @Override
    public List<ExecutableEdge> incoming() {
        return graph.incoming(token.getCurrentNode());
    }

    @Override
    public void publishEvent(Object event) {
        events.add(event);
    }

    /**
     * 返回上下文内收集的事件。
     *
     * @return 事件列表
     */
    public List<Object> events() {
        return events;
    }
}
