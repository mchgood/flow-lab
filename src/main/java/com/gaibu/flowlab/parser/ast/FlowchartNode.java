package com.gaibu.flowlab.parser.ast;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程图节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowchartNode extends ASTNode {

    /**
     * 节点 ID
     */
    private String id;

    /**
     * 节点标签
     */
    private String label;

    /**
     * 节点形状
     */
    private NodeShape shape;

    @Builder
    public FlowchartNode(String id, String label, NodeShape shape) {
        super(ASTNodeType.NODE);
        this.id = id;
        this.label = label;
        this.shape = shape;
    }
}
