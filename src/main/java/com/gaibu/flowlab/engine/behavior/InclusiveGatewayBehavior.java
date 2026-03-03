package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.FailInstruction;
import com.gaibu.flowlab.engine.execution.instruction.ForkInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.JoinInstruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;
import com.gaibu.flowlab.engine.graph.ExecutableEdge;

import java.util.ArrayList;
import java.util.List;

/**
 * 包容网关行为。
 */
public class InclusiveGatewayBehavior extends AbstractGatewayBehavior {

    public InclusiveGatewayBehavior(ExpressionEngine expressionEngine) {
        super(expressionEngine);
    }

    @Override
    public Instruction handle(ExecutionContext context) {
        if (context.incoming().size() > 1) {
            return new JoinInstruction(context.node().getId());
        }

        List<ExecutableEdge> selected = new ArrayList<>();
        ExecutableEdge defaultEdge = null;
        for (ExecutableEdge edge : context.outgoing()) {
            if (edge.isDefaultEdge()) {
                defaultEdge = edge;
                continue;
            }
            if (evaluateCondition(edge.getConditionExpression(), context)) {
                selected.add(edge);
            }
        }

        if (selected.isEmpty() && defaultEdge != null) {
            return new MoveInstruction(defaultEdge.getTarget());
        }
        if (selected.size() == 1) {
            return new MoveInstruction(selected.get(0).getTarget());
        }
        if (!selected.isEmpty()) {
            return new ForkInstruction(selected.stream().map(ExecutableEdge::getTarget).toList());
        }
        return new FailInstruction("No matching route for inclusive gateway: " + context.node().getId().value());
    }
}
