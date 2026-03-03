package com.gaibu.flowlab.engine.runtime.enums;

import lombok.Getter;

/**
 * Scope 运行状态枚举。
 */
@Getter
public enum ScopeStatus {
    ACTIVE("active", "活跃"),
    COMPLETED("completed", "完成"),
    TIMED_OUT("timed_out", "超时"),
    CANCELLED("cancelled", "取消"),
    FAILED("failed", "失败");

    /**
     * 枚举编码。
     */
    private final String code;

    /**
     * 枚举语义描述。
     */
    private final String desc;

    ScopeStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
