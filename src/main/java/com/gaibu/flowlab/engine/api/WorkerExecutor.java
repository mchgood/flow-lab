package com.gaibu.flowlab.engine.api;

import com.gaibu.flowlab.engine.model.NodeExecutionContext;
import com.gaibu.flowlab.engine.model.NodeResult;

/**
 * 节点执行器。
 */
public interface WorkerExecutor {

    NodeResult execute(NodeExecutionContext context);
}
