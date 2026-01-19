package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;

/**
 * 结束节点执行器 - 处理圆形节点作为流程终点
 */
public class EndNodeExecutor implements NodeExecutor {
    @Override
    public String getSupportedShape() {
        return "circle";
    }

    @Override
    public NodeExecutionResult execute(Node node, ExecutionContext context) {
        // 结束节点标记流程完成
        context.setVariable("processCompleted", true);

        return NodeExecutionResult.builder()
                .success(true)
                .status(NodeExecutionStatus.SUCCESS)
                .build();
    }

    @Override
    public boolean validate(Node node) {
        // 验证节点是否为圆形且标记为 end/stop
        return "circle".equals(node.getShape()) &&
                ("end".equalsIgnoreCase(node.getLabel()) || "stop".equalsIgnoreCase(node.getLabel())
                        || "end".equalsIgnoreCase(node.getId()) || "stop".equalsIgnoreCase(node.getId()));
    }
}
