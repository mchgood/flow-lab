package com.gaibu.flowlab.engine.model.enums;

/**
 * 并行模式。
 */
public enum ParallelMode {
    ANY("any", "任一成功即完成"),
    ALL("all", "全部成功才完成");

    private final String code;
    private final String desc;

    ParallelMode(String code, String desc) {
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
     * 执行from并返回结果。
     * @return 执行结果
     */
    public static ParallelMode from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        return "ANY".equalsIgnoreCase(raw.trim()) ? ANY : ALL;
    }
}

