package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.execution.instruction.CompleteInstruction;
import com.gaibu.flowlab.engine.execution.instruction.FailInstruction;
import com.gaibu.flowlab.engine.execution.instruction.ForkInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.execution.instruction.JoinInstruction;
import com.gaibu.flowlab.engine.execution.instruction.MoveInstruction;
import com.gaibu.flowlab.engine.graph.ExecutableNode;
import com.gaibu.flowlab.engine.runtime.NodeId;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.ScopeId;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.runtime.enums.ScopeStatus;
import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;
import com.gaibu.flowlab.engine.scheduler.Scheduler;
import com.gaibu.flowlab.engine.scope.ScopeRuntime;
import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;

/**
 * 指令处理器，作为运行时状态唯一修改点。
 */
public class InstructionHandler {

    /**
     * Token 工厂。
     */
    private final TokenFactory tokenFactory;

    public InstructionHandler(TokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    /**
     * 应用节点指令。
     *
     * @param instruction 节点指令
     * @param ctx 上下文
     * @param scheduler 调度器
     */
    public void apply(Instruction instruction, DefaultExecutionContext ctx, Scheduler scheduler) {
        if (instruction instanceof MoveInstruction move) {
            move(ctx, move, scheduler);
            return;
        }
        if (instruction instanceof ForkInstruction fork) {
            fork(ctx, fork, scheduler);
            return;
        }
        if (instruction instanceof JoinInstruction join) {
            join(ctx, join, scheduler);
            return;
        }
        if (instruction instanceof CompleteInstruction) {
            complete(ctx);
            return;
        }
        if (instruction instanceof FailInstruction fail) {
            fail(ctx, fail);
            return;
        }
        throw new IllegalStateException("Unsupported instruction type: " + instruction.getClass().getName());
    }

    private void move(DefaultExecutionContext ctx, MoveInstruction move, Scheduler scheduler) {
        Token token = ctx.token();
        if (isJoinGateway(ctx, move.nextNode())) {
            collectJoinArrival(ctx, token, move.nextNode(), scheduler);
            return;
        }
        token.setCurrentNode(move.nextNode());
        token.setStatus(TokenStatus.ACTIVE);
        scheduler.schedule(token);
    }

    private void fork(DefaultExecutionContext ctx, ForkInstruction fork, Scheduler scheduler) {
        Token current = ctx.token();
        ProcessInstance instance = ctx.instance();

        current.setStatus(TokenStatus.COMPLETED);
        instance.removeActiveToken(current);

        for (var nextNode : fork.nextNodes()) {
            Token child = tokenFactory.create(nextNode, current.getExecution());
            instance.addToken(child);
            scheduler.schedule(child);
        }
    }

    private void join(DefaultExecutionContext ctx, JoinInstruction join, Scheduler scheduler) {
        ProcessInstance instance = ctx.instance();
        Token token = ctx.token();
        int outgoingSize = ctx.outgoing().size();
        if (outgoingSize == 0) {
            complete(ctx);
            return;
        }
        if (outgoingSize == 1) {
            token.setCurrentNode(ctx.outgoing().get(0).getTarget());
            token.setStatus(TokenStatus.ACTIVE);
            scheduler.schedule(token);
            return;
        }

        for (var edge : ctx.outgoing()) {
            Token child = tokenFactory.create(edge.getTarget(), token.getExecution());
            instance.addToken(child);
            scheduler.schedule(child);
        }
    }

    private void complete(DefaultExecutionContext ctx) {
        Token token = ctx.token();
        ProcessInstance instance = ctx.instance();
        token.setStatus(TokenStatus.COMPLETED);
        instance.removeActiveToken(token);
    }

    private void fail(DefaultExecutionContext ctx, FailInstruction fail) {
        Token token = ctx.token();
        ProcessInstance instance = ctx.instance();
        token.setStatus(TokenStatus.FAILED);
        instance.setStatus(InstanceStatus.FAILED);
        instance.removeActiveToken(token);
    }

    private boolean isJoinGateway(DefaultExecutionContext ctx, NodeId target) {
        ExecutableNode targetNode = ctx.graph().getNode(target);
        if (targetNode == null || targetNode.getType() != NodeType.GATEWAY) {
            return false;
        }
        GatewayType gatewayType = targetNode.getGatewayType();
        if (gatewayType != GatewayType.PARALLEL && gatewayType != GatewayType.INCLUSIVE) {
            return false;
        }
        return ctx.graph().incoming(target).size() > 1;
    }

    private void collectJoinArrival(DefaultExecutionContext ctx, Token token, NodeId joinNode, Scheduler scheduler) {
        ProcessInstance instance = ctx.instance();
        String scopeKey = "JOIN:" + joinNode.value();
        ScopeRuntime scope = instance.getScopes().computeIfAbsent(scopeKey, key -> {
            ScopeRuntime runtime = new ScopeRuntime();
            runtime.setId(new ScopeId("SC-" + key));
            runtime.setJoinNodeId(joinNode);
            runtime.setExpectedTokenCount(Math.max(1, ctx.graph().incoming(joinNode).size()));
            runtime.setStatus(ScopeStatus.ACTIVE);
            return runtime;
        });

        scope.getArrivedTokens().add(token.getId());
        token.setStatus(TokenStatus.COMPLETED);
        instance.removeActiveToken(token);

        if (scope.getArrivedTokens().size() < scope.getExpectedTokenCount()) {
            return;
        }

        scope.setStatus(ScopeStatus.COMPLETED);
        instance.getScopes().remove(scopeKey);

        token.setCurrentNode(joinNode);
        token.setStatus(TokenStatus.ACTIVE);
        instance.getActiveTokens().add(token);
        scheduler.schedule(token);
    }
}
