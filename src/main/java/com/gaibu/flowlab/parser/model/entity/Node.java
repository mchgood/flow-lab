package com.gaibu.flowlab.parser.model.entity;

import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流程定义中的节点模型。
 */
@Getter
@Setter
@NoArgsConstructor
public class Node {

    /**
     * 节点唯一标识。
     */
    private String id;

    /**
     * 节点类型。
     */
    private NodeType type;

    /**
     * 网关类型，仅当 type=GATEWAY 时有效。
     */
    private GatewayType gatewayType;

    /**
     * 节点增强元数据（如 timeout/retry/scope 配置）。
     */
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    public Node(String id, NodeType type) {
        this.id = id;
        this.type = type;
    }
}
