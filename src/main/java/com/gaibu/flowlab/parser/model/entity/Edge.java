package com.gaibu.flowlab.parser.model.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 流程定义中的连线模型。
 */
@Getter
@Setter
public class Edge {

    /**
     * 连线唯一标识。
     */
    private String id;

    /**
     * 源节点 ID。
     */
    private String sourceRef;

    /**
     * 目标节点 ID。
     */
    private String targetRef;

    /**
     * 条件表达式（XOR/OR 等场景使用）。
     */
    private String conditionExpression;

    /**
     * 是否为 default 出边。
     */
    private boolean defaultEdge;

    /**
     * 预编译表达式对象，供后续表达式引擎使用。
     */
    private Object compiledExpression;
}
