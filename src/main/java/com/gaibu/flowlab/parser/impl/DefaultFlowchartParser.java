package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.FlowchartParser;
import com.gaibu.flowlab.parser.api.enums.Direction;
import com.gaibu.flowlab.parser.api.enums.NodeShape;
import com.gaibu.flowlab.parser.api.model.Edge;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flowchart 解析实现。
 *
 * <p>当前实现采用“严格白名单”策略，仅支持约定的 Mermaid 子集：
 * 节点（矩形/菱形/子流程）和单向边（普通边/带标签边）。
 * 解析后会执行 DAG 校验，并校验同一源节点不可混用条件边与顺序边。
 */
public class DefaultFlowchartParser implements FlowchartParser {

    /**
     * 头部：`flowchart + 方向`。
     */
    private static final Pattern HEADER_PATTERN = Pattern.compile("^flowchart\\s+(TD|TB|LR|RL|BT)$");
    /**
     * 矩形节点：`A[Text]`。
     */
    private static final Pattern NODE_RECTANGLE_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[(.+)]$");
    /**
     * 菱形节点：`A{Text}`。
     */
    private static final Pattern NODE_DIAMOND_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\{(.+)}$");
    /**
     * 子流程节点：`A[[Text]]`。
     */
    private static final Pattern NODE_SUBPROCESS_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\[\\[(.+)]\\]$");
    /**
     * 带标签边：`A -->|x > 1| B`。
     */
    private static final Pattern EDGE_LABELED_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*-->\\|([^|]+)\\|\\s*([A-Za-z_][A-Za-z0-9_]*)$");
    /**
     * 普通边：`A --> B`。
     */
    private static final Pattern EDGE_PLAIN_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*-->\\s*([A-Za-z_][A-Za-z0-9_]*)$");

    /**
     * 解析 Mermaid flowchart 文本为图结构。
     *
     * @param mermaidSource Mermaid 源文本
     * @return 解析后的图结构
     */
    @Override
    public Graph parse(String mermaidSource) {
        if (mermaidSource == null || mermaidSource.trim().isEmpty()) {
            throw new ParseException("Mermaid 内容不能为空");
        }

        String[] lines = mermaidSource.split("\\R", -1);
        int headerLineNumber = findHeaderLine(lines);
        Direction direction = parseDirection(lines[headerLineNumber], headerLineNumber + 1);
        Graph graph = new Graph(null, direction);
        Map<String, EdgeType> edgeTypeBySource = new HashMap<>();

        // 行级单通道解析：按“空行/注释/节点/边/报错”的顺序匹配，保证报错位置稳定。
        for (int i = headerLineNumber + 1; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("%%")) {
                continue;
            }
            if (tryParseNode(trimmed, graph)) {
                continue;
            }
            if (tryParseEdge(trimmed, graph, i + 1, edgeTypeBySource)) {
                continue;
            }
            throw new ParseException("不支持的语法: " + trimmed, i + 1, 1);
        }

        if (graph.getNodes().isEmpty()) {
            throw new ValidationException("至少存在一个节点");
        }
        validateDag(graph);
        return graph;
    }

    /**
     * 定位头部行（忽略前导空行和注释行）。
     */
    private int findHeaderLine(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty() || trimmed.startsWith("%%")) {
                continue;
            }
            return i;
        }
        throw new ParseException("缺少 flowchart 头部");
    }

    /**
     * 解析 flowchart 方向。
     */
    private Direction parseDirection(String line, int lineNumber) {
        Matcher matcher = HEADER_PATTERN.matcher(line.trim());
        if (!matcher.matches()) {
            throw new ParseException("仅支持 flowchart + 方向语法", lineNumber, 1);
        }
        return Direction.valueOf(matcher.group(1));
    }

    /**
     * 尝试解析节点定义。
     *
     * @return true 表示当前行已被识别为节点
     */
    private boolean tryParseNode(String line, Graph graph) {
        Matcher subprocess = NODE_SUBPROCESS_PATTERN.matcher(line);
        if (subprocess.matches()) {
            addNode(graph, subprocess.group(1), subprocess.group(2), NodeShape.SUBPROCESS);
            return true;
        }
        Matcher diamond = NODE_DIAMOND_PATTERN.matcher(line);
        if (diamond.matches()) {
            addNode(graph, diamond.group(1), diamond.group(2), NodeShape.DIAMOND);
            return true;
        }
        Matcher rectangle = NODE_RECTANGLE_PATTERN.matcher(line);
        if (rectangle.matches()) {
            addNode(graph, rectangle.group(1), rectangle.group(2), NodeShape.RECTANGLE);
            return true;
        }
        return false;
    }

    /**
     * 添加节点并执行节点 id 唯一性校验。
     */
    private void addNode(Graph graph, String nodeId, String text, NodeShape shape) {
        if (graph.containsNode(nodeId)) {
            throw new ValidationException("Node ID 不可重复", nodeId);
        }
        graph.addNode(new Node(nodeId, text.trim(), shape));
    }

    /**
     * 尝试解析边定义。
     *
     * @return true 表示当前行已被识别为边
     */
    private boolean tryParseEdge(String line, Graph graph, int lineNumber, Map<String, EdgeType> edgeTypeBySource) {
        Matcher labeled = EDGE_LABELED_PATTERN.matcher(line);
        if (labeled.matches()) {
            addEdge(graph, labeled.group(1), labeled.group(3), labeled.group(2).trim(), edgeTypeBySource);
            return true;
        }
        Matcher plain = EDGE_PLAIN_PATTERN.matcher(line);
        if (plain.matches()) {
            addEdge(graph, plain.group(1), plain.group(2), "", edgeTypeBySource);
            return true;
        }
        return false;
    }

    /**
     * 添加边并执行基础合法性校验。
     *
     * <p>校验项包括：禁止自环、边端点必须已声明、同一源节点不可混用条件边与顺序边。
     */
    private void addEdge(Graph graph,
                         String from,
                         String to,
                         String label,
                         Map<String, EdgeType> edgeTypeBySource) {
        if (from.equals(to)) {
            throw new ValidationException("禁止自环", from);
        }
        if (!graph.containsNode(from)) {
            throw new ValidationException("边起点未声明", from);
        }
        if (!graph.containsNode(to)) {
            throw new ValidationException("边终点未声明", to);
        }
        validateEdgeTypeMixing(from, label, edgeTypeBySource);
        graph.addEdge(new Edge(from, to, label));
    }

    /**
     * 校验同一源节点的边类型一致性。
     */
    private void validateEdgeTypeMixing(String from,
                                        String label,
                                        Map<String, EdgeType> edgeTypeBySource) {
        EdgeType current = label == null || label.isBlank() ? EdgeType.SEQUENTIAL : EdgeType.CONDITIONAL;
        EdgeType existing = edgeTypeBySource.get(from);
        if (existing != null && existing != current) {
            throw new ValidationException("条件边与顺序边禁止混用（节点级约束）", from);
        }
        edgeTypeBySource.put(from, current);
    }

    /**
     * 使用 Kahn 算法校验图是否为 DAG。
     */
    private void validateDag(Graph graph) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();
        for (String nodeId : graph.getNodes().keySet()) {
            indegree.put(nodeId, 0);
            adjacency.put(nodeId, new ArrayList<>());
        }
        for (Edge edge : graph.getEdges()) {
            adjacency.get(edge.getFrom()).add(edge.getTo());
            indegree.put(edge.getTo(), indegree.get(edge.getTo()) + 1);
        }

        ArrayDeque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        int visited = 0;
        Set<String> seen = new HashSet<>();
        // 仅当所有节点都能被拓扑消费时，才说明不存在有向环。
        while (!queue.isEmpty()) {
            String node = queue.removeFirst();
            if (!seen.add(node)) {
                continue;
            }
            visited++;
            for (String to : adjacency.get(node)) {
                int next = indegree.get(to) - 1;
                indegree.put(to, next);
                if (next == 0) {
                    queue.addLast(to);
                }
            }
        }
        if (visited != graph.getNodes().size()) {
            throw new ValidationException("Graph 必须为有向无环图（DAG）");
        }
    }

    private enum EdgeType {
        SEQUENTIAL,
        CONDITIONAL
    }
}
