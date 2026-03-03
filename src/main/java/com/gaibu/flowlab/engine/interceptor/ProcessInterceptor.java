package com.gaibu.flowlab.engine.interceptor;

import com.gaibu.flowlab.engine.runtime.ProcessInstance;

/**
 * 流程级生命周期拦截器。
 */
public interface ProcessInterceptor {

    /**
     * 流程启动前回调。
     *
     * @param instance 流程实例
     */
    void beforeStart(ProcessInstance instance);

    /**
     * 流程完成后回调。
     *
     * @param instance 流程实例
     */
    void onCompleted(ProcessInstance instance);

    /**
     * 流程失败后回调。
     *
     * @param instance 流程实例
     * @param ex 失败异常
     */
    void onFailed(ProcessInstance instance, Throwable ex);
}
