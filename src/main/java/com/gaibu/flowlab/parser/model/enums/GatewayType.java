package com.gaibu.flowlab.parser.model.enums;

import lombok.Getter;

/**
 * 网关语义类型。
 */
@Getter
public enum GatewayType {
    EXCLUSIVE("exclusive", "排他网关"),
    PARALLEL("parallel", "并行网关"),
    INCLUSIVE("inclusive", "包容网关");

    /**
     * 枚举编码，用于序列化或对外映射。
     */
    private final String code;

    /**
     * 枚举含义描述，用于说明该枚举项表示的业务语义。
     */
    private final String desc;

    GatewayType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
