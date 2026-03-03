package com.gaibu.flowlab.engine.interceptor;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;

/**
 * 节点执行拦截器。
 */
public interface NodeInterceptor {

    /**
     * 节点执行前回调。
     *
     * @param ctx 执行上下文
     */
    void before(ExecutionContext ctx);

    /**
     * 节点执行成功后回调。
     *
     * @param ctx 执行上下文
     * @param instruction 节点返回指令
     */
    void afterSuccess(ExecutionContext ctx, Instruction instruction);

    /**
     * 节点执行失败后回调。
     *
     * @param ctx 执行上下文
     * @param ex 失败异常
     */
    void afterFailure(ExecutionContext ctx, Throwable ex);
}
