package com.gaibu.flowlab.parser.rule.enums;

import lombok.Getter;

/**
 * 节点 token 类型。
 */
@Getter
public enum NodeTokenKind {
    SUB_PROCESS("sub_process", "子流程节点 token"),
    TASK("task", "任务节点 token"),
    GATEWAY("gateway", "网关节点 token"),
    ROUND("round", "圆形节点 token（Start/End）"),
    PLAIN("plain", "纯节点 ID token");

    /**
     * 枚举编码，用于序列化或对外映射。
     */
    private final String code;

    /**
     * 枚举含义描述，用于说明该枚举项表示的规则语义。
     */
    private final String desc;

    NodeTokenKind(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
