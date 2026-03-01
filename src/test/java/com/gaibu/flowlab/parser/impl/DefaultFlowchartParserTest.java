package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.model.Graph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultFlowchartParser} 的语法与结构校验测试。
 *
 * <p>重点覆盖子集语法支持范围及 DAG 约束。
 */
class DefaultFlowchartParserTest {

    private final DefaultFlowchartParser parser = new DefaultFlowchartParser();

    /**
     * 验证支持的 flowchart 子集可被正确解析为图结构。
     */
    @Test
    void parseStrictSubsetSuccessfully() {
        String mermaid = """
                flowchart TD
                A[Task A]
                B{Judge}
                C[[Sub]]
                A -->|go| B
                B --> C
                """;

        Graph graph = parser.parse(mermaid);
        assertThat(graph.getNodes()).hasSize(3);
        assertThat(graph.getEdges()).hasSize(2);
    }

    /**
     * 禁止隐式建点：边引用的目标节点必须先显式声明。
     */
    @Test
    void rejectImplicitNodeEdge() {
        String mermaid = """
                flowchart TD
                A[Task A]
                A --> B
                """;

        assertThatThrownBy(() -> parser.parse(mermaid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("边终点未声明");
    }

    /**
     * 非白名单语法（例如圆形节点）必须报错。
     */
    @Test
    void rejectUnsupportedSyntax() {
        String mermaid = """
                flowchart TD
                A((circle))
                """;

        assertThatThrownBy(() -> parser.parse(mermaid))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("不支持的语法");
    }

    /**
     * 图结构必须是 DAG，出现环应拒绝。
     */
    @Test
    void rejectCycleGraph() {
        String mermaid = """
                flowchart TD
                A[Task A]
                B[Task B]
                A --> B
                B --> A
                """;

        assertThatThrownBy(() -> parser.parse(mermaid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("DAG");
    }

    /**
     * 单节点不能同时拥有条件边与顺序边。
     */
    @Test
    void rejectMixedConditionalAndSequentialEdgesOnSameNode() {
        String mermaid = """
                flowchart TD
                A{Judge}
                B[Task B]
                C[Task C]
                A -->|x > 0| B
                A --> C
                """;

        assertThatThrownBy(() -> parser.parse(mermaid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("禁止混用");
    }
}
