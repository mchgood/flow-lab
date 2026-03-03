package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;

/**
 * 节点行为接口。
 */
public interface NodeBehavior {

    /**
     * 处理节点并返回引擎指令。
     *
     * @param context 执行上下文
     * @return 待执行指令
     */
    Instruction handle(ExecutionContext context);
}
