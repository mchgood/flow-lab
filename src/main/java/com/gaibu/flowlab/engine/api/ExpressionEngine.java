package com.gaibu.flowlab.engine.api;

import java.util.Map;

/**
 * 表达式求值接口。
 */
public interface ExpressionEngine {

    Object evaluate(String expression, Map<String, Object> variables);
}

