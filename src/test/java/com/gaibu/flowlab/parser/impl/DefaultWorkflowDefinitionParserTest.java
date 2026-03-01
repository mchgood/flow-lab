package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultWorkflowDefinitionParser} 集成解析测试。
 *
 * <p>覆盖 Markdown 多流程解析与 subflow 引用关系校验。
 */
class DefaultWorkflowDefinitionParserTest {

    private final DefaultWorkflowDefinitionParser parser = new DefaultWorkflowDefinitionParser();

    /**
     * 多 workflow 文档应正确产出定义列表并保留子流程元信息。
     */
    @Test
    void parseWorkflowDefinitionsSuccessfully() {
        String markdown = """
                # wfA
                > A desc
                ```mermaid
                flowchart TD
                A[Task A]
                ```
                # wfB
                > B desc
                ```mermaid
                flowchart TD
                %% @subflow node=B1 ref=wfA
                B1[[Call A]]
                ```
                """;

        List<WorkflowDefinition> definitions = parser.parse(markdown);
        assertThat(definitions).hasSize(2);
        assertThat(definitions.get(0).getId()).isEqualTo("wfA");
        assertThat(definitions.get(0).getGraph().getId()).isEqualTo("wfA");
        assertThat(definitions.get(1).getMeta().getSubflows()).containsKey("B1");
    }

    /**
     * 直接循环引用（A -> B -> A）必须在解析期失败。
     */
    @Test
    void rejectSubflowReferenceCycle() {
        String markdown = """
                # wfA
                ```mermaid
                flowchart TD
                %% @subflow node=A1 ref=wfB
                A1[[Call B]]
                ```
                # wfB
                ```mermaid
                flowchart TD
                %% @subflow node=B1 ref=wfA
                B1[[Call A]]
                ```
                """;

        assertThatThrownBy(() -> parser.parse(markdown))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("循环");
    }

    /**
     * 间接循环引用（A -> B -> C -> A）同样必须在解析期失败。
     */
    @Test
    void rejectIndirectSubflowReferenceCycle() {
        String markdown = """
                # wfA
                ```mermaid
                flowchart TD
                %% @subflow node=A1 ref=wfB
                A1[[Call B]]
                ```
                # wfB
                ```mermaid
                flowchart TD
                %% @subflow node=B1 ref=wfC
                B1[[Call C]]
                ```
                # wfC
                ```mermaid
                flowchart TD
                %% @subflow node=C1 ref=wfA
                C1[[Call A]]
                ```
                """;

        assertThatThrownBy(() -> parser.parse(markdown))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("循环");
    }
}
