package com.gaibu.flowlab.parser.api.enums;

/**
 * 图方向枚举。
 */
public enum Direction {
    TD("TD", "Top Down，自上而下布局"),
    TB("TB", "Top Bottom，自上而下布局（TD 别名）"),
    LR("LR", "Left Right，自左向右布局"),
    RL("RL", "Right Left，自右向左布局"),
    BT("BT", "Bottom Top，自下向上布局");

    private final String code;
    private final String desc;

    Direction(String code, String desc) {
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
