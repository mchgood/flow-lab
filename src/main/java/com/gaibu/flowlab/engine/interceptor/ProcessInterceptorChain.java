package com.gaibu.flowlab.engine.interceptor;

import com.gaibu.flowlab.engine.runtime.ProcessInstance;

import java.util.List;

/**
 * 流程拦截器分发器，封装拦截器循环调用细节。
 */
public class ProcessInterceptorChain {

    /**
     * 触发 beforeStart 回调。
     *
     * @param interceptors 流程拦截器
     * @param instance 流程实例
     */
    public void beforeStart(List<ProcessInterceptor> interceptors, ProcessInstance instance) {
        for (ProcessInterceptor interceptor : interceptors) {
            interceptor.beforeStart(instance);
        }
    }

    /**
     * 触发 onCompleted 回调。
     *
     * @param interceptors 流程拦截器
     * @param instance 流程实例
     */
    public void onCompleted(List<ProcessInterceptor> interceptors, ProcessInstance instance) {
        for (ProcessInterceptor interceptor : interceptors) {
            interceptor.onCompleted(instance);
        }
    }

    /**
     * 触发 onFailed 回调。
     *
     * @param interceptors 流程拦截器
     * @param instance 流程实例
     * @param ex 异常
     */
    public void onFailed(List<ProcessInterceptor> interceptors, ProcessInstance instance, Throwable ex) {
        for (ProcessInterceptor interceptor : interceptors) {
            interceptor.onFailed(instance, ex);
        }
    }
}
