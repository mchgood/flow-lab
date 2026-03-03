package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.NodeId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Scope 静态定义模型。
 */
@Getter
@Setter
@NoArgsConstructor
public class ScopeDefinition {

    /**
     * 作用域 ID。
     */
    private String scopeId;

    /**
     * 对应 Join 节点。
     */
    private NodeId joinNode;

    /**
     * 作用域期望分支数量。
     */
    private int expectedTokenCount;
}
