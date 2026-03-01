package com.gaibu.flowlab.engine.api;

import com.gaibu.flowlab.engine.model.WorkflowExecutionResult;

import java.util.Map;

/**
 * Workflow 执行入口。
 */
public interface WorkflowExecutor {

    WorkflowExecutionResult execute(String workflowId, Map<String, Object> variables);
}

