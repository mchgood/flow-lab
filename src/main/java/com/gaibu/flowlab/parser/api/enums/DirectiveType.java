package com.gaibu.flowlab.parser.api.enums;

/**
 * 指令类型枚举。
 */
public enum DirectiveType {
    TIMEOUT("TIMEOUT", "节点超时控制指令"),
    PARALLEL_GROUP("PARALLEL_GROUP", "节点并行分组归属指令"),
    PARALLEL("PARALLEL", "并行组执行策略指令"),
    RETRY("RETRY", "节点重试策略指令"),
    SUBFLOW("SUBFLOW", "子流程引用指令"),
    CONDITION("CONDITION", "条件路由指令"),
    CUSTOM("CUSTOM", "自定义扩展指令");

    private final String code;
    private final String desc;

    DirectiveType(String code, String desc) {
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
