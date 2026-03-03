package com.gaibu.flowlab.engine.graph;

import com.gaibu.flowlab.engine.runtime.NodeId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于内存结构的可执行图实现。
 */
public class InMemoryExecutableGraph implements ExecutableGraph {

    /**
     * 节点索引。
     */
    private final Map<NodeId, ExecutableNode> nodes = new LinkedHashMap<>();

    /**
     * 出边索引。
     */
    private final Map<NodeId, List<ExecutableEdge>> outgoingIndex = new LinkedHashMap<>();

    /**
     * 入边索引。
     */
    private final Map<NodeId, List<ExecutableEdge>> incomingIndex = new LinkedHashMap<>();

    /**
     * 开始节点。
     */
    private NodeId startNodeId;

    @Override
    public ExecutableNode getNode(NodeId nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public List<ExecutableEdge> outgoing(NodeId nodeId) {
        return outgoingIndex.getOrDefault(nodeId, List.of());
    }

    @Override
    public List<ExecutableEdge> incoming(NodeId nodeId) {
        return incomingIndex.getOrDefault(nodeId, List.of());
    }

    @Override
    public NodeId startNodeId() {
        return startNodeId;
    }

    /**
     * 返回可变节点索引，供编译器写入。
     *
     * @return 节点索引
     */
    public Map<NodeId, ExecutableNode> mutableNodes() {
        return nodes;
    }

    /**
     * 返回可变出边索引，供编译器写入。
     *
     * @return 出边索引
     */
    public Map<NodeId, List<ExecutableEdge>> mutableOutgoingIndex() {
        return outgoingIndex;
    }

    /**
     * 返回可变入边索引，供编译器写入。
     *
     * @return 入边索引
     */
    public Map<NodeId, List<ExecutableEdge>> mutableIncomingIndex() {
        return incomingIndex;
    }

    /**
     * 设置开始节点。
     *
     * @param startNodeId 开始节点 ID
     */
    public void setStartNodeId(NodeId startNodeId) {
        this.startNodeId = startNodeId;
    }
}
