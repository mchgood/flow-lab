package com.gaibu.flowlab.engine.enums;

/**
 * 流程定义状态枚举
 */
public enum ProcessDefinitionStatus {
    /**
     * 草稿状态 - 流程定义已创建但未部署
     */
    DRAFT,

    /**
     * 激活状态 - 流程定义已部署，可以创建实例
     */
    ACTIVE,

    /**
     * 归档状态 - 流程定义已归档，不能创建新实例
     */
    ARCHIVED
}
