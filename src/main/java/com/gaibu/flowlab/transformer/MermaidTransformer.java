package com.gaibu.flowlab.transformer;

import com.gaibu.flowlab.parser.ast.*;
import com.gaibu.flowlab.transformer.model.Edge;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import com.gaibu.flowlab.transformer.model.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Mermaid 转换器
 * 将 AST 转换为点线结构的 JSON 模型
 */
public class MermaidTransformer {

    private final Map<String, Node> nodeMap;

    public MermaidTransformer() {
        this.nodeMap = new HashMap<>();
    }

    /**
     * 预加载解析阶段收集的节点，保留标签与形状
     */
    public void preloadNodes(Map<String, FlowchartNode> registry) {
        if (registry == null) {
            return;
        }
        registry.values().forEach(this::addNode);
    }

    /**
     * 转换 AST 为 FlowGraph
     */
    public FlowGraph transform(FlowchartAST ast) {
        FlowGraph flowGraph = new FlowGraph();

        if (ast == null || ast.getStatements() == null) {
            return flowGraph;
        }

        // 第一遍遍历：收集所有节点
        collectNodes(ast.getStatements());

        // 第二遍遍历：处理边和子图
        for (ASTNode statement : ast.getStatements()) {
            processStatement(statement, flowGraph);
        }

        // 添加所有收集到的节点
        flowGraph.getNodes().addAll(nodeMap.values());

        return flowGraph;
    }

    /**
     * 收集所有节点
     */
    private void collectNodes(java.util.List<ASTNode> statements) {
        for (ASTNode statement : statements) {
            if (statement instanceof FlowchartNode) {
                FlowchartNode flowchartNode = (FlowchartNode) statement;
                addNode(flowchartNode);
            } else if (statement instanceof EdgeNode) {
                // 边节点可能包含隐式定义的节点，在处理边时会处理
            } else if (statement instanceof SubgraphNode) {
                SubgraphNode subgraph = (SubgraphNode) statement;
                collectNodes(subgraph.getStatements());
            }
        }
    }

    /**
     * 处理单个语句
     */
    private void processStatement(ASTNode statement, FlowGraph flowGraph) {
        if (statement instanceof FlowchartNode) {
            // 节点已在 collectNodes 中处理
            return;
        } else if (statement instanceof EdgeNode) {
            EdgeNode edgeNode = (EdgeNode) statement;
            processEdge(edgeNode, flowGraph);
        } else if (statement instanceof SubgraphNode) {
            SubgraphNode subgraph = (SubgraphNode) statement;
            processSubgraph(subgraph, flowGraph);
        }
    }

    /**
     * 处理边
     */
    private void processEdge(EdgeNode edgeNode, FlowGraph flowGraph) {
        ensureNodeExists(edgeNode.getFromId());
        ensureNodeExists(edgeNode.getToId());

        // 创建边
        Edge edge = Edge.builder()
                .from(edgeNode.getFromId())
                .to(edgeNode.getToId())
                .label(edgeNode.getLabel() != null ? edgeNode.getLabel() : "")
                .condition(edgeNode.getCondition() != null ? edgeNode.getCondition() : "")
                .build();

        flowGraph.addEdge(edge);
    }

    /**
     * 处理子图
     */
    private void processSubgraph(SubgraphNode subgraph, FlowGraph flowGraph) {
        // 递归处理子图内的语句
        for (ASTNode statement : subgraph.getStatements()) {
            processStatement(statement, flowGraph);
        }
    }

    /**
     * 添加节点到映射
     */
    private void addNode(FlowchartNode flowchartNode) {
        if (nodeMap.containsKey(flowchartNode.getId())) {
            return;
        }

        Node node = Node.builder()
                .id(flowchartNode.getId())
                .label(flowchartNode.getLabel())
                .type(mapShapeToType(flowchartNode.getShape()))
                .shape(flowchartNode.getShape().getValue())
                .build();

        nodeMap.put(flowchartNode.getId(), node);
    }

    /**
     * 确保节点存在（隐式节点默认矩形）
     */
    private void ensureNodeExists(String nodeId) {
        if (nodeMap.containsKey(nodeId)) {
            return;
        }
        Node node = Node.builder()
                .id(nodeId)
                .label(nodeId)
                .type("rectangle")
                .shape("rectangle")
                .build();
        nodeMap.put(nodeId, node);
    }

    /**
     * 将节点形状映射为类型
     */
    private String mapShapeToType(NodeShape shape) {
        if (shape == null) {
            return "rectangle";
        }

        return shape.getValue();
    }

    /**
     * 获取节点映射（用于测试）
     */
    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }
}
