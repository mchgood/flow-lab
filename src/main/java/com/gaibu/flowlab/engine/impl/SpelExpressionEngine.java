package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.ExpressionEngine;
import com.gaibu.flowlab.engine.exception.ExpressionEvaluationException;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * SpEL 表达式引擎实现。
 */
public class SpelExpressionEngine implements ExpressionEngine {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    /**
     * 执行evaluate并返回结果。
     * @return 执行结果
     */
    public Object evaluate(String expression, Map<String, Object> variables) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext(variables);
            context.addPropertyAccessor(new MapAccessor());
            context.setVariables(variables);
            // 显式禁用方法调用，避免表达式触发任意方法执行。
            context.setMethodResolvers(List.of());
            // 禁止类型定位，阻断 T(...) 等类型访问能力。
            context.setTypeLocator(typeName -> {
                throw new EvaluationException("禁止类型访问: " + typeName);
            });
            return parser.parseExpression(expression).getValue(context);
        } catch (Exception e) {
            throw new ExpressionEvaluationException("表达式求值失败: " + expression, e);
        }
    }
}
