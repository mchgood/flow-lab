package com.gaibu.flowlab.engine.model.enums;

/**
 * 执行单元类型。
 */
public enum ExecutionUnitType {
    ROOT("root", "虚拟根"),
    NODE("node", "普通节点"),
    SUBFLOW("subflow", "子流程节点"),
    PARALLEL_GROUP("parallel_group", "并行组");

    private final String code;
    private final String desc;

    ExecutionUnitType(String code, String desc) {
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

