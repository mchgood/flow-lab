package com.gaibu.flowlab.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 执行计划。
 */
@Getter
@Setter
public class ExecutionPlan {

    /**
     * 工作流定义 id。
     */
    private String workflowId;
    /**
     * 虚拟根执行单元 id。
     */
    private String rootUnitId;
    /**
     * 执行单元表（unitId -> unit）。
     */
    private Map<String, ExecutionUnit> unitMap = new LinkedHashMap<>();

    /**
     * 构造ExecutionPlan实例。
     */
    public ExecutionPlan() {
    }

    /**
     * 构造ExecutionPlan实例。
     */
    public ExecutionPlan(String workflowId, String rootUnitId, Map<String, ExecutionUnit> unitMap) {
        this.workflowId = workflowId;
        this.rootUnitId = rootUnitId;
        this.unitMap = new LinkedHashMap<>(unitMap);
    }

    /**
     * 获取root。
     * @return root
     */
    public ExecutionUnit getRoot() {
        return unitMap.get(rootUnitId);
    }
}
