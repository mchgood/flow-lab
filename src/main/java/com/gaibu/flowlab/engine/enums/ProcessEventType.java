package com.gaibu.flowlab.engine.enums;

/**
 * 流程事件类型枚举
 */
public enum ProcessEventType {
    /**
     * 流程启动事件
     */
    PROCESS_STARTED,

    /**
     * 流程完成事件
     */
    PROCESS_COMPLETED,

    /**
     * 流程暂停事件
     */
    PROCESS_SUSPENDED,

    /**
     * 流程恢复事件
     */
    PROCESS_RESUMED,

    /**
     * 流程终止事件
     */
    PROCESS_TERMINATED,

    /**
     * 节点开始事件
     */
    NODE_STARTED,

    /**
     * 节点完成事件
     */
    NODE_COMPLETED,

    /**
     * 节点失败事件
     */
    NODE_FAILED
}
