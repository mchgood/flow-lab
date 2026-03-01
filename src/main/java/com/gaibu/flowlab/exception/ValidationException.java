package com.gaibu.flowlab.exception;

/**
 * 验证异常
 * 当流程图语义验证失败时抛出
 */
public class ValidationException extends RuntimeException {

    private final String nodeId;

    /**
     * 构造ValidationException实例。
     */
    public ValidationException(String message) {
        super(message);
        this.nodeId = null;
    }

    /**
     * 构造ValidationException实例。
     */
    public ValidationException(String message, String nodeId) {
        super(String.format("验证错误 [节点:%s]: %s", nodeId, message));
        this.nodeId = nodeId;
    }

    /**
     * 构造ValidationException实例。
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.nodeId = null;
    }

    /**
     * 获取nodeId。
     * @return nodeId
     */
    public String getNodeId() {
        return nodeId;
    }
}
