package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图语义元数据。
 */
@Getter
@Setter
public class GraphMeta {

    /**
     * 节点元数据（nodeId -> nodeMeta）。
     */
    private Map<String, NodeMeta> nodeMeta = new LinkedHashMap<>();
    /**
     * 分组元数据（groupId -> groupMeta）。
     */
    private Map<String, GroupMeta> groupMeta = new LinkedHashMap<>();
    /**
     * 子流程元数据（nodeId -> subflowMeta）。
     */
    private Map<String, SubflowMeta> subflows = new LinkedHashMap<>();

    /**
     * 构造GraphMeta实例。
     */
    public GraphMeta() {
    }

    /**
     * 执行putNodeMeta。
     */
    public void putNodeMeta(String nodeId, NodeMeta meta) {
        nodeMeta.put(nodeId, meta);
    }

    /**
     * 执行putGroupMeta。
     */
    public void putGroupMeta(String groupId, GroupMeta meta) {
        groupMeta.put(groupId, meta);
    }

    /**
     * 执行putSubflow。
     */
    public void putSubflow(String nodeId, SubflowMeta subflowMeta) {
        subflows.put(nodeId, subflowMeta);
    }
}
