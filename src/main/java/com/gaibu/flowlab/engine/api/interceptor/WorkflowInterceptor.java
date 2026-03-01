package com.gaibu.flowlab.engine.api.interceptor;

import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import com.gaibu.flowlab.engine.model.WorkflowExecutionResult;

import java.util.Map;

/**
 * 流程维度拦截器。
 */
public interface WorkflowInterceptor {

    default boolean supportsWorkflow(String workflowId) {
        return true;
    }

    default void before(String workflowId, Map<String, Object> variables) {
    }

    default void onSuccess(WorkflowExecutionResult result) {
    }

    default void onFailure(String workflowId,
                           Map<String, Object> variables,
                           ExecutionState state,
                           Throwable error) {
    }
}
