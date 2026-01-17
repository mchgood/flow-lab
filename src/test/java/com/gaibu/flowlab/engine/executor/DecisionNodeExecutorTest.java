package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.transformer.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DecisionNodeExecutor 测试类
 */
@DisplayName("DecisionNodeExecutor 测试")
class DecisionNodeExecutorTest {

    private DecisionNodeExecutor executor;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        executor = new DecisionNodeExecutor();
        context = new ExecutionContext();
        context.setId("test-context");
    }

    @Test
    @DisplayName("DNE-001: 获取支持的节点形状")
    void testGetSupportedShape() {
        // 调用 getSupportedShape()
        String shape = executor.getSupportedShape();

        // 验证返回 "diamond"
        assertThat(shape).isEqualTo("diamond");
    }

    @Test
    @DisplayName("DNE-002: 执行决策节点")
    void testExecuteDecisionNode() {
        // 创建菱形节点
        Node node = Node.builder()
                .id("decision1")
                .label("金额>1000?")
                .shape("diamond")
                .type("decision")
                .build();

        // 执行节点
        NodeExecutionResult result = executor.execute(node, context);

        // 验证返回成功结果，状态为 SUCCESS
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NodeExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("DNE-003: 验证菱形节点")
    void testValidateDiamondNode() {
        // 创建菱形节点
        Node node = Node.builder()
                .id("decision2")
                .label("条件判断")
                .shape("diamond")
                .build();

        // 验证节点
        boolean isValid = executor.validate(node);

        // 验证返回 true
        assertThat(isValid).isTrue();
    }
}
