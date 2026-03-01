package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.WorkerExecutor;
import com.gaibu.flowlab.engine.model.NodeExecutionContext;
import com.gaibu.flowlab.engine.model.NodeResult;

import java.util.Map;

/**
 * 默认节点执行器。
 */
public class DefaultWorkerExecutor implements WorkerExecutor {

    @Override
    /**
     * 执行execute并返回结果。
     * @return 执行结果
     */
    public NodeResult execute(NodeExecutionContext context) {
        return NodeResult.success(Map.of());
    }
}
