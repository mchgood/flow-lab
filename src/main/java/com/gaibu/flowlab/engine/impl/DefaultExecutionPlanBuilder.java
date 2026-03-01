package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.ExecutionPlanBuilder;
import com.gaibu.flowlab.engine.model.ExecutionPlan;
import com.gaibu.flowlab.engine.model.ExecutionUnit;
import com.gaibu.flowlab.engine.model.enums.ExecutionUnitType;
import com.gaibu.flowlab.parser.api.model.Edge;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.GroupMeta;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认执行计划构建器。
 */
public class DefaultExecutionPlanBuilder implements ExecutionPlanBuilder {

    public static final String ROOT_UNIT_ID = "__ROOT__";

    @Override
    /**
     * 执行build并返回结果。
     * @return 执行结果
     */
    public ExecutionPlan build(WorkflowDefinition definition) {
        Graph graph = definition.getGraph();
        Map<String, ExecutionUnit> unitMap = new LinkedHashMap<>();

        // 这里只用入度识别启动节点，不把拓扑顺序当作执行顺序。
        Map<String, Integer> indegree = new HashMap<>();
        for (String nodeId : graph.getNodes().keySet()) {
            indegree.put(nodeId, 0);
        }
        for (Edge edge : graph.getEdges()) {
            indegree.put(edge.getTo(), indegree.get(edge.getTo()) + 1);
        }

        ExecutionUnit root = new ExecutionUnit(ROOT_UNIT_ID, ExecutionUnitType.ROOT);
        List<String> roots = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                roots.add(entry.getKey());
            }
        }
        // 虚拟根只用于调度入口聚合，不参与业务节点执行。
        root.setChildren(roots);
        unitMap.put(ROOT_UNIT_ID, root);

        for (String nodeId : graph.getNodes().keySet()) {
            ExecutionUnitType unitType = definition.getMeta().getSubflows().containsKey(nodeId)
                    ? ExecutionUnitType.SUBFLOW
                    : ExecutionUnitType.NODE;
            unitMap.put(nodeId, new ExecutionUnit(nodeId, unitType));
        }

        for (GroupMeta groupMeta : definition.getMeta().getGroupMeta().values()) {
            String groupUnitId = groupUnitId(groupMeta.getGroupId());
            ExecutionUnit groupUnit = new ExecutionUnit(groupUnitId, ExecutionUnitType.PARALLEL_GROUP);
            groupUnit.setChildren(groupMeta.getNodeIds());
            groupUnit.setAttributes(groupMeta.getAttributes());
            unitMap.put(groupUnitId, groupUnit);
        }

        return new ExecutionPlan(definition.getId(), ROOT_UNIT_ID, unitMap);
    }

    /**
     * 执行groupUnitId并返回结果。
     * @return 执行结果
     */
    private String groupUnitId(String groupId) {
        return "__GROUP__:" + groupId;
    }
}
