package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;

/**
 * 决策节点执行器 - 处理菱形节点作为条件分支
 */
public class DecisionNodeExecutor implements NodeExecutor {
    @Override
    public String getSupportedShape() {
        return "diamond";
    }

    @Override
    public NodeExecutionResult execute(Node node, ExecutionContext context) {
        // 决策节点本身不执行逻辑，只是路由
        // 实际的条件判断在流程引擎的 determineNextNodes 方法中进行
        return NodeExecutionResult.builder()
                .success(true)
                .status(NodeExecutionStatus.SUCCESS)
                .build();
    }

    @Override
    public boolean validate(Node node) {
        // 验证节点是否为菱形
        return "diamond".equals(node.getShape());
    }
}
