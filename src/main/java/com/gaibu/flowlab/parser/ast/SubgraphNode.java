package com.gaibu.flowlab.parser.ast;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubgraphNode extends ASTNode {

    /**
     * 子图标题
     */
    private String title;

    /**
     * 子图内的语句列表
     */
    private List<ASTNode> statements;

    @Builder
    public SubgraphNode(String title, List<ASTNode> statements) {
        super(ASTNodeType.SUBGRAPH);
        this.title = title;
        this.statements = statements != null ? statements : new ArrayList<>();
    }

    public void addStatement(ASTNode statement) {
        if (statements == null) {
            statements = new ArrayList<>();
        }
        statements.add(statement);
    }
}
