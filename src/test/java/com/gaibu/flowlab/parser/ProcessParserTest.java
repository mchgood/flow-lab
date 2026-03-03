package com.gaibu.flowlab.parser;

import com.gaibu.flowlab.parser.impl.MermaidProcessParser;
import com.gaibu.flowlab.parser.model.entity.Edge;
import com.gaibu.flowlab.parser.model.entity.Node;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessParserTest {

    private final ProcessParser parser = new MermaidProcessParser();

    @Test
    void shouldParseExclusiveGateway() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{XOR}
                G1 -->|amount > 1000| A[Approve]
                G1 -->|default| B[Reject]
                A --> E(End)
                B --> E
                """;

        ProcessDefinition definition = parser.parse("xor-process", dsl);

        Node gateway = definition.getNodes().get("G1");
        assertThat(gateway).isNotNull();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
        assertThat(gateway.getGatewayType()).isEqualTo(GatewayType.EXCLUSIVE);

        List<Edge> outgoing = definition.getOutgoingIndex().get("G1");
        assertThat(outgoing).hasSize(2);
        assertThat(outgoing.get(0).getConditionExpression()).isEqualTo("amount > 1000");
        assertThat(outgoing.get(0).isDefaultEdge()).isFalse();
        assertThat(outgoing.get(1).isDefaultEdge()).isTrue();
    }

    @Test
    void shouldParseParallelGateway() {
        String dsl = """
                flowchart TD
                S(Start) --> G2{AND}
                G2 --> A[TaskA]
                G2 --> B[TaskB]
                A --> E(End)
                B --> E
                """;

        ProcessDefinition definition = parser.parse("and-process", dsl);

        Node gateway = definition.getNodes().get("G2");
        assertThat(gateway).isNotNull();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
        assertThat(gateway.getGatewayType()).isEqualTo(GatewayType.PARALLEL);

        assertThat(definition.getOutgoingIndex().get("G2")).hasSize(2);
        assertThat(definition.getIncomingIndex().get("E")).hasSize(2);
    }

    @Test
    void shouldParseInclusiveGateway() {
        String dsl = """
                flowchart TD
                S(Start) --> G3{OR}
                G3 -->|vip| A[VipTask]
                G3 -->|region == 'CN'| B[CnTask]
                A --> E(End)
                B --> E
                """;

        ProcessDefinition definition = parser.parse("or-process", dsl);

        Node gateway = definition.getNodes().get("G3");
        assertThat(gateway).isNotNull();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
        assertThat(gateway.getGatewayType()).isEqualTo(GatewayType.INCLUSIVE);

        List<Edge> outgoing = definition.getOutgoingIndex().get("G3");
        assertThat(outgoing).hasSize(2);
        assertThat(outgoing).extracting(Edge::getConditionExpression)
                .containsExactly("vip", "region == 'CN'");
    }

    @Test
    void shouldParseComplexGraphWithGatewaysAndEnhancements() {
        String dsl = """
                flowchart TD
                %% @node:T1 timeout=5s retry=3 async=true
                %% @scope:G2 timeout=10s cancelStrategy=flow onChildError=cancelAll
                S(Start) --> T1[Prepare]
                T1 --> G1{XOR}
                G1 -->|amount > 1000| A[HighTask]
                G1 -->|default| B[MidTask]
                A --> G2{AND}
                B --> G2
                G2 --> P1[ParallelTask1]
                G2 --> P2[ParallelTask2]
                G2 -->|timeout| Tm[TimeoutTask]
                P1 --> G3{OR}
                P2 --> G3
                G3 -->|vip| V[VipTask]
                G3 -->|region == 'CN'| R[RegionTask]
                V --> End(End)
                R --> End
                Tm --> End
                """;

        ProcessDefinition definition = parser.parse("complex-process", dsl);

        assertThat(definition.getId()).isEqualTo("complex-process");

        assertThat(definition.getNodes().get("G1").getGatewayType()).isEqualTo(GatewayType.EXCLUSIVE);
        assertThat(definition.getNodes().get("G2").getGatewayType()).isEqualTo(GatewayType.PARALLEL);
        assertThat(definition.getNodes().get("G3").getGatewayType()).isEqualTo(GatewayType.INCLUSIVE);

        assertThat(definition.getNodes().get("T1").getMetadata())
                .containsEntry("timeout", "5s")
                .containsEntry("retry", 3)
                .containsEntry("async", true);

        assertThat(definition.getNodes().get("G2").getMetadata())
                .containsEntry("scope.timeout", "10s")
                .containsEntry("scope.cancelStrategy", "flow")
                .containsEntry("scope.onChildError", "cancelAll");

        List<Edge> xorOutgoing = definition.getOutgoingIndex().get("G1");
        assertThat(xorOutgoing).hasSize(2);
        assertThat(xorOutgoing.get(1).isDefaultEdge()).isTrue();

        assertThat(definition.getOutgoingIndex()).containsKeys("G1", "G2", "G3");
        assertThat(definition.getIncomingIndex().get("End")).hasSize(3);
    }
}
