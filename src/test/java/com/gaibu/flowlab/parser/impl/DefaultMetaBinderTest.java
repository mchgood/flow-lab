package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.model.GraphMeta;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link DefaultMetaBinder} 语义绑定测试。
 *
 * <p>验证并行元数据映射与重复指令冲突校验。
 */
class DefaultMetaBinderTest {

    private final DefaultFlowchartParser flowchartParser = new DefaultFlowchartParser();
    private final DefaultDirectiveScanner directiveScanner = new DefaultDirectiveScanner();
    private final DefaultMetaBinder metaBinder = new DefaultMetaBinder();

    /**
     * 合法并行配置应绑定到 groupMeta/nodeMeta。
     */
    @Test
    void bindParallelAnyMetaSuccessfully() {
        String mermaid = """
                flowchart TD
                S[Start]
                %% @parallel_group node=A group=g1
                %% @parallel node=A group=g1 mode=ANY any_complete_to=D
                A[Task A]
                %% @parallel_group node=B group=g1
                B[Task B]
                D[Done]
                S --> A
                S --> B
                A --> D
                B --> D
                """;

        GraphMeta meta = metaBinder.bind(flowchartParser.parse(mermaid), directiveScanner.scan(mermaid));

        assertThat(meta.getGroupMeta()).containsKey("g1");
        assertThat(meta.getNodeMeta().get("A").getGroupId()).isEqualTo("g1");
        assertThat(meta.getNodeMeta().get("B").getGroupId()).isEqualTo("g1");
        assertThat(meta.getGroupMeta().get("g1").getAttributes().get("mode")).isEqualTo("ANY");
    }

    /**
     * 同一节点同类型 directive 重复定义必须报错。
     */
    @Test
    void rejectDuplicateSameDirectiveTypeOnNode() {
        String mermaid = """
                flowchart TD
                %% @timeout node=A ms=1000
                %% @timeout node=A timeout=2s
                A[Task A]
                """;

        assertThatThrownBy(() -> metaBinder.bind(flowchartParser.parse(mermaid), directiveScanner.scan(mermaid)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("同类型指令不可重复");
    }
}
