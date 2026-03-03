package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;

/**
 * SUB_PROCESS 节点行为，执行子流程并在完成后继续父流程。
 */
public class SubProcessNodeBehavior implements NodeBehavior {

    /**
     * 子流程启动器。
     */
    private final SubProcessLauncher subProcessLauncher;

    /**
     * 子流程完成后的通用路由行为。
     */
    private final GenericNodeBehavior genericNodeBehavior = new GenericNodeBehavior();

    public SubProcessNodeBehavior(SubProcessLauncher subProcessLauncher) {
        this.subProcessLauncher = subProcessLauncher;
    }

    @Override
    public Instruction handle(ExecutionContext context) {
        String nodeId = context.node().getId().value();
        Object configured = context.node().getMetadata().get("subProcessId");
        if (configured == null) {
            throw new IllegalStateException("Sub process id is required via %% @node annotation at node: " + nodeId);
        }
        String subProcessId = String.valueOf(configured).trim();
        if (subProcessId.isBlank()) {
            throw new IllegalStateException("Sub process id is blank at node: " + nodeId);
        }
        subProcessLauncher.launch(subProcessId, context.variables());

        return genericNodeBehavior.handle(context);
    }
}
