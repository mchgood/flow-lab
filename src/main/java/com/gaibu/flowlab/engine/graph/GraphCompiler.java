package com.gaibu.flowlab.engine.graph;

import com.gaibu.flowlab.engine.behavior.NodeBehaviorFactory;
import com.gaibu.flowlab.engine.runtime.NodeId;
import com.gaibu.flowlab.parser.model.entity.Edge;
import com.gaibu.flowlab.parser.model.entity.Node;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import com.gaibu.flowlab.parser.model.enums.NodeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程定义到可执行图的编译器。
 */
public class GraphCompiler {

    /**
     * 节点行为工厂。
     */
    private final NodeBehaviorFactory behaviorFactory;

    public GraphCompiler() {
        this(new NodeBehaviorFactory());
    }

    public GraphCompiler(NodeBehaviorFactory behaviorFactory) {
        this.behaviorFactory = behaviorFactory;
    }

    /**
     * 编译流程定义。
     *
     * @param definition 流程定义
     * @return 可执行图
     */
    public ExecutableGraph compile(ProcessDefinition definition) {
        InMemoryExecutableGraph graph = new InMemoryExecutableGraph();

        for (Node node : definition.getNodes().values()) {
            ExecutableNode executableNode = new ExecutableNode();
            executableNode.setId(new NodeId(node.getId()));
            executableNode.setType(node.getType());
            executableNode.setGatewayType(node.getGatewayType());
            executableNode.getMetadata().putAll(node.getMetadata());
            executableNode.setBehavior(behaviorFactory.create(node));
            graph.mutableNodes().put(executableNode.getId(), executableNode);

            if (node.getType() == NodeType.START) {
                graph.setStartNodeId(executableNode.getId());
            }
        }

        Map<NodeId, List<ExecutableEdge>> outgoing = new LinkedHashMap<>();
        Map<NodeId, List<ExecutableEdge>> incoming = new LinkedHashMap<>();

        for (Edge edge : definition.getEdges().values()) {
            ExecutableEdge executableEdge = new ExecutableEdge();
            executableEdge.setId(edge.getId());
            executableEdge.setSource(new NodeId(edge.getSourceRef()));
            executableEdge.setTarget(new NodeId(edge.getTargetRef()));
            executableEdge.setConditionExpression(edge.getConditionExpression());
            executableEdge.setDefaultEdge(edge.isDefaultEdge());

            outgoing.computeIfAbsent(executableEdge.getSource(), key -> new ArrayList<>()).add(executableEdge);
            incoming.computeIfAbsent(executableEdge.getTarget(), key -> new ArrayList<>()).add(executableEdge);
        }

        graph.mutableOutgoingIndex().putAll(outgoing);
        graph.mutableIncomingIndex().putAll(incoming);

        if (graph.startNodeId() == null) {
            throw new IllegalStateException("No start node found in process definition: " + definition.getId());
        }

        return graph;
    }
}
