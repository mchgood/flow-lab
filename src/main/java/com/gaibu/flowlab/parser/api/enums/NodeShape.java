package com.gaibu.flowlab.parser.api.enums;

/**
 * 节点形状枚举（仅支持文档定义子集）。
 */
public enum NodeShape {
    RECTANGLE("RECTANGLE", "矩形节点，对应 ID[text]"),
    DIAMOND("DIAMOND", "菱形节点，对应 ID{text}"),
    SUBPROCESS("SUBPROCESS", "子流程节点，对应 ID[[text]]"),
    DEFAULT("DEFAULT", "默认节点形状（兜底）");

    private final String code;
    private final String desc;

    NodeShape(String code, String desc) {
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
