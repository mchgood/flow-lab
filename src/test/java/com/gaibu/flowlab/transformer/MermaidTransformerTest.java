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
                .build();

        EdgeNode edgeBC = EdgeNode.builder()
                .fromId("B")
                .toId("C")
                .label("toC")
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
    }

    @Test
    void testAddImplicitNode() {
        MermaidTransformer transformer = new MermaidTransformer();
        transformer.addImplicitNode("X");
        transformer.addImplicitNode("X");

        assertThat(transformer.getNodeMap()).hasSize(1);
        Node node = transformer.getNodeMap().get("X");
        assertThat(node).isNotNull();
        assertThat(node.getLabel()).isEqualTo("X");
        assertThat(node.getShape()).isEqualTo("rectangle");
    }
}
