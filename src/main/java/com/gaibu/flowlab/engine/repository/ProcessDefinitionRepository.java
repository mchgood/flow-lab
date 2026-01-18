package com.gaibu.flowlab.engine.repository;

import com.gaibu.flowlab.engine.model.ProcessDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 流程定义内存存储仓库
 */
public class ProcessDefinitionRepository {
    private final Map<String, ProcessDefinition> storage = new ConcurrentHashMap<>();

    /**
     * 保存流程定义
     */
    public ProcessDefinition save(ProcessDefinition definition) {
        storage.put(definition.getId(), definition);
        return definition;
    }

    /**
     * 根据ID查找流程定义
     */
    public ProcessDefinition findById(String id) {
        return storage.get(id);
    }

    /**
     * 根据名称查找流程定义
     */
    public List<ProcessDefinition> findByName(String name) {
        return storage.values().stream()
                .filter(def -> def.getName().equals(name))
                .collect(Collectors.toList());
    }

    /**
     * 查找所有流程定义
     */
    public List<ProcessDefinition> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * 删除流程定义
     */
    public void deleteById(String id) {
        storage.remove(id);
    }
}
