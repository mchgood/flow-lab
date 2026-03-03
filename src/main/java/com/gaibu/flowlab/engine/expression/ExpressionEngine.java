package com.gaibu.flowlab.engine.expression;

import java.util.Map;

/**
 * 条件表达式求值引擎。
 */
public interface ExpressionEngine {

    /**
     * 计算表达式布尔结果。
     *
     * @param expression 表达式文本
     * @param variables 变量上下文
     * @return true 表示条件命中
     */
    boolean evaluateBoolean(String expression, Map<String, Object> variables);
}
