package com.gaibu.flowlab.engine.execution.instruction;

/**
 * 引擎指令顶层接口。
 */
public sealed interface Instruction permits MoveInstruction,
        ForkInstruction,
        JoinInstruction,
        CompleteInstruction,
        FailInstruction {
}
