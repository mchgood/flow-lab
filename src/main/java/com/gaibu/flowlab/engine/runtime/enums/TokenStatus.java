package com.gaibu.flowlab.engine.runtime.enums;

import lombok.Getter;

/**
 * Token 运行状态枚举。
 */
@Getter
public enum TokenStatus {
    ACTIVE("active", "活跃可调度"),
    COMPLETED("completed", "执行完成"),
    FAILED("failed", "执行失败");

    /**
     * 枚举编码。
     */
    private final String code;

    /**
     * 枚举语义描述。
     */
    private final String desc;

    TokenStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
