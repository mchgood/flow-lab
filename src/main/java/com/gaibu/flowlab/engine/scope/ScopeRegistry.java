package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.NodeId;

/**
 * Scope 定义注册表。
 */
public interface ScopeRegistry {

    /**
     * 根据节点查找作用域定义。
     *
     * @param nodeId 节点 ID
     * @return 作用域定义，不存在返回 null
     */
    ScopeDefinition getScope(NodeId nodeId);
}
