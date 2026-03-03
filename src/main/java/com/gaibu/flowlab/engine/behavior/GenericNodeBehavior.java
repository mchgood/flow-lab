package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.CompleteInstruction;
import com.gaibu.flowlab.engine.execution.instruction.ForkInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;
import com.gaibu.flowlab.parser.model.enums.NodeType;

import java.util.stream.Collectors;

/**
 * 通用节点行为（START/TASK/SUB_PROCESS/END）。
 */
public class GenericNodeBehavior implements NodeBehavior {

    @Override
    public Instruction handle(ExecutionContext context) {
        if (context.node().getType() == NodeType.END) {
            return new CompleteInstruction();
        }
        int size = context.outgoing().size();
        if (size == 0) {
            return new CompleteInstruction();
        }
        if (size == 1) {
            return new MoveInstruction(context.outgoing().get(0).getTarget());
        }
        return new ForkInstruction(context.outgoing().stream().map(edge -> edge.getTarget()).collect(Collectors.toList()));
    }
}
