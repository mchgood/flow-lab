package com.gaibu.flowlab.engine.repository;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.model.ProcessInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 流程实例内存存储仓库
 */
public class ProcessInstanceRepository {
    private final Map<String, ProcessInstance> storage = new ConcurrentHashMap<>();

    /**
     * 保存流程实例
     */
    public ProcessInstance save(ProcessInstance instance) {
        storage.put(instance.getId(), instance);
        return instance;
    }

    /**
     * 根据ID查找流程实例
     */
    public ProcessInstance findById(String id) {
        return storage.get(id);
    }

    /**
     * 根据流程定义ID查找流程实例
     */
    public List<ProcessInstance> findByDefinitionId(String definitionId) {
        return storage.values().stream()
                .filter(instance -> instance.getProcessDefinitionId().equals(definitionId))
                .collect(Collectors.toList());
    }

    /**
     * 根据业务键查找流程实例
     */
    public ProcessInstance findByBusinessKey(String businessKey) {
        return storage.values().stream()
                .filter(instance -> businessKey.equals(instance.getBusinessKey()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据状态查找流程实例
     */
    public List<ProcessInstance> findByStatus(ProcessInstanceStatus status) {
        return storage.values().stream()
                .filter(instance -> instance.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 查找所有流程实例
     */
    public List<ProcessInstance> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * 删除流程实例
     */
    public void deleteById(String id) {
        storage.remove(id);
    }
}
