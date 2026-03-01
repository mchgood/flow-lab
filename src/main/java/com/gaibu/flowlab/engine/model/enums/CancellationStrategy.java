package com.gaibu.flowlab.engine.model.enums;

/**
 * 取消传播策略。
 */
public enum CancellationStrategy {
    PROPAGATE_DOWN("propagate_down", "向下传播"),
    PROPAGATE_UP("propagate_up", "向上传播"),
    ISOLATED("isolated", "隔离取消");

    private final String code;
    private final String desc;

    CancellationStrategy(String code, String desc) {
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

