package com.gaibu.flowlab.engine.graph;

import com.gaibu.flowlab.engine.runtime.NodeId;

import java.util.List;

/**
 * 编译后的流程可执行图接口。
 */
public interface ExecutableGraph {

    /**
     * 获取节点。
     *
     * @param nodeId 节点 ID
     * @return 节点对象，不存在返回 null
     */
    ExecutableNode getNode(NodeId nodeId);

    /**
     * 获取节点出边。
     *
     * @param nodeId 节点 ID
     * @return 出边列表
     */
    List<ExecutableEdge> outgoing(NodeId nodeId);

    /**
     * 获取节点入边。
     *
     * @param nodeId 节点 ID
     * @return 入边列表
     */
    List<ExecutableEdge> incoming(NodeId nodeId);

    /**
     * 获取开始节点。
     *
     * @return 开始节点 ID
     */
    NodeId startNodeId();
}
