package com.gaibu.flowlab.parser.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析后的流程定义聚合对象。
 */
@Getter
@Setter
public class ProcessDefinition {

    /**
     * 流程定义唯一标识。
     */
    private String id;

    /**
     * 节点表（key=nodeId）。
     */
    private final Map<String, Node> nodes = new LinkedHashMap<>();

    /**
     * 连线表（key=edgeId）。
     */
    private final Map<String, Edge> edges = new LinkedHashMap<>();

    /**
     * 出边索引（key=nodeId，value=按 DSL 顺序排列的出边列表）。
     */
    private final Map<String, List<Edge>> outgoingIndex = new LinkedHashMap<>();

    /**
     * 入边索引（key=nodeId，value=入边列表）。
     */
    private final Map<String, List<Edge>> incomingIndex = new LinkedHashMap<>();
}
