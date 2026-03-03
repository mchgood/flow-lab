package com.gaibu.flowlab.engine.scope;

import com.gaibu.flowlab.engine.runtime.NodeId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内存版 Scope 注册表。
 */
public class InMemoryScopeRegistry implements ScopeRegistry {

    /**
     * 节点到 Scope 的映射。
     */
    private final Map<NodeId, ScopeDefinition> byNode = new LinkedHashMap<>();

    @Override
    public ScopeDefinition getScope(NodeId nodeId) {
        return byNode.get(nodeId);
    }

    /**
     * 注册作用域定义。
     *
     * @param nodeId 节点 ID
     * @param definition 定义对象
     */
    public void register(NodeId nodeId, ScopeDefinition definition) {
        byNode.put(nodeId, definition);
    }
}
