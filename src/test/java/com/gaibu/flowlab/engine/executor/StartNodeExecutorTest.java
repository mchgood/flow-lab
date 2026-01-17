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
 * StartNodeExecutor 测试类
 */
@DisplayName("StartNodeExecutor 测试")
class StartNodeExecutorTest {

    private StartNodeExecutor executor;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        executor = new StartNodeExecutor();
        context = new ExecutionContext();
        context.setId("test-context");
    }

    @Test
    @DisplayName("SNE-001: 获取支持的节点形状")
    void testGetSupportedShape() {
        // 调用 getSupportedShape()
        String shape = executor.getSupportedShape();

        // 验证返回 "circle"
        assertThat(shape).isEqualTo("circle");
    }

    @Test
    @DisplayName("SNE-002: 执行开始节点")
    void testExecuteStartNode() {
        // 创建圆形节点
        Node node = Node.builder()
                .id("start")
                .label("开始")
                .shape("circle")
                .type("start")
                .build();

        // 执行节点
        NodeExecutionResult result = executor.execute(node, context);

        // 验证返回成功结果，状态为 SUCCESS
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NodeExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("SNE-003: 验证圆形节点")
    void testValidateCircleNode() {
        // 创建圆形节点
        Node node = Node.builder()
                .id("start")
                .label("开始")
                .shape("circle")
                .build();

        // 验证节点
        boolean isValid = executor.validate(node);

        // 验证返回 true
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("SNE-004: 验证非圆形节点")
    void testValidateNonCircleNode() {
        // 创建矩形节点
        Node node = Node.builder()
                .id("task")
                .label("任务")
                .shape("rectangle")
                .build();

        // 验证节点
        boolean isValid = executor.validate(node);

        // 验证返回 false
        assertThat(isValid).isFalse();
    }
}
