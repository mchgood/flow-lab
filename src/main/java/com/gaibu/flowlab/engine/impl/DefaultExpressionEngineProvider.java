package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.ExpressionEngine;
import com.gaibu.flowlab.engine.api.ExpressionEngineProvider;

/**
 * 默认表达式引擎提供器。
 */
public class DefaultExpressionEngineProvider implements ExpressionEngineProvider {

    private final ExpressionEngine engine;

    /**
     * 构造DefaultExpressionEngineProvider实例。
     */
    public DefaultExpressionEngineProvider() {
        this.engine = new SpelExpressionEngine();
    }

    @Override
    /**
     * 执行get并返回结果。
     * @return 执行结果
     */
    public ExpressionEngine get() {
        return engine;
    }
}

