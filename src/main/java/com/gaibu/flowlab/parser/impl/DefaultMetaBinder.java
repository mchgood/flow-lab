package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.MetaBinder;
import com.gaibu.flowlab.parser.api.enums.DirectiveType;
import com.gaibu.flowlab.parser.api.enums.GroupType;
import com.gaibu.flowlab.parser.api.model.Directive;
import com.gaibu.flowlab.parser.api.model.Edge;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.GraphMeta;
import com.gaibu.flowlab.parser.api.model.GroupMeta;
import com.gaibu.flowlab.parser.api.model.NodeMeta;
import com.gaibu.flowlab.parser.api.model.SubflowMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 语义绑定实现。
 *
 * <p>该绑定器把扫描得到的 directive 归并为结构化元数据：
 * 节点级属性（超时、重试、分组）、并行组属性、子流程映射。
 */
public class DefaultMetaBinder implements MetaBinder {

    /**
     * 将图结构与 directive 绑定为 {@link GraphMeta}。
     *
     * @param graph 流程图
     * @param directives 指令列表
     * @return 绑定完成的元数据
     */
    @Override
    public GraphMeta bind(Graph graph, List<Directive> directives) {
        if (graph == null) {
            throw new ValidationException("graph 不能为空");
        }

        GraphMeta result = new GraphMeta();
        // 节点草稿：承载每个节点在绑定阶段累积的元信息。
        Map<String, NodeMetaDraft> nodeDrafts = initNodeDrafts(graph);
        // 同一节点内去重，同类型 directive 只允许出现一次。
        Map<String, Set<DirectiveType>> directiveTypesByNode = new HashMap<>();
        // 并行组上的 @parallel 指令参数（每组仅允许一个）。
        Map<String, Map<String, String>> parallelDirectiveByGroup = new LinkedHashMap<>();
        // 记录每组 @parallel 指令声明在哪个节点，便于精确报错。
        Map<String, String> parallelScopedNodeByGroup = new HashMap<>();
        // 子流程定义草稿，按节点聚合。
        Map<String, SubflowDraft> subflowDrafts = new LinkedHashMap<>();

        List<Directive> safeDirectives = directives == null ? List.of() : directives;
        for (Directive directive : safeDirectives) {
            String scopedNodeId = resolveScopedNodeId(directive);
            if (scopedNodeId == null || scopedNodeId.isBlank()) {
                throw new ValidationException("directive 缺少作用节点");
            }
            if (!graph.getNodes().containsKey(scopedNodeId)) {
                throw new ValidationException("directive 作用节点不存在", scopedNodeId);
            }

            Set<DirectiveType> seenTypes = directiveTypesByNode.computeIfAbsent(scopedNodeId, k -> new HashSet<>());
            if (!seenTypes.add(directive.getType())) {
                throw new ValidationException("同一节点同类型指令不可重复", scopedNodeId);
            }

            applyDirective(graph, directive, scopedNodeId, nodeDrafts, parallelDirectiveByGroup, parallelScopedNodeByGroup, subflowDrafts);
        }

        // 并行组需要满足“共同入口、唯一汇合点”等结构约束。
        validateParallelGroups(graph, nodeDrafts, parallelDirectiveByGroup, parallelScopedNodeByGroup);

        for (NodeMetaDraft draft : nodeDrafts.values()) {
            result.putNodeMeta(draft.nodeId, new NodeMeta(draft.nodeId, draft.timeout, draft.retry, draft.groupId));
        }
        for (Map.Entry<String, Map<String, String>> entry : parallelDirectiveByGroup.entrySet()) {
            String groupId = entry.getKey();
            Map<String, String> attrs = new LinkedHashMap<>(entry.getValue());
            List<String> nodeIds = collectGroupNodes(nodeDrafts, groupId);
            result.putGroupMeta(groupId, new GroupMeta(groupId, GroupType.PARALLEL, attrs, nodeIds));
        }
        for (SubflowDraft draft : subflowDrafts.values()) {
            result.putSubflow(draft.nodeId, new SubflowMeta(draft.nodeId, draft.referenceId, draft.inputMapping, draft.outputMapping));
        }

        return result;
    }

    /**
     * 为所有节点初始化默认元数据草稿。
     */
    private Map<String, NodeMetaDraft> initNodeDrafts(Graph graph) {
        Map<String, NodeMetaDraft> drafts = new LinkedHashMap<>();
        for (String nodeId : graph.getNodes().keySet()) {
            NodeMetaDraft draft = new NodeMetaDraft();
            draft.nodeId = nodeId;
            draft.retry = 0;
            drafts.put(nodeId, draft);
        }
        return drafts;
    }

    /**
     * 解析 directive 作用节点。
     *
     * <p>优先使用 scanner 已识别的 scopedNodeId，回退到参数中的 node 字段。
     */
    private String resolveScopedNodeId(Directive directive) {
        if (directive.getScopedNodeId() != null && !directive.getScopedNodeId().isBlank()) {
            return directive.getScopedNodeId();
        }
        return directive.getArguments().get("node");
    }

    /**
     * 按 directive 类型更新草稿数据。
     */
    private void applyDirective(Graph graph,
                                Directive directive,
                                String scopedNodeId,
                                Map<String, NodeMetaDraft> nodeDrafts,
                                Map<String, Map<String, String>> parallelDirectiveByGroup,
                                Map<String, String> parallelScopedNodeByGroup,
                                Map<String, SubflowDraft> subflowDrafts) {
        Map<String, String> args = directive.getArguments();
        NodeMetaDraft draft = nodeDrafts.get(scopedNodeId);
        switch (directive.getType()) {
            case TIMEOUT -> draft.timeout = parseTimeout(args, scopedNodeId);
            case RETRY -> draft.retry = parseRetry(args, scopedNodeId);
            case PARALLEL_GROUP -> {
                String groupId = firstNonBlank(args.get("group"), args.get("groupId"));
                if (groupId == null) {
                    throw new ValidationException("@parallel_group 缺少 group 参数", scopedNodeId);
                }
                if (draft.groupId != null && !draft.groupId.equals(groupId)) {
                    throw new ValidationException("一个节点只能属于一个并行组", scopedNodeId);
                }
                draft.groupId = groupId;
            }
            case PARALLEL -> {
                String groupId = firstNonBlank(args.get("group"), args.get("groupId"));
                if (groupId == null) {
                    throw new ValidationException("@parallel 缺少 group 参数", scopedNodeId);
                }
                if (parallelDirectiveByGroup.containsKey(groupId)) {
                    throw new ValidationException("同一并行组 parallel 指令不可重复", scopedNodeId);
                }
                Map<String, String> groupArgs = new LinkedHashMap<>(args);
                normalizeMode(groupArgs, scopedNodeId);
                parallelDirectiveByGroup.put(groupId, groupArgs);
                parallelScopedNodeByGroup.put(groupId, scopedNodeId);
            }
            case SUBFLOW -> {
                String ref = args.get("ref");
                if (ref == null || ref.isBlank()) {
                    throw new ValidationException("@subflow 缺少 ref 参数", scopedNodeId);
                }
                if (graph.getId() != null && graph.getId().equals(ref)) {
                    throw new ValidationException("子流程禁止直接自引用", scopedNodeId);
                }
                SubflowDraft subflowDraft = new SubflowDraft();
                subflowDraft.nodeId = scopedNodeId;
                subflowDraft.referenceId = ref;
                subflowDraft.inputMapping = extractMapping(args, "in.");
                subflowDraft.outputMapping = extractMapping(args, "out.");
                subflowDrafts.put(scopedNodeId, subflowDraft);
            }
            case CONDITION, CUSTOM -> {
                // 当前版本仅记录合法性，具体语义由执行层扩展。
            }
            default -> throw new ValidationException("不支持的 directive 类型", scopedNodeId);
        }
    }

    /**
     * 解析 timeout 指令，支持 `ms` 与简写时长字符串。
     */
    private Long parseTimeout(Map<String, String> args, String nodeId) {
        String ms = args.get("ms");
        String timeout = args.get("timeout");
        if ((ms == null || ms.isBlank()) && (timeout == null || timeout.isBlank())) {
            throw new ValidationException("@timeout 缺少 ms 或 timeout 参数", nodeId);
        }
        if (ms != null && !ms.isBlank()) {
            try {
                return Long.parseLong(ms);
            } catch (NumberFormatException e) {
                throw new ValidationException("@timeout ms 参数非法", nodeId);
            }
        }
        return parseDuration(timeout, nodeId);
    }

    /**
     * 解析形如 `100ms`、`5s`、`2m`、`1h` 的时长字符串。
     */
    private long parseDuration(String value, String nodeId) {
        String v = value.trim().toLowerCase();
        int i = 0;
        while (i < v.length() && Character.isDigit(v.charAt(i))) {
            i++;
        }
        if (i == 0 || i == v.length()) {
            throw new ValidationException("@timeout 格式非法", nodeId);
        }
        long amount;
        try {
            amount = Long.parseLong(v.substring(0, i));
        } catch (NumberFormatException e) {
            throw new ValidationException("@timeout 数值非法", nodeId);
        }
        String unit = v.substring(i);
        return switch (unit) {
            case "ms" -> amount;
            case "s" -> amount * 1000L;
            case "m" -> amount * 60_000L;
            case "h" -> amount * 3_600_000L;
            default -> throw new ValidationException("@timeout 单位非法", nodeId);
        };
    }

    /**
     * 解析 retry 次数，要求为非负整数。
     */
    private Integer parseRetry(Map<String, String> args, String nodeId) {
        String retry = firstNonBlank(args.get("retry"), args.get("times"));
        if (retry == null) {
            throw new ValidationException("@retry 缺少 retry 参数", nodeId);
        }
        try {
            int val = Integer.parseInt(retry);
            if (val < 0) {
                throw new ValidationException("@retry 不能为负数", nodeId);
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException("@retry 参数非法", nodeId);
        }
    }

    /**
     * 返回首个非空白字符串。
     */
    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    /**
     * 按前缀提取映射参数（例如 in./out.）。
     */
    private Map<String, String> extractMapping(Map<String, String> args, String prefix) {
        Map<String, String> mapping = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                mapping.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return mapping;
    }

    /**
     * 标准化并行模式，默认 ALL，仅允许 ALL/ANY。
     */
    private void normalizeMode(Map<String, String> groupArgs, String nodeId) {
        String mode = groupArgs.get("mode");
        if (mode == null || mode.isBlank()) {
            groupArgs.put("mode", "ALL");
            return;
        }
        String normalized = mode.trim().toUpperCase();
        if (!"ALL".equals(normalized) && !"ANY".equals(normalized)) {
            throw new ValidationException("@parallel mode 仅支持 ANY/ALL", nodeId);
        }
        groupArgs.put("mode", normalized);
    }

    /**
     * 校验并行组结构约束。
     *
     * <p>每个成员节点都必须：
     * 1) 共享相同的组外父节点集合；
     * 2) 对外只有一个流出目标；
     * 3) 所有流出目标必须汇合到同一节点。
     */
    private void validateParallelGroups(Graph graph,
                                        Map<String, NodeMetaDraft> nodeDrafts,
                                        Map<String, Map<String, String>> parallelDirectiveByGroup,
                                        Map<String, String> parallelScopedNodeByGroup) {
        for (Map.Entry<String, Map<String, String>> entry : parallelDirectiveByGroup.entrySet()) {
            String groupId = entry.getKey();
            Map<String, String> args = entry.getValue();
            List<String> groupNodes = collectGroupNodes(nodeDrafts, groupId);
            if (groupNodes.isEmpty()) {
                throw new ValidationException("并行组未绑定任何节点", parallelScopedNodeByGroup.get(groupId));
            }

            String scopedNode = parallelScopedNodeByGroup.get(groupId);
            if (!groupNodes.contains(scopedNode)) {
                throw new ValidationException("@parallel 作用节点必须属于目标并行组", scopedNode);
            }

            Set<String> expectedParents = null;
            String convergeNode = null;
            for (String nodeId : groupNodes) {
                Set<String> parents = incomingFromOutside(graph, nodeId, groupNodes);
                if (expectedParents == null) {
                    expectedParents = parents;
                } else if (!expectedParents.equals(parents)) {
                    throw new ValidationException("并行组节点父节点集合不一致", nodeId);
                }

                List<String> outsideTargets = outgoingToOutside(graph, nodeId, groupNodes);
                if (outsideTargets.size() != 1) {
                    throw new ValidationException("并行组节点必须且仅能汇合到一个公共节点", nodeId);
                }
                String target = outsideTargets.get(0);
                if (convergeNode == null) {
                    convergeNode = target;
                } else if (!convergeNode.equals(target)) {
                    throw new ValidationException("并行组汇合节点不一致", nodeId);
                }
            }

            String mode = args.get("mode");
            if ("ANY".equals(mode)) {
                String anyCompleteTo = args.get("any_complete_to");
                if (anyCompleteTo == null || anyCompleteTo.isBlank()) {
                    throw new ValidationException("ANY 模式必须声明 any_complete_to", scopedNode);
                }
                if (!anyCompleteTo.equals(convergeNode)) {
                    throw new ValidationException("any_complete_to 必须等于并行组唯一汇合节点", scopedNode);
                }
            }
        }
    }

    /**
     * 收集指定并行组内的所有节点。
     */
    private List<String> collectGroupNodes(Map<String, NodeMetaDraft> nodeDrafts, String groupId) {
        List<String> nodes = new ArrayList<>();
        for (NodeMetaDraft draft : nodeDrafts.values()) {
            if (groupId.equals(draft.groupId)) {
                nodes.add(draft.nodeId);
            }
        }
        return nodes;
    }

    /**
     * 统计组外流入到目标节点的父节点集合。
     */
    private Set<String> incomingFromOutside(Graph graph, String nodeId, List<String> groupNodes) {
        Set<String> groupSet = new HashSet<>(groupNodes);
        Set<String> parents = new HashSet<>();
        for (Edge edge : graph.getEdges()) {
            if (nodeId.equals(edge.getTo()) && !groupSet.contains(edge.getFrom())) {
                parents.add(edge.getFrom());
            }
        }
        return parents;
    }

    /**
     * 收集目标节点指向组外的后继节点。
     */
    private List<String> outgoingToOutside(Graph graph, String nodeId, List<String> groupNodes) {
        Set<String> groupSet = new HashSet<>(groupNodes);
        List<String> targets = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (nodeId.equals(edge.getFrom()) && !groupSet.contains(edge.getTo())) {
                targets.add(edge.getTo());
            }
        }
        return targets;
    }

    private static class NodeMetaDraft {
        /**
         * 节点 id。
         */
        private String nodeId;
        /**
         * 节点超时（毫秒）。
         */
        private Long timeout;
        /**
         * 节点重试次数。
         */
        private Integer retry;
        /**
         * 节点所属并行组。
         */
        private String groupId;
    }

    private static class SubflowDraft {
        /**
         * 子流程挂载节点。
         */
        private String nodeId;
        /**
         * 子流程引用 id。
         */
        private String referenceId;
        /**
         * 父流程 -> 子流程入参映射。
         */
        private Map<String, String> inputMapping;
        /**
         * 子流程 -> 父流程出参映射。
         */
        private Map<String, String> outputMapping;
    }
}
