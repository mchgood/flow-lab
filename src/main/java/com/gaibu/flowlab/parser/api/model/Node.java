package com.gaibu.flowlab.parser.api.model;

import com.gaibu.flowlab.parser.api.enums.NodeShape;
import lombok.Getter;
import lombok.Setter;

/**
 * 图节点模型。
 */
@Getter
@Setter
public class Node {

    /**
     * 节点 id。
     */
    private String id;
    /**
     * 节点显示文本。
     */
    private String text;
    /**
     * 节点形状（矩形/菱形/子流程）。
     */
    private NodeShape shape;

    /**
     * 构造Node实例。
     */
    public Node() {
    }

    /**
     * 构造Node实例。
     */
    public Node(String id, String text, NodeShape shape) {
        this.id = id;
        this.text = text;
        this.shape = shape;
    }

}
