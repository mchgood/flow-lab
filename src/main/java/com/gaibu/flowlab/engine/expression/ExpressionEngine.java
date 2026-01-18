package com.gaibu.flowlab.engine.expression;

import com.gaibu.flowlab.engine.model.ExecutionContext;

/**
 * 表达式引擎接口 - 用于评估条件表达式和计算变量值
 */
public interface ExpressionEngine {
    /**
     * 评估布尔表达式
     * @param expression 表达式字符串
     * @param context 执行上下文
     * @return 表达式评估结果
     */
    boolean evaluate(String expression, ExecutionContext context);

    /**
     * 计算表达式值
     * @param expression 表达式字符串
     * @param context 执行上下文
     * @return 表达式计算结果
     */
    Object calculateValue(String expression, ExecutionContext context);

    /**
     * 验证表达式语法
     * @param expression 表达式字符串
     * @return 是否有效
     */
    boolean validateSyntax(String expression);
}
