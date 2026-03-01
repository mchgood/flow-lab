package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.parser.api.model.Node;
import lombok.Getter;

import java.util.Map;

/**
 * 节点级执行上下文。
 *
 * <p>该对象按节点 attempt 创建，引用同一个流程级 {@link FlowContext}。
 */
@Getter
public class NodeExecutionContext {

    /**
     * 流程级共享上下文。
     */
    private final FlowContext flowContext;
    /**
     * 当前执行节点 id。
     */
    private final String nodeId;
    /**
     * 当前执行节点定义。
     */
    private final Node node;
    /**
     * 当前节点执行尝试次数（0-based）。
     */
    private final int attempt;

    /**
     * 构造NodeExecutionContext实例。
     */
    public NodeExecutionContext(FlowContext flowContext, String nodeId, Node node, int attempt) {
        this.flowContext = flowContext;
        this.nodeId = nodeId;
        this.node = node;
        this.attempt = attempt;
    }

    /**
     * 获取当前执行所属 workflow id。
     */
    public String getWorkflowId() {
        return flowContext.getWorkflowId();
    }

    /**
     * 获取流程共享变量。
     */
    public Map<String, Object> getVariables() {
        return flowContext.getVariables();
    }
}
