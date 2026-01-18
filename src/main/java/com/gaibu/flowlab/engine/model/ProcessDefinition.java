package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.enums.ProcessDefinitionStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程定义 - 通过 Mermaid 语法定义的流程模板
 */
@Data
public class ProcessDefinition {
    /**
     * 流程定义ID
     */
    private String id;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * Mermaid 源码
     */
    private String mermaidSource;

    /**
     * 解析后的 FlowGraph JSON
     */
    private String flowGraphJson;

    /**
     * 状态：DRAFT, ACTIVE, ARCHIVED
     */
    private ProcessDefinitionStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;
}
