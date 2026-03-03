package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.CompleteInstruction;
import com.gaibu.flowlab.engine.execution.instruction.ForkInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.JoinInstruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;

import java.util.stream.Collectors;

/**
 * 并行网关行为。
 */
public class ParallelGatewayBehavior implements NodeBehavior {

    @Override
    public Instruction handle(ExecutionContext context) {
        int incoming = context.incoming().size();
        int outgoing = context.outgoing().size();
        if (incoming > 1) {
            return new JoinInstruction(context.node().getId());
        }
        if (outgoing == 0) {
            return new CompleteInstruction();
        }
        if (outgoing == 1) {
            return new MoveInstruction(context.outgoing().get(0).getTarget());
        }
        return new ForkInstruction(context.outgoing().stream().map(edge -> edge.getTarget()).collect(Collectors.toList()));
    }
}
