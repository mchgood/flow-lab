package com.gaibu.flowlab.engine.exception;

/**
 * Workflow 执行异常。
 */
public class WorkflowExecutionException extends RuntimeException {

    /**
     * 构造WorkflowExecutionException实例。
     */
    public WorkflowExecutionException(String message) {
        super(message);
    }

    /**
     * 构造WorkflowExecutionException实例。
     */
    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

