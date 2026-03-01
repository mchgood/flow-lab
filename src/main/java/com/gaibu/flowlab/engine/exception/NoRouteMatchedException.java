package com.gaibu.flowlab.engine.exception;

/**
 * 无可匹配路由异常。
 */
public class NoRouteMatchedException extends RuntimeException {

    /**
     * 构造NoRouteMatchedException实例。
     */
    public NoRouteMatchedException(String message) {
        super(message);
    }
}

