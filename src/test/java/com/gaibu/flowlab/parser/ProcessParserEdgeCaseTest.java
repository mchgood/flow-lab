package com.gaibu.flowlab.parser;

import com.gaibu.flowlab.parser.impl.MermaidProcessParser;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProcessParserEdgeCaseTest {

    private final ProcessParser parser = new MermaidProcessParser();

    @Test
    void shouldIgnoreEmptyLinesAndCodeBlocks() {
        String dsl = """
                ```
                flowchart TD

                S(Start) --> A[Task]

                A --> E(End)
                ```
                """;

        ProcessDefinition definition = parser.parse("ignore-lines", dsl);

        assertThat(definition.getNodes()).containsKeys("S", "A", "E");
    }

    @Test
    void shouldAllowPlainNodeReferenceBeforeDefinition() {
        String dsl = """
                flowchart TD
                S(Start) --> A
                A --> E(End)
                """;

        ProcessDefinition definition = parser.parse("plain-node", dsl);

        assertThat(definition.getNodes()).containsKeys("S", "A", "E");
    }

    @Test
    void shouldFailWhenUnknownNodeInAnnotation() {
        String dsl = """
                flowchart TD
                %% @node:X timeout=PT5M
                S(Start) --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("unknown-annotation", dsl))
                .hasMessageContaining("unknown node");
    }

    @Test
    void shouldFailWhenUnsupportedGatewayType() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{ABC}
                G1 --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("invalid-gateway", dsl))
                .hasMessageContaining("Unsupported gateway type");
    }
}