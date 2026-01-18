package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;

/**
 * 节点执行器接口 - 定义节点执行的标准行为
 */
public interface NodeExecutor {
    /**
     * 获取支持的节点形状
     * @return 节点形状（如：rectangle, diamond, circle等）
     */
    String getSupportedShape();

    /**
     * 执行节点
     * @param node 节点信息
     * @param context 执行上下文
     * @return 节点执行结果
     */
    NodeExecutionResult execute(Node node, ExecutionContext context);

    /**
     * 验证节点配置
     * @param node 节点信息
     * @return 是否有效
     */
    boolean validate(Node node);
}
