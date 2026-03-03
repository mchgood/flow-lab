package com.gaibu.flowlab.engine.graph;

import com.gaibu.flowlab.engine.runtime.NodeId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 可执行图中的连线模型。
 */
@Getter
@Setter
@NoArgsConstructor
public class ExecutableEdge {

    /**
     * 连线唯一标识。
     */
    private String id;

    /**
     * 源节点 ID。
     */
    private NodeId source;

    /**
     * 目标节点 ID。
     */
    private NodeId target;

    /**
     * 条件表达式（XOR/OR 场景使用）。
     */
    private String conditionExpression;

    /**
     * 是否为默认路径。
     */
    private boolean defaultEdge;

}
