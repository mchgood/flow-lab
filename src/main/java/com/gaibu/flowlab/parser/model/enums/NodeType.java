package com.gaibu.flowlab.parser.model.enums;

import lombok.Getter;

/**
 * 流程节点类型。
 */
@Getter
public enum NodeType {
    START("start", "开始节点"),
    END("end", "结束节点"),
    TASK("task", "任务节点"),
    SUB_PROCESS("sub_process", "子流程节点"),
    GATEWAY("gateway", "网关节点");

    /**
     * 枚举编码，用于序列化或对外映射。
     */
    private final String code;

    /**
     * 枚举含义描述，用于说明该枚举项表示的业务语义。
     */
    private final String desc;

    NodeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
