package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.NodeId;
import com.gaibu.flowlab.engine.runtime.ScopeId;
import com.gaibu.flowlab.engine.runtime.TokenId;
import com.gaibu.flowlab.engine.runtime.enums.ScopeStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 作用域运行时状态。
 */
@Getter
@Setter
@NoArgsConstructor
public class ScopeRuntime {

    /**
     * 作用域唯一标识。
     */
    private ScopeId id;

    /**
     * 该作用域对应的 Join 节点。
     */
    private NodeId joinNodeId;

    /**
     * 已到达 Join 的 Token 集合。
     */
    private final Set<TokenId> arrivedTokens = new LinkedHashSet<>();

    /**
     * 期望到达 Join 的 Token 总数。
     */
    private int expectedTokenCount;

    /**
     * 作用域状态。
     */
    private ScopeStatus status;
}
