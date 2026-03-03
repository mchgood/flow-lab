package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.Token;

/**
 * Join 判定策略。
 */
public interface JoinStrategy {

    /**
     * 是否满足汇聚条件。
     *
     * @param scope 作用域运行时
     * @param token 当前到达 Token
     * @return true 表示可继续
     */
    boolean canJoin(ScopeRuntime scope, Token token);
}
