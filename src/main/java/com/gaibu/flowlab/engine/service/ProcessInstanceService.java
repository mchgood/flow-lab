package com.gaibu.flowlab.engine.service;

import com.gaibu.flowlab.engine.core.ProcessEngine;
import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import com.gaibu.flowlab.engine.repository.ProcessInstanceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 流程实例管理服务
 */
public class ProcessInstanceService {
    private final ProcessInstanceRepository repository;
    private final ProcessDefinitionService definitionService;
    private final ProcessEngine processEngine;

    public ProcessInstanceService(ProcessInstanceRepository repository,
                                   ProcessDefinitionService definitionService,
                                   ProcessEngine processEngine) {
        this.repository = repository;
        this.definitionService = definitionService;
        this.processEngine = processEngine;
    }

    /**
     * 创建流程实例
     */
    public ProcessInstance create(String processDefinitionId, String businessKey, Map<String, Object> variables) {
        // 获取流程定义
        ProcessDefinition definition = definitionService.getById(processDefinitionId);
        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + processDefinitionId);
        }

        // 创建执行上下文
        ExecutionContext context = new ExecutionContext();
        context.setId(UUID.randomUUID().toString());
        if (variables != null) {
            context.setVariables(variables);
        }

        // 创建流程实例
        ProcessInstance instance = new ProcessInstance();
        instance.setId(UUID.randomUUID().toString());
        instance.setProcessDefinitionId(processDefinitionId);
        instance.setBusinessKey(businessKey);
        instance.setStatus(ProcessInstanceStatus.RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setContext(context);

        return repository.save(instance);
    }

    /**
     * 启动流程实例
     */
    public void start(String instanceId) {
        ProcessInstance instance = repository.findById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + instanceId);
        }

        // 获取流程定义
        ProcessDefinition definition = definitionService.getById(instance.getProcessDefinitionId());
        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + instance.getProcessDefinitionId());
        }

        // 执行流程
        processEngine.execute(instance, definition.getMermaidSource());

        // 保存更新后的实例
        repository.save(instance);
    }

    /**
     * 创建并启动流程实例
     */
    public ProcessInstance createAndStart(String processDefinitionId, String businessKey, Map<String, Object> variables) {
        ProcessInstance instance = create(processDefinitionId, businessKey, variables);
        start(instance.getId());
        return instance;
    }

    /**
     * 暂停流程实例
     */
    public void suspend(String instanceId) {
        ProcessInstance instance = repository.findById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + instanceId);
        }

        if (instance.getStatus() != ProcessInstanceStatus.RUNNING) {
            throw new IllegalStateException("Only RUNNING process instances can be suspended");
        }

        instance.setStatus(ProcessInstanceStatus.SUSPENDED);
        repository.save(instance);
    }

    /**
     * 恢复流程实例
     */
    public void resume(String instanceId) {
        ProcessInstance instance = repository.findById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + instanceId);
        }

        if (instance.getStatus() != ProcessInstanceStatus.SUSPENDED) {
            throw new IllegalStateException("Only SUSPENDED process instances can be resumed");
        }

        instance.setStatus(ProcessInstanceStatus.RUNNING);
        repository.save(instance);

        // 继续执行流程
        ProcessDefinition definition = definitionService.getById(instance.getProcessDefinitionId());
        processEngine.execute(instance, definition.getMermaidSource());
        repository.save(instance);
    }

    /**
     * 终止流程实例
     */
    public void terminate(String instanceId) {
        ProcessInstance instance = repository.findById(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + instanceId);
        }

        instance.setStatus(ProcessInstanceStatus.TERMINATED);
        instance.setEndTime(LocalDateTime.now());
        repository.save(instance);
    }

    /**
     * 根据ID查询流程实例
     */
    public ProcessInstance getById(String instanceId) {
        return repository.findById(instanceId);
    }

    /**
     * 根据流程定义ID查询流程实例
     */
    public List<ProcessInstance> listByDefinitionId(String definitionId) {
        return repository.findByDefinitionId(definitionId);
    }

    /**
     * 根据状态查询流程实例
     */
    public List<ProcessInstance> listByStatus(ProcessInstanceStatus status) {
        return repository.findByStatus(status);
    }

    /**
     * 查询所有流程实例
     */
    public List<ProcessInstance> listAll() {
        return repository.findAll();
    }
}
