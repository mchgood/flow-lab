package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;

/**
 * 任务节点执行器 - 处理矩形节点作为任务执行单元
 */
public class TaskNodeExecutor implements NodeExecutor {
    @Override
    public String getSupportedShape() {
        return "rectangle";
    }

    @Override
    public NodeExecutionResult execute(Node node, ExecutionContext context) {
        // 任务节点的具体执行逻辑
        // 可以通过节点标签来确定具体的任务类型
        // 这里提供一个基础实现，实际使用时可以扩展

        // 记录任务执行
        context.setVariable("lastExecutedTask", node.getLabel());

        return NodeExecutionResult.builder()
                .success(true)
                .status(NodeExecutionStatus.SUCCESS)
                .build();
    }

    @Override
    public boolean validate(Node node) {
        // 验证节点是否为矩形
        return "rectangle".equals(node.getShape());
    }
}
