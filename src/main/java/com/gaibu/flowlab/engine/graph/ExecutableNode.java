package com.gaibu.flowlab.engine.graph;

import com.gaibu.flowlab.engine.behavior.NodeBehavior;
import com.gaibu.flowlab.engine.runtime.NodeId;
import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 可执行图中的节点模型。
 */
@Getter
@Setter
@NoArgsConstructor
public class ExecutableNode {

    /**
     * 节点唯一标识。
     */
    private NodeId id;

    /**
     * 节点类型。
     */
    private NodeType type;

    /**
     * 网关类型，仅 type=GATEWAY 时有效。
     */
    private GatewayType gatewayType;

    /**
     * 节点行为处理器。
     */
    private NodeBehavior behavior;

    /**
     * 节点元数据。
     */
    private final Map<String, Object> metadata = new LinkedHashMap<>();
}
