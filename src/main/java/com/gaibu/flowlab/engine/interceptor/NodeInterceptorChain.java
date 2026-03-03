package com.gaibu.flowlab.engine.interceptor;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;

import java.util.List;

/**
 * 节点拦截器分发器，封装拦截器循环调用细节。
 */
public class NodeInterceptorChain {

    /**
     * 触发 before 回调。
     *
     * @param interceptors 节点拦截器
     * @param ctx 执行上下文
     */
    public void before(List<NodeInterceptor> interceptors, ExecutionContext ctx) {
        for (NodeInterceptor interceptor : interceptors) {
            interceptor.before(ctx);
        }
    }

    /**
     * 触发 afterSuccess 回调。
     *
     * @param interceptors 节点拦截器
     * @param ctx 执行上下文
     * @param instruction 节点返回指令
     */
    public void afterSuccess(List<NodeInterceptor> interceptors, ExecutionContext ctx, Instruction instruction) {
        for (NodeInterceptor interceptor : interceptors) {
            interceptor.afterSuccess(ctx, instruction);
        }
    }

    /**
     * 触发 afterFailure 回调。
     *
     * @param interceptors 节点拦截器
     * @param ctx 执行上下文
     * @param ex 异常
     */
    public void afterFailure(List<NodeInterceptor> interceptors, ExecutionContext ctx, Throwable ex) {
        for (NodeInterceptor interceptor : interceptors) {
            interceptor.afterFailure(ctx, ex);
        }
    }
}
