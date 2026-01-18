package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;

/**
 * 开始节点执行器 - 处理圆形节点作为流程起点
 */
public class StartNodeExecutor implements NodeExecutor {
    @Override
    public String getSupportedShape() {
        return "circle";
    }

    @Override
    public NodeExecutionResult execute(Node node, ExecutionContext context) {
        // 开始节点不需要执行任何逻辑，直接返回成功
        return NodeExecutionResult.builder()
                .success(true)
                .status(NodeExecutionStatus.SUCCESS)
                .build();
    }

    @Override
    public boolean validate(Node node) {
        // 验证节点是否为圆形
        return "circle".equals(node.getShape());
    }
}
