package com.gaibu.flowlab.parser.ast;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程图 AST 根节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowchartAST extends ASTNode {

    /**
     * 流程图方向
     */
    private String direction;

    /**
     * 所有语句（节点定义、边定义、子图等）
     */
    private List<ASTNode> statements;

    @Builder
    public FlowchartAST(String direction, List<ASTNode> statements) {
        super(ASTNodeType.FLOWCHART);
        this.direction = direction;
        this.statements = statements != null ? statements : new ArrayList<>();
    }

    public void addStatement(ASTNode statement) {
        if (statements == null) {
            statements = new ArrayList<>();
        }
        statements.add(statement);
    }
}
