package com.gaibu.flowlab.engine.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExecutionContext 测试类
 */
@DisplayName("ExecutionContext 测试")
class ExecutionContextTest {

    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        context = new ExecutionContext();
        context.setId("test-context-id");
    }

    @Test
    @DisplayName("EC-001: 设置和获取变量")
    void testSetAndGetVariable() {
        // 设置变量
        context.setVariable("key1", "value1");

        // 获取变量
        Object value = context.getVariable("key1");

        // 验证
        assertThat(value).isEqualTo("value1");
    }

    @Test
    @DisplayName("EC-002: 获取不存在的变量")
    void testGetNonExistentVariable() {
        // 获取不存在的变量
        Object value = context.getVariable("key2");

        // 验证返回 null
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("EC-003: 获取变量带默认值")
    void testGetVariableWithDefault() {
        // 获取不存在的变量，提供默认值
        Object value = context.getVariable("key3", "default");

        // 验证返回默认值
        assertThat(value).isEqualTo("default");
    }

    @Test
    @DisplayName("EC-004: 判断变量是否存在")
    void testHasVariable() {
        // 设置变量
        context.setVariable("key4", "value4");

        // 判断变量是否存在
        boolean exists1 = context.hasVariable("key4");
        boolean exists2 = context.hasVariable("key5");

        // 验证
        assertThat(exists1).isTrue();
        assertThat(exists2).isFalse();
    }

    @Test
    @DisplayName("EC-005: 移除变量")
    void testRemoveVariable() {
        // 设置变量
        context.setVariable("key6", "value6");

        // 移除变量
        context.removeVariable("key6");

        // 获取变量
        Object value = context.getVariable("key6");

        // 验证返回 null
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("EC-006: 批量设置变量")
    void testSetVariables() {
        // 准备多个变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", "value1");
        variables.put("var2", 100);
        variables.put("var3", true);

        // 批量设置变量
        context.setVariables(variables);

        // 获取所有变量
        Map<String, Object> allVariables = context.getAllVariables();

        // 验证
        assertThat(allVariables).containsEntry("var1", "value1");
        assertThat(allVariables).containsEntry("var2", 100);
        assertThat(allVariables).containsEntry("var3", true);
    }

    @Test
    @DisplayName("EC-007: 获取所有变量")
    void testGetAllVariables() {
        // 设置多个变量
        context.setVariable("key1", "value1");
        context.setVariable("key2", 200);
        context.setVariable("key3", false);

        // 获取所有变量
        Map<String, Object> allVariables = context.getAllVariables();

        // 验证
        assertThat(allVariables).hasSize(3);
        assertThat(allVariables).containsEntry("key1", "value1");
        assertThat(allVariables).containsEntry("key2", 200);
        assertThat(allVariables).containsEntry("key3", false);

        // 验证返回的是副本（修改不影响原始数据）
        allVariables.put("key4", "value4");
        assertThat(context.hasVariable("key4")).isFalse();
    }
}
