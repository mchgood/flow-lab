package com.gaibu.flowlab.parser.api.enums;

/**
 * 分组类型枚举。
 */
public enum GroupType {
    PARALLEL("PARALLEL", "并行分组"),
    SEQUENTIAL("SEQUENTIAL", "串行分组"),
    CUSTOM("CUSTOM", "自定义分组");

    private final String code;
    private final String desc;

    GroupType(String code, String desc) {
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
