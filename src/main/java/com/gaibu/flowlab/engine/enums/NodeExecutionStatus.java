package com.gaibu.flowlab.engine.enums;

/**
 * 节点执行状态枚举
 */
public enum NodeExecutionStatus {
    /**
     * 执行成功
     */
    SUCCESS,

    /**
     * 执行失败
     */
    FAILED,

    /**
     * 等待中 - 节点正在等待外部输入或条件满足
     */
    WAITING
}
