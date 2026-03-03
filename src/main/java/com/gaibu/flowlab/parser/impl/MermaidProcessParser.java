package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.parser.ProcessParser;
import com.gaibu.flowlab.parser.exception.DefinitionException;
import com.gaibu.flowlab.parser.model.entity.Edge;
import com.gaibu.flowlab.parser.model.entity.Node;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import com.gaibu.flowlab.parser.model.enums.GatewayType;
import com.gaibu.flowlab.parser.model.enums.NodeType;
import com.gaibu.flowlab.parser.rule.MermaidParsingRules;
import com.gaibu.flowlab.parser.rule.MermaidParsingRules.AnnotationMatch;
import com.gaibu.flowlab.parser.rule.MermaidParsingRules.EdgeMatch;
import com.gaibu.flowlab.parser.rule.MermaidParsingRules.NodeTokenMatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mermaid Flowchart 解析器。
 *
 * <p>职责包括：节点/连线提取、注释增强提取、索引构建和核心约束校验。
 */
public class MermaidProcessParser implements ProcessParser {

    /**
     * 解析完整 Mermaid 文本并返回定义对象。
     */
    @Override
    public ProcessDefinition parse(String processId, String dsl) {
        if (processId == null || processId.isBlank()) {
            throw new DefinitionException("processId is blank.");
        }
        if (dsl == null || dsl.isBlank()) {
            throw new DefinitionException("DSL is blank.");
        }

        ProcessDefinition definition = new ProcessDefinition();
        definition.setId(processId.trim());

        // 暂存注释增强，等节点实际创建后再统一回填，避免前置声明顺序依赖。
        Map<String, Map<String, Object>> pendingNodeMetadata = new LinkedHashMap<>();
        Map<String, Map<String, Object>> pendingScopeMetadata = new LinkedHashMap<>();
        AtomicInteger edgeCounter = new AtomicInteger(1);

        // 主解析循环：按行处理 Mermaid 文本。
        String[] lines = dsl.split("\\R");
        for (int lineNo = 0; lineNo < lines.length; lineNo++) {
            String line = sanitizeLine(lines[lineNo]);
            if (line.isEmpty() || MermaidParsingRules.isMermaidHeader(line)) {
                continue;
            }
            // 优先处理注释增强，命中后不进入节点/连线解析。
            if (applyAnnotation(line, pendingNodeMetadata, pendingScopeMetadata)) {
                continue;
            }
            // 根据是否包含箭头决定解析连线或独立节点。
            if (line.contains("-->")) {
                parseEdgeLine(line, definition, pendingNodeMetadata, pendingScopeMetadata, edgeCounter, lineNo + 1);
            } else {
                parseStandaloneNodeLine(line, definition, pendingNodeMetadata, pendingScopeMetadata, lineNo + 1);
            }
        }

        // 解析完成后统一回填元数据、构建邻接索引并执行定义级约束校验。
        applyPendingMetadata(definition, pendingNodeMetadata, pendingScopeMetadata);
        buildIndexes(definition);
        validate(definition);
        return definition;
    }

    @Override
    public void validate(ProcessDefinition definition) {
        if (definition == null) {
            throw new DefinitionException("definition is null.");
        }
        validateDefinition(definition);
    }

    private boolean applyAnnotation(
            String line,
            Map<String, Map<String, Object>> pendingNodeMetadata,
            Map<String, Map<String, Object>> pendingScopeMetadata) {
        // 节点增强：如 timeout/retry/async。
        AnnotationMatch nodeMatch = MermaidParsingRules.matchNodeAnnotation(line).orElse(null);
        if (nodeMatch != null) {
            Map<String, Object> attrs = MermaidParsingRules.parseAttributes(nodeMatch.attributesRaw());
            pendingNodeMetadata.computeIfAbsent(nodeMatch.id(), k -> new LinkedHashMap<>()).putAll(attrs);
            return true;
        }

        // Scope 增强：属性统一加 scope. 前缀，避免与节点属性冲突。
        AnnotationMatch scopeMatch = MermaidParsingRules.matchScopeAnnotation(line).orElse(null);
        if (scopeMatch != null) {
            Map<String, Object> attrs = MermaidParsingRules.parseAttributes(scopeMatch.attributesRaw());
            Map<String, Object> prefixed = new LinkedHashMap<>();
            attrs.forEach((k, v) -> prefixed.put("scope." + k, v));
            pendingScopeMetadata.computeIfAbsent(scopeMatch.id(), k -> new LinkedHashMap<>()).putAll(prefixed);
            return true;
        }

        return false;
    }

    private void parseEdgeLine(
            String line,
            ProcessDefinition definition,
            Map<String, Map<String, Object>> pendingNodeMetadata,
            Map<String, Map<String, Object>> pendingScopeMetadata,
            AtomicInteger edgeCounter,
            int lineNo) {
        // 先通过规则层提取 source/label/target 三段结构。
        EdgeMatch edgeMatch = MermaidParsingRules.matchEdge(line).orElse(null);
        if (edgeMatch == null) {
            throw new DefinitionException("Invalid edge syntax at line " + lineNo + ": " + line);
        }

        // 解析边端点时会按需创建节点，支持“边先于节点定义”的写法。
        Node sourceNode = parseOrGetNode(edgeMatch.sourceToken(), definition, pendingNodeMetadata, pendingScopeMetadata, lineNo);
        Node targetNode = parseOrGetNode(edgeMatch.targetToken(), definition, pendingNodeMetadata, pendingScopeMetadata, lineNo);

        // 构建边并解析 label 语义（default / event / condition）。
        Edge edge = new Edge();
        edge.setId("E" + edgeCounter.getAndIncrement());
        edge.setSourceRef(sourceNode.getId());
        edge.setTargetRef(targetNode.getId());
        parseEdgeLabel(edgeMatch.label(), edge);

        definition.getEdges().put(edge.getId(), edge);
    }

    private void parseStandaloneNodeLine(
            String line,
            ProcessDefinition definition,
            Map<String, Map<String, Object>> pendingNodeMetadata,
            Map<String, Map<String, Object>> pendingScopeMetadata,
            int lineNo) {
        parseOrGetNode(line, definition, pendingNodeMetadata, pendingScopeMetadata, lineNo);
    }

    private Node parseOrGetNode(
            String token,
            ProcessDefinition definition,
            Map<String, Map<String, Object>> pendingNodeMetadata,
            Map<String, Map<String, Object>> pendingScopeMetadata,
            int lineNo) {
        ParsedNode parsed = parseNodeToken(token, lineNo);
        Node existing = definition.getNodes().get(parsed.id());
        if (existing != null) {
            // 节点曾以“纯 ID”占位时，允许被后续更具体语法升级。
            if (existing.getType() == NodeType.TASK && parsed.type() != NodeType.TASK) {
                existing.setType(parsed.type());
                existing.setGatewayType(parsed.gatewayType());
            }
            return existing;
        }

        // 首次出现时创建节点并尽早附加暂存元数据。
        Node node = new Node(parsed.id(), parsed.type());
        node.setGatewayType(parsed.gatewayType());

        Map<String, Object> nodeAttrs = pendingNodeMetadata.get(parsed.id());
        if (nodeAttrs != null) {
            node.getMetadata().putAll(nodeAttrs);
        }
        Map<String, Object> scopeAttrs = pendingScopeMetadata.get(parsed.id());
        if (scopeAttrs != null) {
            node.getMetadata().putAll(scopeAttrs);
        }

        definition.getNodes().put(node.getId(), node);
        return node;
    }

    private ParsedNode parseNodeToken(String token, int lineNo) {
        String t = token.trim();
        if (t.isEmpty()) {
            throw new DefinitionException("Empty node token at line " + lineNo + ".");
        }

        // 节点结构匹配全部由规则层提供，Parser 只做语义映射。
        NodeTokenMatch tokenMatch = MermaidParsingRules.matchNodeToken(t).orElse(null);
        if (tokenMatch == null) {
            throw new DefinitionException("Unsupported node token at line " + lineNo + ": " + token);
        }

        return switch (tokenMatch.kind()) {
            case SUB_PROCESS -> new ParsedNode(tokenMatch.id(), NodeType.SUB_PROCESS, null);
            case TASK, PLAIN -> new ParsedNode(tokenMatch.id(), NodeType.TASK, null);
            case GATEWAY -> new ParsedNode(tokenMatch.id(), NodeType.GATEWAY, parseGatewayType(tokenMatch.content(), lineNo));
            case ROUND -> new ParsedNode(tokenMatch.id(), parseStartEndType(tokenMatch.content(), lineNo), null);
        };
    }

    private NodeType parseStartEndType(String label, int lineNo) {
        String normalized = label.trim().toLowerCase(Locale.ROOT);
        if ("start".equals(normalized)) {
            return NodeType.START;
        }
        if ("end".equals(normalized)) {
            return NodeType.END;
        }
        throw new DefinitionException("Round node must be Start or End at line " + lineNo + ": " + label);
    }

    private GatewayType parseGatewayType(String raw, int lineNo) {
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "XOR", "EXCLUSIVE" -> GatewayType.EXCLUSIVE;
            case "AND", "PARALLEL" -> GatewayType.PARALLEL;
            case "OR", "INCLUSIVE" -> GatewayType.INCLUSIVE;
            default -> throw new DefinitionException("Unsupported gateway type at line " + lineNo + ": " + raw);
        };
    }

    private void parseEdgeLabel(String label, Edge edge) {
        if (label == null || label.isBlank()) {
            return;
        }

        String normalized = label.trim();
        // default 出边用于 XOR 回退路径。
        if ("default".equalsIgnoreCase(normalized)) {
            edge.setDefaultEdge(true);
            return;
        }

        // 其他场景统一作为条件表达式保留到边上。
        edge.setConditionExpression(normalized);
    }

    private void applyPendingMetadata(
            ProcessDefinition definition,
            Map<String, Map<String, Object>> pendingNodeMetadata,
            Map<String, Map<String, Object>> pendingScopeMetadata) {
        // 节点增强回填：注释中引用的节点必须真实存在。
        pendingNodeMetadata.forEach((nodeId, attrs) -> {
            Node node = definition.getNodes().get(nodeId);
            if (node == null) {
                throw new DefinitionException("@node annotation references unknown node: " + nodeId);
            }
            node.getMetadata().putAll(attrs);
        });

        // Scope 增强回填：仅允许作用于网关节点。
        pendingScopeMetadata.forEach((scopeId, attrs) -> {
            Node node = definition.getNodes().get(scopeId);
            if (node == null) {
                throw new DefinitionException("@scope annotation references unknown node: " + scopeId);
            }
            if (node.getType() != NodeType.GATEWAY) {
                throw new DefinitionException("@scope must target a gateway node: " + scopeId);
            }
            node.getMetadata().putAll(attrs);
        });
    }

    private void buildIndexes(ProcessDefinition definition) {
        // 先初始化每个节点的入/出边桶，保证后续读取稳定。
        for (Node node : definition.getNodes().values()) {
            definition.getOutgoingIndex().put(node.getId(), new ArrayList<>());
            definition.getIncomingIndex().put(node.getId(), new ArrayList<>());
        }
        // 再按边填充邻接索引，保持解析顺序即 DSL 顺序。
        for (Edge edge : definition.getEdges().values()) {
            List<Edge> outgoing = definition.getOutgoingIndex().get(edge.getSourceRef());
            List<Edge> incoming = definition.getIncomingIndex().get(edge.getTargetRef());
            if (outgoing == null || incoming == null) {
                throw new DefinitionException("Edge references unknown node: " + edge.getId());
            }
            outgoing.add(edge);
            incoming.add(edge);
        }
    }

    private void validateDefinition(ProcessDefinition definition) {
        // 约束顺序遵循“局部语义 -> 全局结构”，便于尽早给出可定位错误。
        validateXorDefaultOrder(definition);
        validateScopeFlowTimeoutEdge(definition);
        validateSingleStartEnd(definition);
    }

    private void validateXorDefaultOrder(ProcessDefinition definition) {
        for (Node node : definition.getNodes().values()) {
            if (node.getType() != NodeType.GATEWAY || node.getGatewayType() != GatewayType.EXCLUSIVE) {
                continue;
            }
            List<Edge> outgoing = definition.getOutgoingIndex().getOrDefault(node.getId(), List.of());
            int defaultCount = 0;
            int lastDefaultIndex = -1;
            for (int i = 0; i < outgoing.size(); i++) {
                if (outgoing.get(i).isDefaultEdge()) {
                    defaultCount++;
                    lastDefaultIndex = i;
                }
            }
            if (defaultCount > 1) {
                throw new DefinitionException("XOR gateway has multiple default edges: " + node.getId());
            }
            if (defaultCount == 1 && lastDefaultIndex != outgoing.size() - 1) {
                throw new DefinitionException("XOR default edge must be last: " + node.getId());
            }
        }
    }

    private void validateScopeFlowTimeoutEdge(ProcessDefinition definition) {
        for (Node node : definition.getNodes().values()) {
            Object strategy = node.getMetadata().get("scope.cancelStrategy");
            if (!"flow".equalsIgnoreCase(Objects.toString(strategy, ""))) {
                continue;
            }

            Object timeout = node.getMetadata().get("scope.timeout");
            if (timeout == null) {
                continue;
            }

            List<Edge> outgoing = definition.getOutgoingIndex().getOrDefault(node.getId(), List.of());
            boolean hasTimeoutEdge = outgoing.stream()
                    .anyMatch(e -> "timeout".equalsIgnoreCase(Objects.toString(e.getConditionExpression(), "")));
            if (!hasTimeoutEdge) {
                throw new DefinitionException(
                        "Scope gateway requires timeout edge when cancelStrategy=flow: " + node.getId());
            }
        }
    }

    private void validateSingleStartEnd(ProcessDefinition definition) {
        int startCount = 0;
        int endCount = 0;
        for (Node node : definition.getNodes().values()) {
            if (node.getType() == NodeType.START) {
                startCount++;
            } else if (node.getType() == NodeType.END) {
                endCount++;
            }
        }
        if (startCount != 1) {
            throw new DefinitionException("Definition must contain exactly one START node, found: " + startCount);
        }
        if (endCount < 1) {
            throw new DefinitionException("Definition must contain at least one END node.");
        }
    }

    private String sanitizeLine(String raw) {
        String line = raw == null ? "" : raw.trim();
        if (line.startsWith("```") || line.startsWith("---")) {
            return "";
        }
        return line;
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private record ParsedNode(String id, NodeType type, GatewayType gatewayType) {
    }
}
