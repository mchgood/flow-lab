package com.gaibu.flowlab.engine.exception;

/**
 * 表达式求值异常。
 */
public class ExpressionEvaluationException extends RuntimeException {

    /**
     * 构造ExpressionEvaluationException实例。
     */
    public ExpressionEvaluationException(String message) {
        super(message);
    }

    /**
     * 构造ExpressionEvaluationException实例。
     */
    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}

