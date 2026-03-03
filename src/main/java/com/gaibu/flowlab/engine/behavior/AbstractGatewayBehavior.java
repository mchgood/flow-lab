package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import com.gaibu.flowlab.engine.execution.ExecutionContext;

/**
 * 网关行为公共能力基类。
 */
public abstract class AbstractGatewayBehavior implements NodeBehavior {

    /**
     * 表达式求值引擎。
     */
    private final ExpressionEngine expressionEngine;

    protected AbstractGatewayBehavior(ExpressionEngine expressionEngine) {
        this.expressionEngine = expressionEngine;
    }

    /**
     * 计算条件表达式结果。
     *
     * @param expression 条件表达式
     * @param context 执行上下文
     * @return 命中结果
     */
    protected boolean evaluateCondition(String expression, ExecutionContext context) {
        return expressionEngine.evaluateBoolean(expression, context.variables().snapshot());
    }
}
