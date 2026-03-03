package com.gaibu.flowlab.parser.rule;

import com.gaibu.flowlab.parser.rule.enums.NodeTokenKind;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mermaid 文本解析规则层。
 *
 * <p>集中管理正则表达式与匹配提取逻辑，避免在解析器实现中散落正则细节。
 */
public final class MermaidParsingRules {

    /**
     * 连线规则：`source --> target` 或 `source -->|label| target`。
     */
    private static final Pattern EDGE_PATTERN = Pattern.compile("^(.*?)-->(?:\\|(.*?)\\|)?(.*)$");

    /**
     * 节点增强注释规则：`%% @node:NodeId k=v ...`。
     */
    private static final Pattern NODE_ANNOTATION_PATTERN = Pattern.compile("^%%\\s*@node:([^\\s]+)\\s*(.*)$");

    /**
     * Scope 增强注释规则：`%% @scope:GatewayId k=v ...`。
     */
    private static final Pattern SCOPE_ANNOTATION_PATTERN = Pattern.compile("^%%\\s*@scope:([^\\s]+)\\s*(.*)$");

    /**
     * 键值对规则：匹配 `k=v` 形式的属性片段。
     */
    private static final Pattern KV_PATTERN = Pattern.compile("([A-Za-z0-9_.-]+)=([^\\s]+)");

    /**
     * 子流程节点规则：`Id[[SubProcess]]`。
     */
    private static final Pattern SUB_PROCESS_NODE_PATTERN = Pattern.compile("^([A-Za-z0-9_:-]+)\\[\\[(.+)\\]\\]$");

    /**
     * 任务节点规则：`Id[Task]`。
     */
    private static final Pattern TASK_NODE_PATTERN = Pattern.compile("^([A-Za-z0-9_:-]+)\\[(.+)\\]$");

    /**
     * 网关节点规则：`Id{XOR|AND|OR|...}`。
     */
    private static final Pattern GATEWAY_NODE_PATTERN = Pattern.compile("^([A-Za-z0-9_:-]+)\\{(.+)}$");

    /**
     * 起止节点规则：`Id(Start)` / `Id(End)`。
     */
    private static final Pattern START_END_NODE_PATTERN = Pattern.compile("^([A-Za-z0-9_:-]+)\\((.+)\\)$");

    /**
     * 纯节点 ID 规则：`Id`。
     */
    private static final Pattern PLAIN_NODE_PATTERN = Pattern.compile("^([A-Za-z0-9_:-]+)$");

    /**
     * 整数字面量规则（用于属性值类型推断）。
     */
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");

    private MermaidParsingRules() {
    }

    /**
     * 提取边定义中的 source / label / target。
     */
    public static Optional<EdgeMatch> matchEdge(String line) {
        Matcher matcher = EDGE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String sourceToken = matcher.group(1).trim();
        String label = matcher.group(2) == null ? null : matcher.group(2).trim();
        String targetToken = matcher.group(3).trim();
        return Optional.of(new EdgeMatch(sourceToken, label, targetToken));
    }

    /**
     * 匹配 `%% @node:*` 注释增强。
     */
    public static Optional<AnnotationMatch> matchNodeAnnotation(String line) {
        Matcher matcher = NODE_ANNOTATION_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new AnnotationMatch(matcher.group(1).trim(), matcher.group(2)));
    }

    /**
     * 匹配 `%% @scope:*` 注释增强。
     */
    public static Optional<AnnotationMatch> matchScopeAnnotation(String line) {
        Matcher matcher = SCOPE_ANNOTATION_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new AnnotationMatch(matcher.group(1).trim(), matcher.group(2)));
    }

    /**
     * 解析节点 token 的结构类型与内容。
     */
    public static Optional<NodeTokenMatch> matchNodeToken(String token) {
        String t = token == null ? "" : token.trim();

        Matcher matcher = SUB_PROCESS_NODE_PATTERN.matcher(t);
        if (matcher.matches()) {
            return Optional.of(new NodeTokenMatch(NodeTokenKind.SUB_PROCESS, matcher.group(1), matcher.group(2).trim()));
        }

        matcher = TASK_NODE_PATTERN.matcher(t);
        if (matcher.matches()) {
            return Optional.of(new NodeTokenMatch(NodeTokenKind.TASK, matcher.group(1), matcher.group(2).trim()));
        }

        matcher = GATEWAY_NODE_PATTERN.matcher(t);
        if (matcher.matches()) {
            return Optional.of(new NodeTokenMatch(NodeTokenKind.GATEWAY, matcher.group(1), matcher.group(2).trim()));
        }

        matcher = START_END_NODE_PATTERN.matcher(t);
        if (matcher.matches()) {
            return Optional.of(new NodeTokenMatch(NodeTokenKind.ROUND, matcher.group(1), matcher.group(2).trim()));
        }

        matcher = PLAIN_NODE_PATTERN.matcher(t);
        if (matcher.matches()) {
            return Optional.of(new NodeTokenMatch(NodeTokenKind.PLAIN, matcher.group(1), ""));
        }

        return Optional.empty();
    }

    /**
     * 解析 `k=v` 风格属性串。
     */
    public static Map<String, Object> parseAttributes(String attrsRaw) {
        if (attrsRaw == null || attrsRaw.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, Object> attrs = new LinkedHashMap<>();
        Matcher matcher = KV_PATTERN.matcher(attrsRaw);
        while (matcher.find()) {
            attrs.put(matcher.group(1).trim(), parseLiteral(matcher.group(2).trim()));
        }
        return attrs;
    }

    /**
     * 判断是否 Mermaid 图头声明。
     */
    public static boolean isMermaidHeader(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.startsWith("flowchart")
                || lower.startsWith("graph")
                || lower.startsWith("classdiagram")
                || lower.startsWith("sequencediagram");
    }

    private static Object parseLiteral(String raw) {
        if ("true".equalsIgnoreCase(raw) || "false".equalsIgnoreCase(raw)) {
            return Boolean.parseBoolean(raw);
        }
        if (INTEGER_PATTERN.matcher(raw).matches()) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException ignored) {
                return Long.parseLong(raw);
            }
        }
        return raw;
    }

    /**
     * 边匹配结果。
     */
    public record EdgeMatch(String sourceToken, String label, String targetToken) {
    }

    /**
     * 注释匹配结果。
     */
    public record AnnotationMatch(String id, String attributesRaw) {
    }

    /**
     * 节点 token 匹配结果。
     */
    public record NodeTokenMatch(NodeTokenKind kind, String id, String content) {
    }
}
