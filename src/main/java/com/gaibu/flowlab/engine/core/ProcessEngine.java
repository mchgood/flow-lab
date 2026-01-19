package com.gaibu.flowlab.engine.core;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.event.*;
import com.gaibu.flowlab.engine.executor.NodeExecutor;
import com.gaibu.flowlab.engine.executor.NodeExecutorRegistry;
import com.gaibu.flowlab.engine.expression.ExpressionEngine;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import com.gaibu.flowlab.service.FlowParserService;
import com.gaibu.flowlab.transformer.model.Edge;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import com.gaibu.flowlab.transformer.model.Node;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程执行引擎 - 负责流程实例的执行调度和状态管理
 */
public class ProcessEngine {
    private final NodeExecutorRegistry executorRegistry;
    private final ExpressionEngine expressionEngine;
    private final EventPublisher eventPublisher;
    private final FlowParserService flowParserService;

    public ProcessEngine(NodeExecutorRegistry executorRegistry,
                         ExpressionEngine expressionEngine,
                         EventPublisher eventPublisher) {
        this.executorRegistry = executorRegistry;
        this.expressionEngine = expressionEngine;
        this.eventPublisher = eventPublisher;
        this.flowParserService = new FlowParserService();
    }

    /**
     * 执行流程实例
     */
    public void execute(ProcessInstance instance, String mermaidSource) {
        try {
            // 1. 发布流程启动事件
            eventPublisher.publish(new ProcessStartedEvent(instance));

            // 2. 解析流程定义，获取 FlowGraph
            FlowGraph flowGraph = flowParserService.parse(mermaidSource);

            // 3. 查找开始节点（圆形节点）
            Node startNode = findStartNode(flowGraph);
            if (startNode == null) {
                throw new IllegalStateException("No start node found in flow graph");
            }

            // 4. 更新流程实例状态
            instance.setStatus(ProcessInstanceStatus.RUNNING);
            instance.setCurrentNodeId(startNode.getId());

            // 5. 从开始节点开始执行（迭代调度，包含循环检测）
            executeIterative(instance, flowGraph, startNode.getId());

        } catch (Exception e) {
            // 执行失败，终止流程
            instance.setStatus(ProcessInstanceStatus.TERMINATED);
            instance.setEndTime(LocalDateTime.now());
            throw new RuntimeException("Failed to execute process instance: " + instance.getId(), e);
        }
    }

    /**
     * 迭代执行节点，带循环检测与最大步数保护
     */
    private void executeIterative(ProcessInstance instance, FlowGraph flowGraph, String startNodeId) {
        final int MAX_STEPS = 1000;
        int steps = 0;
        List<String> pending = new ArrayList<>();
        pending.add(startNodeId);

        while (!pending.isEmpty()) {
            if (steps++ > MAX_STEPS) {
                throw new IllegalStateException("Process exceeded maximum step limit, possible loop detected.");
            }

            String nodeId = pending.remove(0);
            Node node = getNodeById(flowGraph, nodeId);
            if (node == null) {
                throw new IllegalStateException("Node not found: " + nodeId);
            }

            instance.setCurrentNodeId(nodeId);

            // 1. 发布节点开始事件
            eventPublisher.publish(new NodeStartedEvent(instance, nodeId, node.getLabel()));

            // 2. 获取节点执行器（按节点特征匹配）
            NodeExecutor executor = executorRegistry.getExecutor(node);

            // 3. 执行节点
            NodeExecutionResult result = executor.execute(node, instance.getContext());

            // 4. 发布节点完成事件
            eventPublisher.publish(new NodeCompletedEvent(instance, nodeId, result));

            if (!result.isSuccess()) {
                suspendProcess(instance, result.getErrorMessage());
                return;
            }

            // 5. 决定下一步
            List<String> nextNodeIds = determineNextNodes(flowGraph, nodeId, instance.getContext());
            if (nextNodeIds.isEmpty()) {
                completeProcess(instance);
                return;
            }

            pending.addAll(nextNodeIds);
        }
    }

    /**
     * 确定下一个节点
     */
    private List<String> determineNextNodes(FlowGraph flowGraph, String currentNodeId, ExecutionContext context) {
        List<Edge> outgoingEdges = getOutgoingEdges(flowGraph, currentNodeId);
        List<String> nextNodeIds = new ArrayList<>();

        for (Edge edge : outgoingEdges) {
            // 如果边有条件表达式，则评估
            if (edge.getCondition() != null && !edge.getCondition().isEmpty()) {
                try {
                    boolean conditionMet = expressionEngine.evaluate(edge.getCondition(), context);
                    if (conditionMet) {
                        nextNodeIds.add(edge.getTo());
                    }
                } catch (Exception e) {
                    // 表达式评估失败，记录日志但继续
                    System.err.println("Failed to evaluate expression: " + edge.getCondition() + ", error: " + e.getMessage());
                }
            } else {
                // 无条件边，直接添加
                nextNodeIds.add(edge.getTo());
            }
        }

        return nextNodeIds;
    }

    /**
     * 完成流程
     */
    private void completeProcess(ProcessInstance instance) {
        instance.setStatus(ProcessInstanceStatus.COMPLETED);
        instance.setEndTime(LocalDateTime.now());
        eventPublisher.publish(new ProcessCompletedEvent(instance));
    }

    /**
     * 暂停流程
     */
    private void suspendProcess(ProcessInstance instance, String reason) {
        instance.setStatus(ProcessInstanceStatus.SUSPENDED);
        System.err.println("Process suspended: " + instance.getId() + ", reason: " + reason);
    }

    /**
     * 查找开始节点（圆形节点）
     */
    private Node findStartNode(FlowGraph flowGraph) {
        for (Node node : flowGraph.getNodes()) {
            if (isStartNode(flowGraph, node)) {
                return node;
            }
        }
        return null;
    }

    private boolean isStartNode(FlowGraph flowGraph, Node node) {
        if ("start".equalsIgnoreCase(node.getShape())
                || "start".equalsIgnoreCase(node.getLabel())
                || "start".equalsIgnoreCase(node.getId())) {
            return true;
        }
        if ("circle".equals(node.getShape())) {
            // 圆形且无入边时作为回退方案
            List<Edge> incomingEdges = getIncomingEdges(flowGraph, node.getId());
            return incomingEdges.isEmpty();
        }
        return false;
    }

    /**
     * 根据ID获取节点
     */
    private Node getNodeById(FlowGraph flowGraph, String nodeId) {
        for (Node node : flowGraph.getNodes()) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 获取节点的出边
     */
    private List<Edge> getOutgoingEdges(FlowGraph flowGraph, String nodeId) {
        List<Edge> outgoingEdges = new ArrayList<>();
        for (Edge edge : flowGraph.getEdges()) {
            if (edge.getFrom().equals(nodeId)) {
                outgoingEdges.add(edge);
            }
        }
        return outgoingEdges;
    }

    /**
     * 获取节点的入边
     */
    private List<Edge> getIncomingEdges(FlowGraph flowGraph, String nodeId) {
        List<Edge> incomingEdges = new ArrayList<>();
        for (Edge edge : flowGraph.getEdges()) {
            if (edge.getTo().equals(nodeId)) {
                incomingEdges.add(edge);
            }
        }
        return incomingEdges;
    }
}
