package com.gaibu.flowlab.parser.ast;

import lombok.Data;

/**
 * AST 节点基类
 */
@Data
public abstract class ASTNode {

    /**
     * 节点类型
     */
    private ASTNodeType nodeType;

    public ASTNode(ASTNodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * AST 节点类型
     */
    public enum ASTNodeType {
        FLOWCHART,      // 流程图根节点
        NODE,           // 流程图节点
        EDGE,           // 边
        SUBGRAPH        // 子图
    }
}
