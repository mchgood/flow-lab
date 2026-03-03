package com.gaibu.flowlab.engine.trace;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.CompleteInstruction;
import com.gaibu.flowlab.engine.execution.instruction.FailInstruction;
import com.gaibu.flowlab.engine.execution.instruction.ForkInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.JoinInstruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;
import com.gaibu.flowlab.engine.interceptor.NodeInterceptor;

import java.util.List;

/**
 * 轨迹采集拦截器。
 */
public class TraceNodeInterceptor implements NodeInterceptor {

    /**
     * 轨迹存储。
     */
    private final ExecutionTraceStore traceStore;

    public TraceNodeInterceptor(ExecutionTraceStore traceStore) {
        this.traceStore = traceStore;
    }

    @Override
    public void before(ExecutionContext ctx) {
    }

    @Override
    public void afterSuccess(ExecutionContext ctx, Instruction instruction) {
        TraceStep step = baseStep(ctx, instruction.getClass().getSimpleName(), true);
        step.getToNodeIds().addAll(resolveTargets(instruction));
        traceStore.append(step);
    }

    @Override
    public void afterFailure(ExecutionContext ctx, Throwable ex) {
        TraceStep step = baseStep(ctx, "FAILURE", false);
        step.setErrorMessage(ex.getMessage());
        traceStore.append(step);
    }

    private TraceStep baseStep(ExecutionContext ctx, String instructionType, boolean success) {
        TraceStep step = new TraceStep();
        step.setInstanceId(ctx.instance().getId());
        step.setTokenId(ctx.token().getId().value());
        step.setFromNodeId(ctx.node().getId().value());
        step.setInstructionType(instructionType);
        step.setSuccess(success);
        step.setTimestamp(System.currentTimeMillis());
        return step;
    }

    private List<String> resolveTargets(Instruction instruction) {
        if (instruction instanceof MoveInstruction move) {
            return List.of(move.nextNode().value());
        }
        if (instruction instanceof ForkInstruction fork) {
            return fork.nextNodes().stream().map(nodeId -> nodeId.value()).toList();
        }
        if (instruction instanceof JoinInstruction join) {
            return List.of(join.joinNode().value());
        }
        if (instruction instanceof CompleteInstruction) {
            return List.of();
        }
        if (instruction instanceof FailInstruction) {
            return List.of();
        }
        return List.of();
    }
}
