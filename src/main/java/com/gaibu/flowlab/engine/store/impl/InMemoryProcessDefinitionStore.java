package com.gaibu.flowlab.engine.store.impl;

import com.gaibu.flowlab.engine.store.ProcessDefinitionStore;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内存版流程定义存储实现。
 */
public class InMemoryProcessDefinitionStore implements ProcessDefinitionStore {

    /**
     * 流程定义索引。
     */
    private final Map<String, ProcessDefinition> definitions = new LinkedHashMap<>();

    @Override
    public void put(ProcessDefinition definition) {
        definitions.put(definition.getId(), definition);
    }

    @Override
    public ProcessDefinition get(String processId) {
        return definitions.get(processId);
    }
}
