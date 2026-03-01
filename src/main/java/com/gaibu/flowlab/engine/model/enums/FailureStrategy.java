package com.gaibu.flowlab.engine.model.enums;

/**
 * 失败传播策略。
 */
public enum FailureStrategy {
    FAIL_FAST("fail_fast", "失败立即终止"),
    CONTINUE("continue", "失败后继续执行");

    private final String code;
    private final String desc;

    FailureStrategy(String code, String desc) {
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
}

