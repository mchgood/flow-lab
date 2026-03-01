package com.gaibu.flowlab.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工作流级共享上下文。
 *
 * <p>同一次 workflow 执行期间仅创建一个实例，在所有节点执行中共享。
 */
@Getter
@Setter
public class FlowContext {

    /**
     * 当前执行所属 workflow id。
     */
    private String workflowId;
    /**
     * 流程共享变量容器（整个执行过程共享同一份）。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * 构造FlowContext实例。
     */
    public FlowContext() {
    }

    /**
     * 构造FlowContext实例。
     */
    public FlowContext(String workflowId, Map<String, Object> variables) {
        this.workflowId = workflowId;
        this.variables = variables == null ? new LinkedHashMap<>() : variables;
    }

}
