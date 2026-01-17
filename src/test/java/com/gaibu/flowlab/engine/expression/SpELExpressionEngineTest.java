package com.gaibu.flowlab.engine.expression;

import com.gaibu.flowlab.engine.model.ExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SpELExpressionEngine 测试类
 */
@DisplayName("SpELExpressionEngine 测试")
class SpELExpressionEngineTest {

    private SpELExpressionEngine engine;
    private ExecutionContext context;

    @BeforeEach
    void setUp() {
        engine = new SpELExpressionEngine();
        context = new ExecutionContext();
        context.setId("test-context");
    }

    @Test
    @DisplayName("EE-001: 评估简单布尔表达式")
    void testEvaluateSimpleBooleanExpression() {
        // 设置变量
        context.setVariable("amount", 1500);

        // 评估表达式
        boolean result = engine.evaluate("#amount > 1000", context);

        // 验证返回 true
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EE-002: 评估复杂布尔表达式")
    void testEvaluateComplexBooleanExpression() {
        // 设置变量
        context.setVariable("amount", 1500);
        context.setVariable("status", "pending");

        // 评估表达式
        boolean result = engine.evaluate("#amount > 1000 and #status == 'pending'", context);

        // 验证返回 true
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EE-003: 评估逻辑运算表达式")
    void testEvaluateLogicalExpression() {
        // 设置变量
        context.setVariable("type", "A");

        // 评估表达式
        boolean result = engine.evaluate("#type == 'A' or #type == 'B'", context);

        // 验证返回 true
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EE-004: 评估比较运算表达式")
    void testEvaluateComparisonExpression() {
        // 设置变量
        context.setVariable("count", 10);

        // 评估表达式
        boolean result = engine.evaluate("#count >= 10", context);

        // 验证返回 true
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EE-005: 评估不等式表达式")
    void testEvaluateInequalityExpression() {
        // 设置变量
        context.setVariable("status", "approved");

        // 评估表达式
        boolean result = engine.evaluate("#status != 'rejected'", context);

        // 验证返回 true
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("EE-006: 计算表达式值")
    void testCalculateExpressionValue() {
        // 设置变量
        context.setVariable("price", 100);
        context.setVariable("quantity", 5);

        // 计算表达式
        Object result = engine.calculateValue("#price * #quantity", context);

        // 验证返回 500
        assertThat(result).isEqualTo(500);
    }

    @Test
    @DisplayName("EE-007: 验证有效表达式语法")
    void testValidateValidSyntax() {
        // 验证有效表达式
        boolean isValid = engine.validateSyntax("#amount > 1000");

        // 验证返回 true
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("EE-008: 验证无效表达式语法")
    void testValidateInvalidSyntax() {
        // 验证无效表达式
        boolean isValid = engine.validateSyntax("#amount >");

        // 验证返回 false
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("EE-009: 评估表达式时变量不存在")
    void testEvaluateWithMissingVariable() {
        // 不设置任何变量，尝试评估表达式
        assertThatThrownBy(() -> engine.evaluate("#amount > 1000", context))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("Failed to evaluate expression");
    }

    @Test
    @DisplayName("EE-010: 评估表达式时类型不匹配")
    void testEvaluateWithTypeMismatch() {
        // 设置字符串类型的 amount
        context.setVariable("amount", "abc");

        // 尝试进行数值比较
        assertThatThrownBy(() -> engine.evaluate("#amount > 1000", context))
                .isInstanceOf(ExpressionEvaluationException.class)
                .hasMessageContaining("Failed to evaluate expression");
    }
}
