package com.gaibu.flowlab.engine.service;

import com.gaibu.flowlab.engine.core.ProcessEngine;
import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.event.EventPublisher;
import com.gaibu.flowlab.engine.executor.*;
import com.gaibu.flowlab.engine.expression.SpELExpressionEngine;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import com.gaibu.flowlab.engine.repository.ProcessDefinitionRepository;
import com.gaibu.flowlab.engine.repository.ProcessInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProcessInstanceService 测试类
 */
@DisplayName("ProcessInstanceService 测试")
class ProcessInstanceServiceTest {

    private ProcessInstanceService instanceService;
    private ProcessDefinitionService definitionService;
    private ProcessEngine processEngine;

    @BeforeEach
    void setUp() {
        // 创建仓库
        ProcessDefinitionRepository definitionRepository = new ProcessDefinitionRepository();
        ProcessInstanceRepository instanceRepository = new ProcessInstanceRepository();

        // 创建流程引擎组件
        List<NodeExecutor> executors = Arrays.asList(
                new StartNodeExecutor(),
                new TaskNodeExecutor(),
                new DecisionNodeExecutor(),
                new EndNodeExecutor()
        );
        NodeExecutorRegistry executorRegistry = new NodeExecutorRegistry(executors);
        SpELExpressionEngine expressionEngine = new SpELExpressionEngine();
        EventPublisher eventPublisher = new EventPublisher(new ArrayList<>());
        processEngine = new ProcessEngine(executorRegistry, expressionEngine, eventPublisher);

        // 创建服务
        definitionService = new ProcessDefinitionService(definitionRepository);
        instanceService = new ProcessInstanceService(instanceRepository, definitionService, processEngine);
    }

    @Test
    @DisplayName("PIS-001: 创建流程实例")
    void testCreateProcessInstance() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();

        // 创建流程实例
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", "ORDER_001");
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", variables);

        // 验证流程实例创建成功，状态为 RUNNING
        assertThat(instance).isNotNull();
        assertThat(instance.getId()).isNotNull();
        assertThat(instance.getProcessDefinitionId()).isEqualTo(definition.getId());
        assertThat(instance.getBusinessKey()).isEqualTo("ORDER_001");
        assertThat(instance.getStatus()).isEqualTo(ProcessInstanceStatus.RUNNING);
        assertThat(instance.getContext().getVariable("orderId")).isEqualTo("ORDER_001");
    }

    @Test
    @DisplayName("PIS-002: 创建流程实例时流程定义不存在")
    void testCreateProcessInstanceWithNonExistentDefinition() {
        // 使用不存在的流程定义ID
        assertThatThrownBy(() -> instanceService.create("non-existent-id", "ORDER_001", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Process definition not found");
    }

    @Test
    @DisplayName("PIS-003: 创建流程实例并设置初始变量")
    void testCreateProcessInstanceWithInitialVariables() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();

        // 创建流程实例，设置变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("orderId", "ORDER_001");
        variables.put("amount", 1500);
        variables.put("userId", "user_123");
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", variables);

        // 验证上下文包含设置的变量
        assertThat(instance.getContext().getVariable("orderId")).isEqualTo("ORDER_001");
        assertThat(instance.getContext().getVariable("amount")).isEqualTo(1500);
        assertThat(instance.getContext().getVariable("userId")).isEqualTo("user_123");
    }

    @Test
    @DisplayName("PIS-004: 启动流程实例")
    void testStartProcessInstance() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();
        definitionService.deploy(definition.getId());

        // 创建流程实例
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", null);

        // 启动流程实例
        instanceService.start(instance.getId());

        // 验证流程执行完成
        ProcessInstance started = instanceService.getById(instance.getId());
        assertThat(started.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);
    }

    @Test
    @DisplayName("PIS-005: 创建并启动流程实例")
    void testCreateAndStartProcessInstance() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();
        definitionService.deploy(definition.getId());

        // 创建并启动流程实例
        ProcessInstance instance = instanceService.createAndStart(definition.getId(), "ORDER_001", null);

        // 验证流程实例创建并执行完成
        assertThat(instance).isNotNull();
        assertThat(instance.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);
    }

    @Test
    @DisplayName("PIS-006: 暂停流程实例")
    void testSuspendProcessInstance() {
        // 创建流程实例
        ProcessDefinition definition = createSimpleProcessDefinition();
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", null);

        // 暂停流程实例
        instanceService.suspend(instance.getId());

        // 验证状态变为 SUSPENDED
        ProcessInstance suspended = instanceService.getById(instance.getId());
        assertThat(suspended.getStatus()).isEqualTo(ProcessInstanceStatus.SUSPENDED);
    }

    @Test
    @DisplayName("PIS-007: 暂停非运行中的流程实例")
    void testSuspendNonRunningProcessInstance() {
        // 创建并完成流程实例
        ProcessDefinition definition = createSimpleProcessDefinition();
        definitionService.deploy(definition.getId());
        ProcessInstance instance = instanceService.createAndStart(definition.getId(), "ORDER_001", null);

        // 尝试暂停已完成的流程实例
        assertThatThrownBy(() -> instanceService.suspend(instance.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only RUNNING process instances can be suspended");
    }

    @Test
    @DisplayName("PIS-010: 终止流程实例")
    void testTerminateProcessInstance() {
        // 创建流程实例
        ProcessDefinition definition = createSimpleProcessDefinition();
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", null);

        // 终止流程实例
        instanceService.terminate(instance.getId());

        // 验证状态变为 TERMINATED，设置结束时间
        ProcessInstance terminated = instanceService.getById(instance.getId());
        assertThat(terminated.getStatus()).isEqualTo(ProcessInstanceStatus.TERMINATED);
        assertThat(terminated.getEndTime()).isNotNull();
    }

    @Test
    @DisplayName("PIS-011: 根据ID查询流程实例")
    void testGetProcessInstanceById() {
        // 创建流程实例
        ProcessDefinition definition = createSimpleProcessDefinition();
        ProcessInstance instance = instanceService.create(definition.getId(), "ORDER_001", null);

        // 根据ID查询
        ProcessInstance found = instanceService.getById(instance.getId());

        // 验证返回正确的流程实例
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(instance.getId());
    }

    @Test
    @DisplayName("PIS-012: 根据流程定义ID查询流程实例")
    void testListProcessInstancesByDefinitionId() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();

        // 创建多个流程实例
        instanceService.create(definition.getId(), "ORDER_001", null);
        instanceService.create(definition.getId(), "ORDER_002", null);
        instanceService.create(definition.getId(), "ORDER_003", null);

        // 根据流程定义ID查询
        List<ProcessInstance> instances = instanceService.listByDefinitionId(definition.getId());

        // 验证返回该流程定义的所有实例
        assertThat(instances).hasSize(3);
    }

    @Test
    @DisplayName("PIS-013: 根据状态查询流程实例")
    void testListProcessInstancesByStatus() {
        // 创建流程定义
        ProcessDefinition definition = createSimpleProcessDefinition();

        // 创建不同状态的流程实例
        ProcessInstance instance1 = instanceService.create(definition.getId(), "ORDER_001", null);
        ProcessInstance instance2 = instanceService.create(definition.getId(), "ORDER_002", null);
        instanceService.suspend(instance2.getId());

        // 根据状态查询
        List<ProcessInstance> runningInstances = instanceService.listByStatus(ProcessInstanceStatus.RUNNING);
        List<ProcessInstance> suspendedInstances = instanceService.listByStatus(ProcessInstanceStatus.SUSPENDED);

        // 验证返回指定状态的所有实例
        assertThat(runningInstances).hasSize(1);
        assertThat(suspendedInstances).hasSize(1);
    }

    /**
     * 创建简单的流程定义
     */
    private ProcessDefinition createSimpleProcessDefinition() {
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B[任务]
                    B --> C((结束))
                """;
        return definitionService.create("测试流程", mermaidSource);
    }
}
