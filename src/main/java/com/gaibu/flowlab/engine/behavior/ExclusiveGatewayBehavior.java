package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.FailInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;
import com.gaibu.flowlab.engine.graph.ExecutableEdge;

/**
 * 排他网关行为。
 */
public class ExclusiveGatewayBehavior extends AbstractGatewayBehavior {

    public ExclusiveGatewayBehavior(ExpressionEngine expressionEngine) {
        super(expressionEngine);
    }

    @Override
    public Instruction handle(ExecutionContext context) {
        ExecutableEdge defaultEdge = null;
        for (ExecutableEdge edge : context.outgoing()) {
            if (edge.isDefaultEdge()) {
                defaultEdge = edge;
                continue;
            }
            if (evaluateCondition(edge.getConditionExpression(), context)) {
                return new MoveInstruction(edge.getTarget());
            }
        }
        if (defaultEdge != null) {
            return new MoveInstruction(defaultEdge.getTarget());
        }
        return new FailInstruction("No matching route for exclusive gateway: " + context.node().getId().value());
    }
}
