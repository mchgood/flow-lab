package com.gaibu.flowlab.parser.syntax;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.ast.ASTNode;
import com.gaibu.flowlab.parser.ast.EdgeNode;
import com.gaibu.flowlab.parser.ast.FlowchartAST;
import com.gaibu.flowlab.parser.ast.FlowchartNode;
import com.gaibu.flowlab.parser.ast.NodeShape;
import com.gaibu.flowlab.parser.ast.SubgraphNode;
import com.gaibu.flowlab.parser.lexer.MermaidLexer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MermaidParser 语法分析器测试
 */
class MermaidParserTest {

    @Test
    void testParseDefaultDirection() {
        String mermaid = """
                flowchart
                    A[开始] --> B[结束]
                """;

        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());
        FlowchartAST ast = parser.parse();

        assertThat(ast.getDirection()).isEqualTo("TD");

        Map<String, FlowchartNode> registry = parser.getNodeRegistry();
        assertThat(registry).containsKeys("A", "B");
        assertThat(registry.get("A").getShape()).isEqualTo(NodeShape.RECTANGLE);
        assertThat(registry.get("B").getLabel()).isEqualTo("结束");
    }

    @Test
    void testParseSubgraphTitleAndStatements() {
        String mermaid = """
                flowchart TD
                    A[开始]
                    subgraph 子流程1
                        B[步骤1] --> C[步骤2]
                    end
                    A --> B
                """;

        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());
        FlowchartAST ast = parser.parse();

        SubgraphNode subgraph = ast.getStatements().stream()
                .filter(statement -> statement instanceof SubgraphNode)
                .map(statement -> (SubgraphNode) statement)
                .findFirst()
                .orElse(null);

        assertThat(subgraph).isNotNull();
        assertThat(subgraph.getTitle()).isEqualTo("子流程1");
        assertThat(subgraph.getStatements()).isNotEmpty();

        Map<String, FlowchartNode> registry = parser.getNodeRegistry();
        assertThat(registry).containsKeys("A", "B", "C");
    }

    @Test
    void testParseEdgeLabelExpression() {
        String mermaid = """
                flowchart TD
                    A((开始)) --> B{判断}
                    B -->|#amount > 1000| C[高额审批]
                """;

        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());
        FlowchartAST ast = parser.parse();

        EdgeNode edge = findFirstEdge(ast.getStatements(), "B", "C");
        assertThat(edge).isNotNull();
        // 普通标签保持为展示标签，不作为条件
        assertThat(edge.getLabel()).isEqualTo("#amount>1000");
        assertThat(edge.getCondition()).isEmpty();
    }

    @Test
    void testParseEdgeConditionPrefix() {
        String mermaid = """
                flowchart TD
                    A((开始)) --> B{判断}
                    B -->|?#amount > 1000| C[高额审批]
                """;

        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());
        FlowchartAST ast = parser.parse();

        EdgeNode edge = findFirstEdge(ast.getStatements(), "B", "C");
        assertThat(edge).isNotNull();
        assertThat(edge.getLabel()).isEqualTo("");
        assertThat(edge.getCondition()).isEqualTo("#amount>1000");
    }

    @Test
    void testImplicitNodeCreatedFromEdge() {
        String mermaid = """
                flowchart TD
                    A --> B
                """;

        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());
        parser.parse();

        Map<String, FlowchartNode> registry = parser.getNodeRegistry();
        assertThat(registry).containsKeys("A", "B");
        assertThat(registry.get("A").getShape()).isEqualTo(NodeShape.RECTANGLE);
        assertThat(registry.get("B").getShape()).isEqualTo(NodeShape.RECTANGLE);
    }

    @Test
    void testParseRequiresFlowchartOrGraphKeyword() {
        String mermaid = "A[开始] --> B[结束]";
        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("flowchart");
    }

    @Test
    void testSubgraphMissingEndThrows() {
        String mermaid = """
                flowchart TD
                    subgraph 子流程
                        A[开始]
                """;
        MermaidParser parser = new MermaidParser(new MermaidLexer(mermaid).tokenize());

        assertThatThrownBy(parser::parse)
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("end");
    }

    private EdgeNode findFirstEdge(List<ASTNode> statements, String from, String to) {
        return statements.stream()
                .filter(statement -> statement instanceof EdgeNode)
                .map(statement -> (EdgeNode) statement)
                .filter(edge -> from.equals(edge.getFromId()) && to.equals(edge.getToId()))
                .findFirst()
                .orElse(null);
    }
}
