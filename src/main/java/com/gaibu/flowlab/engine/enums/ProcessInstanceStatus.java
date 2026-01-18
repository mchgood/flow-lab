package com.gaibu.flowlab.engine.enums;

/**
 * 流程实例状态枚举
 */
public enum ProcessInstanceStatus {
    /**
     * 运行中 - 流程实例正在执行
     */
    RUNNING,

    /**
     * 已暂停 - 流程实例已暂停，等待恢复
     */
    SUSPENDED,

    /**
     * 已完成 - 流程实例正常完成
     */
    COMPLETED,

    /**
     * 已终止 - 流程实例被强制终止
     */
    TERMINATED
}
