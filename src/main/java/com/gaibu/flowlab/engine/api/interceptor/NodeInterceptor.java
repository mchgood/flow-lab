package com.gaibu.flowlab.engine.api.interceptor;

import com.gaibu.flowlab.engine.model.NodeExecutionContext;
import com.gaibu.flowlab.engine.model.NodeResult;

/**
 * 节点维度拦截器。
 */
public interface NodeInterceptor {

    default boolean supportsNode(String workflowId, String nodeId) {
        return true;
    }

    default void before(NodeExecutionContext context) {
    }

    default void onSuccess(NodeExecutionContext context, NodeResult result) {
    }

    default void onFailure(NodeExecutionContext context, NodeResult result) {
    }
}
