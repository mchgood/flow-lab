package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 图边模型。
 */
@Getter
@Setter
public class Edge {

    /**
     * 边起点节点 id。
     */
    private String from;
    /**
     * 边终点节点 id。
     */
    private String to;
    /**
     * 边标签（条件表达式或空字符串）。
     */
    private String label;

    /**
     * 构造Edge实例。
     */
    public Edge() {
    }

    /**
     * 构造Edge实例。
     */
    public Edge(String from, String to, String label) {
        this.from = from;
        this.to = to;
        this.label = label == null ? "" : label;
    }

}
