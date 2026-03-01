package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.model.enums.ExecutionUnitType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行单元定义。
 */
@Getter
@Setter
public class ExecutionUnit {

    /**
     * 执行单元 id。
     */
    private String id;
    /**
     * 执行单元类型（普通节点/子流程/并行组/根节点）。
     */
    private ExecutionUnitType type;
    /**
     * 下游执行单元 id 列表。
     */
    private List<String> children = new ArrayList<>();
    /**
     * 执行单元附加属性（如 nodeId、groupId 等）。
     */
    private Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * 构造ExecutionUnit实例。
     */
    public ExecutionUnit() {
    }

    /**
     * 构造ExecutionUnit实例。
     */
    public ExecutionUnit(String id, ExecutionUnitType type) {
        this.id = id;
        this.type = type;
    }

}
