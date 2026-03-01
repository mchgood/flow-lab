package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.model.Directive;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultDirectiveScanner} 的指令解析测试。
 *
 * <p>覆盖引号参数解析与 directive 作用域绑定约束。
 */
class DefaultDirectiveScannerTest {

    private final DefaultDirectiveScanner scanner = new DefaultDirectiveScanner();

    /**
     * 带引号参数值应去除引号并保留空格内容。
     */
    @Test
    void scanDirectiveWithQuotedValue() {
        String mermaid = """
                flowchart TD
                %% @subflow node=A ref="workflow 2"
                A[[Call]]
                """;

        List<Directive> directives = scanner.scan(mermaid);
        assertThat(directives).hasSize(1);
        assertThat(directives.get(0).getArguments().get("ref")).isEqualTo("workflow 2");
    }

    /**
     * directive 的 node 参数必须与紧随节点一致，否则报错。
     */
    @Test
    void rejectNodeScopeMismatch() {
        String mermaid = """
                flowchart TD
                %% @timeout node=B ms=5000
                A[Task]
                """;

        assertThatThrownBy(() -> scanner.scan(mermaid))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("不一致");
    }
}
