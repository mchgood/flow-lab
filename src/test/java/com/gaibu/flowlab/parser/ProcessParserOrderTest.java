package com.gaibu.flowlab.parser;

import com.gaibu.flowlab.parser.impl.MermaidProcessParser;
import com.gaibu.flowlab.parser.model.entity.Edge;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessParserOrderTest {

    private final ProcessParser parser = new MermaidProcessParser();

    @Test
    void shouldPreserveOutgoingEdgeOrder() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{XOR}
                G1 -->|cond1| A[TaskA]
                G1 -->|cond2| B[TaskB]
                G1 -->|default| C[TaskC]
                A --> E(End)
                B --> E
                C --> E
                """;

        ProcessDefinition definition = parser.parse("order-test", dsl);

        List<Edge> outgoing = definition.getOutgoingIndex().get("G1");

        assertThat(outgoing).extracting(Edge::getConditionExpression)
                .containsExactly("cond1", "cond2", null);

        assertThat(outgoing.get(2).isDefaultEdge()).isTrue();
    }

    @Test
    void shouldBuildIncomingIndexCorrectly() {
        String dsl = """
                flowchart TD
                S(Start) --> A[TaskA]
                S --> B[TaskB]
                A --> E(End)
                B --> E
                """;

        ProcessDefinition definition = parser.parse("incoming-index", dsl);

        assertThat(definition.getIncomingIndex().get("E")).hasSize(2);
    }
}