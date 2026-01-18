package com.gaibu.flowlab.engine.expression;

/**
 * 表达式评估异常 - 当表达式评估失败时抛出
 */
public class ExpressionEvaluationException extends RuntimeException {
    public ExpressionEvaluationException(String message) {
        super(message);
    }

    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
