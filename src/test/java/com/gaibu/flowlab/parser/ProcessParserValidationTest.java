package com.gaibu.flowlab.parser;

import com.gaibu.flowlab.parser.exception.DefinitionException;
import com.gaibu.flowlab.parser.impl.MermaidProcessParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessParserValidationTest {

    private final ProcessParser parser = new MermaidProcessParser();

    @Test
    void shouldFailWhenMultipleStartNodes() {
        String dsl = """
                flowchart TD
                S1(Start)
                S2(Start)
                S1 --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("multi-start", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("exactly one START");
    }

    @Test
    void shouldFailWhenUsingEventGateway() {
        String dsl = """
                flowchart TD
                S(Start) --> E1((EVENT))
                E1 --> A[Task]
                A --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("event-gateway-unsupported", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("Round node must be Start or End");
    }

    @Test
    void shouldFailWhenXorDefaultNotLast() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{XOR}
                G1 -->|default| A[TaskA]
                G1 -->|amount > 100| B[TaskB]
                A --> E(End)
                B --> E
                """;

        assertThatThrownBy(() -> parser.parse("xor-default-order", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("default edge must be last");
    }

    @Test
    void shouldFailWhenScopeFlowWithoutTimeoutEdge() {
        String dsl = """
                flowchart TD
                %% @scope:G1 timeout=10s cancelStrategy=flow
                S(Start) --> G1{AND}
                G1 --> A[TaskA]
                A --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("scope-flow-invalid", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("timeout edge");
    }

    @Test
    void shouldFailWhenNodeIdContainsUnsupportedCharacters() {
        String dsl = """
                flowchart TD
                S(Start) --> task-a[TaskA]
                task-a --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("invalid-node-id-char", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("Unsupported node token");
    }

    @Test
    void shouldFailWhenAnnotationNodeIdIsNotMermaidIdentifier() {
        String dsl = """
                flowchart TD
                %% @node:task:a timeout=5s
                S(Start) --> task_a[Task]
                task_a --> E(End)
                """;

        assertThatThrownBy(() -> parser.parse("invalid-annotation-node-id", dsl))
                .isInstanceOf(DefinitionException.class)
                .hasMessageContaining("Unsupported node token");
    }
}
