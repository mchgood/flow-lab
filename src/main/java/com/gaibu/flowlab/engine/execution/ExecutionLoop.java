package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.graph.ExecutableGraph;
import com.gaibu.flowlab.engine.graph.ExecutableNode;
import com.gaibu.flowlab.engine.interceptor.NodeExecutionTemplate;
import com.gaibu.flowlab.engine.interceptor.NodeInterceptor;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;
import com.gaibu.flowlab.engine.scheduler.Scheduler;

import java.util.List;

/**
 * Token 执行循环。
 */
public class ExecutionLoop {

    /**
     * 节点执行模板。
     */
    private final NodeExecutionTemplate executionTemplate;

    /**
     * 指令处理器。
     */
    private final InstructionHandler instructionHandler;

    public ExecutionLoop(NodeExecutionTemplate executionTemplate, InstructionHandler instructionHandler) {
        this.executionTemplate = executionTemplate;
        this.instructionHandler = instructionHandler;
    }

    /**
     * 执行实例调度循环。
     *
     * @param instance 流程实例
     * @param graph 可执行图
     * @param scheduler 调度器
     * @param interceptors 节点拦截器
     */
    public void run(ProcessInstance instance, ExecutableGraph graph, Scheduler scheduler, List<NodeInterceptor> interceptors) {
        while (scheduler.hasNext() && instance.getStatus() == InstanceStatus.RUNNING) {
            Token token = scheduler.poll();
            if (token == null || token.getStatus() != TokenStatus.ACTIVE) {
                continue;
            }

            ExecutableNode node = graph.getNode(token.getCurrentNode());
            if (node == null) {
                token.setStatus(TokenStatus.FAILED);
                instance.setStatus(InstanceStatus.FAILED);
                instance.removeActiveToken(token);
                continue;
            }

            DefaultExecutionContext context = new DefaultExecutionContext(instance, token, graph);
            var instruction = executionTemplate.execute(node.getBehavior(), context, interceptors);
            instructionHandler.apply(instruction, context, scheduler);
        }
    }
}
