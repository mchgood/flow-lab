package com.gaibu.flowlab.transformer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程图模型（点线结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowGraph {

    /**
     * 节点列表
     */
    @Builder.Default
    private List<Node> nodes = new ArrayList<>();

    /**
     * 边列表
     */
    @Builder.Default
    private List<Edge> edges = new ArrayList<>();

    /**
     * 添加节点
     */
    public void addNode(Node node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        nodes.add(node);
    }

    /**
     * 添加边
     */
    public void addEdge(Edge edge) {
        if (edges == null) {
            edges = new ArrayList<>();
        }
        edges.add(edge);
    }
}
