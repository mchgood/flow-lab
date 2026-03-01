package com.gaibu.flowlab.engine.model.enums;

/**
 * 执行状态。
 */
public enum ExecutionState {
    CREATED("created", "已创建"),
    PENDING("pending", "待执行"),
    RUNNING("running", "执行中"),
    SUCCESS("success", "执行成功"),
    FAILED("failed", "执行失败"),
    CANCELLED("cancelled", "已取消"),
    TIMEOUT("timeout", "执行超时");

    private final String code;
    private final String desc;

    ExecutionState(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取code。
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取desc。
     * @return desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 判断terminal。
     * @return true 表示terminal
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }
}

