package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.graph.ExecutableGraph;
import com.gaibu.flowlab.engine.graph.ExecutableNode;
import com.gaibu.flowlab.engine.interceptor.NodeInterceptorChain;
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
    private static final String ERROR_KEY = "process.error";
    private static final String ERROR_MESSAGE_KEY = "process.error.message";
    private static final String ERROR_TYPE_KEY = "process.error.type";
    private static final String ERROR_NODE_KEY = "process.error.nodeId";

    private final NodeInterceptorChain interceptorChain;

    /**
     * 指令处理器。
     */
    private final InstructionHandler instructionHandler;

    public ExecutionLoop(InstructionHandler instructionHandler) {
        this.interceptorChain = new NodeInterceptorChain();
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
            try {
                interceptorChain.before(interceptors, context);
                var instruction = node.getBehavior().handle(context);
                interceptorChain.afterSuccess(interceptors, context, instruction);
                instructionHandler.apply(instruction, context, scheduler);
            } catch (Exception ex) {
                interceptorChain.afterFailure(interceptors, context, ex);
                token.setStatus(TokenStatus.FAILED);
                instance.removeActiveToken(token);
                instance.setStatus(InstanceStatus.FAILED);
                instance.setFailureCause(ex);
                if (instance.getVariables() != null) {
                    instance.getVariables().put(ERROR_KEY, ex);
                    instance.getVariables().put(ERROR_MESSAGE_KEY, ex.getMessage());
                    instance.getVariables().put(ERROR_TYPE_KEY, ex.getClass().getName());
                    instance.getVariables().put(ERROR_NODE_KEY, context.node().getId().value());
                }
                return;
            }
        }
    }
}
