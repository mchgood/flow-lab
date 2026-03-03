package com.gaibu.flowlab.engine.store;

import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;

/**
 * 流程定义存储接口。
 */
public interface ProcessDefinitionStore {

    /**
     * 注册流程定义。
     *
     * @param definition 流程定义
     */
    void put(ProcessDefinition definition);

    /**
     * 读取流程定义。
     *
     * @param processId 流程定义 ID
     * @return 流程定义，不存在返回 null
     */
    ProcessDefinition get(String processId);
}
