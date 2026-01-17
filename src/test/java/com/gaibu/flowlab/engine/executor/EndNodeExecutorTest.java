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
 * EndNodeExecutor 测试类
 */
@DisplayName("EndNodeExecutor 测试")
class EndNodeExecutorTest {

    private EndNodeExecutor executor;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        executor = new EndNodeExecutor();
        context = new ExecutionContext();
        context.setId("test-context");
    }

    @Test
    @DisplayName("ENE-001: 获取支持的节点形状")
    void testGetSupportedShape() {
        // 调用 getSupportedShape()
        String shape = executor.getSupportedShape();

        // 验证返回 "circle"
        assertThat(shape).isEqualTo("circle");
    }

    @Test
    @DisplayName("ENE-002: 执行结束节点")
    void testExecuteEndNode() {
        // 创建圆形节点
        Node node = Node.builder()
                .id("end")
                .label("结束")
                .shape("circle")
                .type("end")
                .build();

        // 执行节点
        NodeExecutionResult result = executor.execute(node, context);

        // 验证返回成功结果，状态为 SUCCESS
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NodeExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("ENE-003: 验证结束节点设置完成标志")
    void testExecuteEndNodeSetsCompletionFlag() {
        // 创建结束节点
        Node node = Node.builder()
                .id("end")
                .label("结束")
                .shape("circle")
                .build();

        // 执行节点
        executor.execute(node, context);

        // 检查上下文变量
        Object completed = context.getVariable("processCompleted");

        // 验证上下文中包含 "processCompleted" = true
        assertThat(completed).isEqualTo(true);
    }
}
