package com.gaibu.flowlab.engine.trace;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 轨迹 Mermaid 渲染器。
 */
public class ExecutionTraceMermaidRenderer {

    /**
     * 轨迹存储。
     */
    private final ExecutionTraceStore traceStore;

    public ExecutionTraceMermaidRenderer(ExecutionTraceStore traceStore) {
        this.traceStore = traceStore;
    }

    /**
     * 渲染实例执行链路图。
     *
     * @param instanceId 实例 ID
     * @return Mermaid flowchart 文本
     */
    public String render(String instanceId) {
        List<TraceStep> steps = traceStore.getByInstanceId(instanceId);
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD\n");

        Map<String, String> nodeAlias = new LinkedHashMap<>();
        Set<String> edges = new LinkedHashSet<>();

        for (TraceStep step : steps) {
            String fromAlias = alias(step.getFromNodeId(), nodeAlias);
            if (step.getToNodeIds().isEmpty()) {
                if (!step.isSuccess()) {
                    String failNode = "FAIL_" + step.getFromNodeId();
                    String failAlias = alias(failNode, nodeAlias);
                    edges.add(fromAlias + " --> " + failAlias);
                }
                continue;
            }
            for (String to : step.getToNodeIds()) {
                String toAlias = alias(to, nodeAlias);
                edges.add(fromAlias + " --> " + toAlias);
            }
        }

        for (Map.Entry<String, String> entry : nodeAlias.entrySet()) {
            sb.append(entry.getValue()).append("[").append(quote(entry.getKey())).append("]\n");
        }
        for (String edge : edges) {
            sb.append(edge).append("\n");
        }
        return sb.toString();
    }

    private String alias(String nodeId, Map<String, String> nodeAlias) {
        return nodeAlias.computeIfAbsent(nodeId, key -> "N" + (nodeAlias.size() + 1));
    }

    private String quote(String text) {
        return "\"" + text.replace("\"", "'") + "\"";
    }
}
