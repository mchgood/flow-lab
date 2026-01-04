package com.gaibu.flowlab.exception;

/**
 * 验证异常
 * 当流程图语义验证失败时抛出
 */
public class ValidationException extends RuntimeException {

    private final String nodeId;

    public ValidationException(String message) {
        super(message);
        this.nodeId = null;
    }

    public ValidationException(String message, String nodeId) {
        super(String.format("验证错误 [节点:%s]: %s", nodeId, message));
        this.nodeId = nodeId;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.nodeId = null;
    }

    public String getNodeId() {
        return nodeId;
    }
}
