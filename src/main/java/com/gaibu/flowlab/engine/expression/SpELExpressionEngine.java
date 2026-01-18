package com.gaibu.flowlab.engine.expression;

import com.gaibu.flowlab.engine.model.ExecutionContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * 基于 Spring Expression Language 的表达式引擎实现
 */
public class SpELExpressionEngine implements ExpressionEngine {
    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    public boolean evaluate(String expression, ExecutionContext context) {
        try {
            Expression exp = parser.parseExpression(expression);
            StandardEvaluationContext evalContext = createEvaluationContext(context);
            Boolean result = exp.getValue(evalContext, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            throw new ExpressionEvaluationException("Failed to evaluate expression: " + expression, e);
        }
    }

    @Override
    public Object calculateValue(String expression, ExecutionContext context) {
        try {
            Expression exp = parser.parseExpression(expression);
            StandardEvaluationContext evalContext = createEvaluationContext(context);
            return exp.getValue(evalContext);
        } catch (Exception e) {
            throw new ExpressionEvaluationException("Failed to calculate expression: " + expression, e);
        }
    }

    @Override
    public boolean validateSyntax(String expression) {
        try {
            parser.parseExpression(expression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建评估上下文，将流程变量注入到表达式上下文中
     */
    private StandardEvaluationContext createEvaluationContext(ExecutionContext context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        // 将流程变量注入到表达式上下文
        context.getAllVariables().forEach(evalContext::setVariable);
        return evalContext;
    }
}
