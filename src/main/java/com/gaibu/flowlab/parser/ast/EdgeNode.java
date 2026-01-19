package com.gaibu.flowlab.parser.ast;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 边节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EdgeNode extends ASTNode {

    /**
     * 起始节点 ID
     */
    private String fromId;

    /**
     * 目标节点 ID
     */
    private String toId;

    /**
     * 边标签
     */
    private String label;

    /**
     * 条件表达式（可选，SpEL）
     */
    private String condition;

    @Builder
    public EdgeNode(String fromId, String toId, String label, String condition) {
        super(ASTNodeType.EDGE);
        this.fromId = fromId;
        this.toId = toId;
        this.label = label != null ? label : "";
        this.condition = condition != null ? condition : "";
    }
}
