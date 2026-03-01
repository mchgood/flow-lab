package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.api.model.MermaidDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultMarkdownParser} 的行为测试。
 *
 * <p>覆盖 Markdown 标题、说明块、mermaid block 关联规则与异常分支。
 */
class DefaultMarkdownParserTest {

    private final DefaultMarkdownParser parser = new DefaultMarkdownParser();

    /**
     * 验证标准格式：标题 + 说明引用块 + 单个 mermaid block。
     */
    @Test
    void parseSingleWorkflowWithDescription() {
        String markdown = """
                # wf1
                > 示例说明
                ```mermaid
                flowchart TD
                  A[Task A]
                ```
                """;

        List<MermaidDocument> docs = parser.parse(markdown);
        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getId()).isEqualTo("wf1");
        assertThat(docs.get(0).getDescription()).isEqualTo("示例说明");
    }

    /**
     * 同一标题下出现多个 mermaid block 时应直接拒绝。
     */
    @Test
    void rejectMultipleMermaidUnderSameHeading() {
        String markdown = """
                # wf1
                ```mermaid
                flowchart TD
                A[Task A]
                ```
                ```mermaid
                flowchart TD
                B[Task B]
                ```
                """;

        assertThatThrownBy(() -> parser.parse(markdown))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("多个 mermaid block");
    }

    /**
     * workflowId 必须全局唯一，重复应抛解析异常。
     */
    @Test
    void rejectDuplicateWorkflowId() {
        String markdown = """
                # wf1
                > first
                ```mermaid
                flowchart TD
                A[Task A]
                ```
                # wf1
                > second
                ```mermaid
                flowchart TD
                B[Task B]
                ```
                """;

        assertThatThrownBy(() -> parser.parse(markdown))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("workflowId 重复");
    }

    /**
     * 标题内联描述（# wf > desc）不符合规范，必须使用下一行 > 注释。
     */
    @Test
    void rejectInlineDescriptionInHeading() {
        String markdown = """
                # wf1 > 示例说明
                ```mermaid
                flowchart TD
                A[Task A]
                ```
                """;

        assertThatThrownBy(() -> parser.parse(markdown))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("不支持内联描述");
    }
}
