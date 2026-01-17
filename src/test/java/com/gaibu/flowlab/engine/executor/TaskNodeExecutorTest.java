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
 * TaskNodeExecutor 测试类
 */
@DisplayName("TaskNodeExecutor 测试")
class TaskNodeExecutorTest {

    private TaskNodeExecutor executor;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        executor = new TaskNodeExecutor();
        context = new ExecutionContext();
        context.setId("test-context");
    }

    @Test
    @DisplayName("TNE-001: 获取支持的节点形状")
    void testGetSupportedShape() {
        // 调用 getSupportedShape()
        String shape = executor.getSupportedShape();

        // 验证返回 "rectangle"
        assertThat(shape).isEqualTo("rectangle");
    }

    @Test
    @DisplayName("TNE-002: 执行任务节点")
    void testExecuteTaskNode() {
        // 创建矩形节点
        Node node = Node.builder()
                .id("task1")
                .label("处理订单")
                .shape("rectangle")
                .type("task")
                .build();

        // 执行节点
        NodeExecutionResult result = executor.execute(node, context);

        // 验证返回成功结果，状态为 SUCCESS
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo(NodeExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("TNE-003: 验证任务执行后的上下文")
    void testExecuteTaskNodeUpdatesContext() {
        // 创建节点，标签为 "测试任务"
        Node node = Node.builder()
                .id("task2")
                .label("测试任务")
                .shape("rectangle")
                .build();

        // 执行节点
        executor.execute(node, context);

        // 检查上下文变量
        Object lastTask = context.getVariable("lastExecutedTask");

        // 验证上下文中包含 "lastExecutedTask" = "测试任务"
        assertThat(lastTask).isEqualTo("测试任务");
    }

    @Test
    @DisplayName("TNE-004: 验证矩形节点")
    void testValidateRectangleNode() {
        // 创建矩形节点
        Node node = Node.builder()
                .id("task3")
                .label("任务")
                .shape("rectangle")
                .build();

        // 验证节点
        boolean isValid = executor.validate(node);

        // 验证返回 true
        assertThat(isValid).isTrue();
    }
}
