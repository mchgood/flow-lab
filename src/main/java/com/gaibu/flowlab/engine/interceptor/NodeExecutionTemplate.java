package com.gaibu.flowlab.engine.interceptor;

import com.gaibu.flowlab.engine.behavior.NodeBehavior;
import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;

import java.util.List;

/**
 * 节点执行模板，统一编排拦截器生命周期。
 */
public class NodeExecutionTemplate {

    /**
     * 执行节点行为。
     *
     * @param behavior 节点行为
     * @param ctx 执行上下文
     * @param interceptors 命中拦截器
     * @return 节点指令
     */
    public Instruction execute(NodeBehavior behavior, ExecutionContext ctx, List<NodeInterceptor> interceptors) {
        for (NodeInterceptor interceptor : interceptors) {
            interceptor.before(ctx);
        }
        try {
            Instruction instruction = behavior.handle(ctx);
            for (NodeInterceptor interceptor : interceptors) {
                interceptor.afterSuccess(ctx, instruction);
            }
            return instruction;
        } catch (Exception ex) {
            for (NodeInterceptor interceptor : interceptors) {
                interceptor.afterFailure(ctx, ex);
            }
            throw ex;
        }
    }
}
