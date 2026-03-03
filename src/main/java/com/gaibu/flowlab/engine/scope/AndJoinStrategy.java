package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.Token;

/**
 * AND Join 判定策略。
 */
public class AndJoinStrategy implements JoinStrategy {

    @Override
    public boolean canJoin(ScopeRuntime scope, Token token) {
        return scope.getArrivedTokens().size() >= scope.getExpectedTokenCount();
    }
}
