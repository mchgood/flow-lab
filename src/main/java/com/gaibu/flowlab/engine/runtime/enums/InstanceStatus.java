package com.gaibu.flowlab.engine.runtime.enums;

import lombok.Getter;

/**
 * 流程实例状态枚举。
 */
@Getter
public enum InstanceStatus {
    RUNNING("running", "运行中"),
    INTERRUPTED("interrupted", "被任务主动中断"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    /**
     * 枚举编码。
     */
    private final String code;

    /**
     * 枚举语义描述。
     */
    private final String desc;

    InstanceStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
