package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import com.gaibu.flowlab.engine.expression.impl.SpelExpressionEngine;
import com.gaibu.flowlab.engine.task.TaskRegistry;
import com.gaibu.flowlab.engine.task.impl.InMemoryTaskRegistry;
import com.gaibu.flowlab.parser.model.entity.Node;
import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;

/**
 * 节点行为工厂。
 */
public class NodeBehaviorFactory {

    /**
     * 条件表达式引擎。
     */
    private final ExpressionEngine expressionEngine;
    /**
     * 任务注册表。
     */
    private final TaskRegistry taskRegistry;
    /**
     * 子流程启动器。
     */
    private final SubProcessLauncher subProcessLauncher;

    public NodeBehaviorFactory() {
        this(
                new SpelExpressionEngine(),
                new InMemoryTaskRegistry(),
                (processId, variables) -> {
                    throw new IllegalStateException("Sub process is not enabled in current engine context: " + processId);
                }
        );
    }

    public NodeBehaviorFactory(ExpressionEngine expressionEngine) {
        this(
                expressionEngine,
                new InMemoryTaskRegistry(),
                (processId, variables) -> {
                    throw new IllegalStateException("Sub process is not enabled in current engine context: " + processId);
                }
        );
    }

    public NodeBehaviorFactory(ExpressionEngine expressionEngine, TaskRegistry taskRegistry) {
        this(
                expressionEngine,
                taskRegistry,
                (processId, variables) -> {
                    throw new IllegalStateException("Sub process is not enabled in current engine context: " + processId);
                }
        );
    }

    public NodeBehaviorFactory(
            ExpressionEngine expressionEngine,
            TaskRegistry taskRegistry,
            SubProcessLauncher subProcessLauncher) {
        this.expressionEngine = expressionEngine;
        this.taskRegistry = taskRegistry;
        this.subProcessLauncher = subProcessLauncher;
    }

    /**
     * 根据定义节点构建行为实现。
     *
     * @param node 定义节点
     * @return 节点行为
     */
    public NodeBehavior create(Node node) {
        if (node.getType() == NodeType.TASK) {
            return new TaskNodeBehavior(taskRegistry);
        }
        if (node.getType() == NodeType.SUB_PROCESS) {
            return new SubProcessNodeBehavior(subProcessLauncher);
        }
        if (node.getType() != NodeType.GATEWAY) {
            return new GenericNodeBehavior();
        }
        GatewayType gatewayType = node.getGatewayType();
        if (gatewayType == GatewayType.EXCLUSIVE) {
            return new ExclusiveGatewayBehavior(expressionEngine);
        }
        if (gatewayType == GatewayType.PARALLEL) {
            return new ParallelGatewayBehavior();
        }
        if (gatewayType == GatewayType.INCLUSIVE) {
            return new InclusiveGatewayBehavior(expressionEngine);
        }
        return new GenericNodeBehavior();
    }
}
