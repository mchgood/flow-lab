package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 解析阶段最终产物。
 */
@Getter
@Setter
public class WorkflowDefinition {

    /**
     * 工作流 id。
     */
    private String id;
    /**
     * 工作流说明。
     */
    private String description;
    /**
     * 图结构定义。
     */
    private Graph graph;
    /**
     * 图语义元数据。
     */
    private GraphMeta meta;

    /**
     * 构造WorkflowDefinition实例。
     */
    public WorkflowDefinition() {
    }

    /**
     * 构造WorkflowDefinition实例。
     */
    public WorkflowDefinition(String id, String description, Graph graph, GraphMeta meta) {
        this.id = id;
        this.description = description;
        this.graph = graph;
        this.meta = meta;
    }

}
