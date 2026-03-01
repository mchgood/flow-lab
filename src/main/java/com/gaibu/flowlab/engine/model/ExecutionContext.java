package com.gaibu.flowlab.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 执行上下文。
 */
@Getter
@Setter
public class ExecutionContext {

    /**
     * 工作流级变量快照，节点执行时从该容器读取和写回变量。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * 构造ExecutionContext实例。
     */
    public ExecutionContext() {
    }

    /**
     * 构造ExecutionContext实例。
     */
    public ExecutionContext(Map<String, Object> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
    }

}
