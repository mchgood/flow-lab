package com.gaibu.flowlab.engine.expression.impl;

import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * 基于 SpEL 的表达式引擎实现。
 */
public class SpelExpressionEngine implements ExpressionEngine {

    /**
     * SpEL 解析器。
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        if (expression == null || expression.isBlank()) {
            return false;
        }

        String trimmed = expression.trim();
        if ("true".equalsIgnoreCase(trimmed)) {
            return true;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return false;
        }

        Map<String, Object> safeVariables = variables == null ? Map.of() : variables;

        StandardEvaluationContext context = new StandardEvaluationContext(new VariableRoot(safeVariables));
        context.setVariables(safeVariables);
        context.addPropertyAccessor(new VariableRootAccessor());

        Expression parsed = parser.parseExpression(trimmed);
        Object value = parsed.getValue(context);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.TRUE.equals(value);
    }

    /**
     * 表达式根对象。
     *
     * @param values 变量 Map
     */
    private record VariableRoot(Map<String, Object> values) {
    }

    /**
     * 变量属性访问器，使 SpEL 可直接使用裸变量名（如 amount > 1000）。
     */
    private static class VariableRootAccessor implements PropertyAccessor {

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return new Class[]{VariableRoot.class};
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) {
            VariableRoot root = (VariableRoot) target;
            return root.values().containsKey(name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            VariableRoot root = (VariableRoot) target;
            return new TypedValue(root.values().get(name));
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) {
            throw new UnsupportedOperationException("Read-only accessor.");
        }
    }
}
