package com.gaibu.flowlab.engine.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NodeExecutorRegistry 测试类
 */
@DisplayName("NodeExecutorRegistry 测试")
class NodeExecutorRegistryTest {

    private NodeExecutorRegistry registry;

    @BeforeEach
    void setUp() {
        // 创建多个执行器
        List<NodeExecutor> executors = Arrays.asList(
                new StartNodeExecutor(),
                new TaskNodeExecutor(),
                new DecisionNodeExecutor(),
                new EndNodeExecutor()
        );

        // 创建注册表
        registry = new NodeExecutorRegistry(executors);
    }

    @Test
    @DisplayName("NER-001: 注册节点执行器")
    void testRegisterExecutors() {
        // 验证所有执行器注册成功
        assertThat(registry.hasExecutor("circle")).isTrue();
        assertThat(registry.hasExecutor("rectangle")).isTrue();
        assertThat(registry.hasExecutor("diamond")).isTrue();
    }

    @Test
    @DisplayName("NER-002: 获取已注册的执行器")
    void testGetRegisteredExecutor() {
        // 根据形状获取执行器
        NodeExecutor circleExecutor = registry.getExecutor("circle");
        NodeExecutor rectangleExecutor = registry.getExecutor("rectangle");
        NodeExecutor diamondExecutor = registry.getExecutor("diamond");

        // 验证返回对应的执行器
        assertThat(circleExecutor).isNotNull();
        assertThat(circleExecutor.getSupportedShape()).isEqualTo("circle");

        assertThat(rectangleExecutor).isNotNull();
        assertThat(rectangleExecutor.getSupportedShape()).isEqualTo("rectangle");

        assertThat(diamondExecutor).isNotNull();
        assertThat(diamondExecutor.getSupportedShape()).isEqualTo("diamond");
    }

    @Test
    @DisplayName("NER-003: 获取未注册的执行器")
    void testGetUnregisteredExecutor() {
        // 尝试获取未注册的形状的执行器
        assertThatThrownBy(() -> registry.getExecutor("hexagon"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No executor found for shape: hexagon");
    }

    @Test
    @DisplayName("NER-004: 判断执行器是否存在")
    void testHasExecutor() {
        // 判断已注册的形状
        boolean hasCircle = registry.hasExecutor("circle");
        boolean hasRectangle = registry.hasExecutor("rectangle");
        boolean hasDiamond = registry.hasExecutor("diamond");

        // 判断未注册的形状
        boolean hasHexagon = registry.hasExecutor("hexagon");
        boolean hasTriangle = registry.hasExecutor("triangle");

        // 验证
        assertThat(hasCircle).isTrue();
        assertThat(hasRectangle).isTrue();
        assertThat(hasDiamond).isTrue();
        assertThat(hasHexagon).isFalse();
        assertThat(hasTriangle).isFalse();
    }
}
