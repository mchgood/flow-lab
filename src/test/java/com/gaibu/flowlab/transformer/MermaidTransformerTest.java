package com.gaibu.flowlab.transformer;

import com.gaibu.flowlab.parser.ast.ASTNode;
import com.gaibu.flowlab.parser.ast.EdgeNode;
import com.gaibu.flowlab.parser.ast.FlowchartAST;
import com.gaibu.flowlab.parser.ast.FlowchartNode;
import com.gaibu.flowlab.parser.ast.NodeShape;
import com.gaibu.flowlab.parser.ast.SubgraphNode;
import com.gaibu.flowlab.transformer.model.Edge;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import com.gaibu.flowlab.transformer.model.Node;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MermaidTransformer 测试
 */
class MermaidTransformerTest {

    @Test
    void testTransformNullAst() {
        MermaidTransformer transformer = new MermaidTransformer();
        FlowGraph graph = transformer.transform(null);

        assertThat(graph.getNodes()).isEmpty();
        assertThat(graph.getEdges()).isEmpty();
    }

    @Test
    void testTransformWithSubgraphAndEdges() {
        FlowchartNode nodeA = FlowchartNode.builder()
                .id("A")
                .label("开始")
                .shape(NodeShape.RECTANGLE)
                .build();

        FlowchartNode nodeB = FlowchartNode.builder()
                .id("B")
                .label("步骤1")
                .shape(NodeShape.RECTANGLE)
                .build();

        FlowchartNode nodeC = FlowchartNode.builder()
                .id("C")
                .label("步骤2")
                .shape(NodeShape.RECTANGLE)
                .build();

        EdgeNode edgeAB = EdgeNode.builder()
                .fromId("A")
                .toId("B")
                .label("")
                .condition("")
                .build();

        EdgeNode edgeBC = EdgeNode.builder()
                .fromId("B")
                .toId("C")
                .label("toC")
                .condition("?x>0")
                .build();

        SubgraphNode subgraph = SubgraphNode.builder()
                .title("子流程")
                .statements(Arrays.asList(nodeB, edgeBC, nodeC))
                .build();

        List<ASTNode> statements = Arrays.asList(nodeA, edgeAB, subgraph);
        FlowchartAST ast = FlowchartAST.builder()
                .direction("TD")
                .statements(statements)
                .build();

        MermaidTransformer transformer = new MermaidTransformer();
        FlowGraph graph = transformer.transform(ast);

        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);

        Node node = graph.getNodes().stream()
                .filter(item -> "A".equals(item.getId()))
                .findFirst()
                .orElse(null);
        assertThat(node).isNotNull();
        assertThat(node.getShape()).isEqualTo("rectangle");

        Edge edge = graph.getEdges().stream()
                .filter(item -> "B".equals(item.getFrom()) && "C".equals(item.getTo()))
                .findFirst()
                .orElse(null);
        assertThat(edge).isNotNull();
        assertThat(edge.getLabel()).isEqualTo("toC");
        assertThat(edge.getCondition()).isEqualTo("?x>0");
    }

    @Test
    void testImplicitNodesCreatedFromEdges() {
        // Edge references nodes not declared elsewhere
        EdgeNode edge = EdgeNode.builder()
                .fromId("X")
                .toId("Y")
                .label("")
                .condition("")
                .build();

        FlowchartAST ast = FlowchartAST.builder()
                .direction("TD")
                .statements(List.of(edge))
                .build();

        MermaidTransformer transformer = new MermaidTransformer();
        FlowGraph graph = transformer.transform(ast);

        assertThat(graph.getNodes()).hasSize(2);
        Node nodeX = graph.getNodes().stream().filter(n -> "X".equals(n.getId())).findFirst().orElse(null);
        Node nodeY = graph.getNodes().stream().filter(n -> "Y".equals(n.getId())).findFirst().orElse(null);
        assertThat(nodeX).isNotNull();
        assertThat(nodeY).isNotNull();
        assertThat(nodeX.getShape()).isEqualTo("rectangle");
        assertThat(nodeY.getShape()).isEqualTo("rectangle");
    }
}
