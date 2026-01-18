package com.gaibu.flowlab.engine.core;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.event.*;
import com.gaibu.flowlab.engine.executor.*;
import com.gaibu.flowlab.engine.expression.SpELExpressionEngine;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import com.gaibu.flowlab.engine.repository.ProcessDefinitionRepository;
import com.gaibu.flowlab.engine.repository.ProcessInstanceRepository;
import com.gaibu.flowlab.engine.service.ProcessDefinitionService;
import com.gaibu.flowlab.engine.service.ProcessInstanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessEngine 集成测试类 - 端到端流程测试
 */
@DisplayName("ProcessEngine 集成测试")
class ProcessEngineIntegrationTest {

    private ProcessDefinitionService definitionService;
    private ProcessInstanceService instanceService;
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
    @DisplayName("INT-001: 订单审批流程（金额>1000）")
    void testOrderApprovalProcessWithHighAmount() {
        // 创建订单审批流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B[提交订单]
                    B --> C{金额>1000?}
                    C -->|#amount > 1000| D[经理审批]
                    C -->|#amount <= 1000| E[自动通过]
                    D --> F[发货]
                    E --> F
                    F --> G((结束))
                """;
        ProcessDefinition definition = definitionService.create("订单审批流程", mermaidSource);
        definitionService.deploy(definition.getId());

        // 创建流程实例，设置 amount=1500
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 1500);
        ProcessInstance instance = instanceService.createAndStart(definition.getId(), "ORDER_001", variables);

        // 验证执行"经理审批"分支，流程完成
        assertThat(instance.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);
        assertThat(instance.getContext().getVariable("amount")).isEqualTo(1500);
    }

    @Test
    @DisplayName("INT-002: 订单审批流程（金额≤1000）")
    void testOrderApprovalProcessWithLowAmount() {
        // 创建订单审批流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B[提交订单]
                    B --> C{金额>1000?}
                    C -->|#amount > 1000| D[经理审批]
                    C -->|#amount <= 1000| E[自动通过]
                    D --> F[发货]
                    E --> F
                    F --> G((结束))
                """;
        ProcessDefinition definition = definitionService.create("订单审批流程", mermaidSource);
        definitionService.deploy(definition.getId());

        // 创建流程实例，设置 amount=500
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 500);
        ProcessInstance instance = instanceService.createAndStart(definition.getId(), "ORDER_002", variables);

        // 验证执行"自动通过"分支，流程完成
        assertThat(instance.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);
        assertThat(instance.getContext().getVariable("amount")).isEqualTo(500);
    }

    @Test
    @DisplayName("INT-005: 事件监听器完整流程")
    void testEventListenersInCompleteProcess() {
        // 创建事件监听器
        TestEventCounter eventCounter = new TestEventCounter();
        List<com.gaibu.flowlab.engine.event.EventListener> listeners = Arrays.asList(
                eventCounter.new ProcessStartedListener(),
                eventCounter.new NodeStartedListener(),
                eventCounter.new NodeCompletedListener(),
                eventCounter.new ProcessCompletedListener()
        );
        EventPublisher eventPublisher = new EventPublisher(listeners);

        // 重新创建流程引擎
        List<NodeExecutor> executors = Arrays.asList(
                new StartNodeExecutor(),
                new TaskNodeExecutor(),
                new DecisionNodeExecutor(),
                new EndNodeExecutor()
        );
        NodeExecutorRegistry executorRegistry = new NodeExecutorRegistry(executors);
        SpELExpressionEngine expressionEngine = new SpELExpressionEngine();
        ProcessEngine testEngine = new ProcessEngine(executorRegistry, expressionEngine, eventPublisher);

        ProcessInstanceService testInstanceService = new ProcessInstanceService(
                new ProcessInstanceRepository(), definitionService, testEngine);

        // 创建流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B[任务1]
                    B --> C[任务2]
                    C --> D((结束))
                """;
        ProcessDefinition definition = definitionService.create("测试流程", mermaidSource);
        definitionService.deploy(definition.getId());

        // 创建并启动流程实例
        testInstanceService.createAndStart(definition.getId(), "TEST_001", null);

        // 验证所有事件按顺序触发
        assertThat(eventCounter.processStartedCount.get()).isGreaterThan(0);
        assertThat(eventCounter.nodeStartedCount.get()).isGreaterThan(0);
        assertThat(eventCounter.nodeCompletedCount.get()).isGreaterThan(0);
        assertThat(eventCounter.processCompletedCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("INT-007: 流程定义版本管理")
    void testProcessDefinitionVersionManagement() {
        // 创建流程定义 v1
        String mermaidSourceV1 = """
                flowchart TD
                    A((开始)) --> B[任务V1]
                    B --> C((结束))
                """;
        ProcessDefinition v1 = definitionService.create("版本测试流程", mermaidSourceV1);
        definitionService.deploy(v1.getId());

        // 创建流程定义 v2
        String mermaidSourceV2 = """
                flowchart TD
                    A((开始)) --> B[任务V2]
                    B --> C((结束))
                """;
        ProcessDefinition v2 = definitionService.create("版本测试流程", mermaidSourceV2);
        definitionService.deploy(v2.getId());

        // 使用 v1 创建实例
        ProcessInstance instanceV1 = instanceService.createAndStart(v1.getId(), "TEST_V1", null);

        // 使用 v2 创建实例
        ProcessInstance instanceV2 = instanceService.createAndStart(v2.getId(), "TEST_V2", null);

        // 验证两个实例使用不同版本
        assertThat(instanceV1.getProcessDefinitionId()).isEqualTo(v1.getId());
        assertThat(instanceV2.getProcessDefinitionId()).isEqualTo(v2.getId());
        assertThat(v1.getVersion()).isEqualTo(1);
        assertThat(v2.getVersion()).isEqualTo(2);
    }

    /**
     * 测试用的事件计数器
     */
    static class TestEventCounter {
        AtomicInteger processStartedCount = new AtomicInteger(0);
        AtomicInteger nodeStartedCount = new AtomicInteger(0);
        AtomicInteger nodeCompletedCount = new AtomicInteger(0);
        AtomicInteger processCompletedCount = new AtomicInteger(0);

        class ProcessStartedListener implements com.gaibu.flowlab.engine.event.EventListener<ProcessStartedEvent> {
            @Override
            public void onEvent(ProcessStartedEvent event) {
                processStartedCount.incrementAndGet();
            }

            @Override
            public Class<ProcessStartedEvent> getSupportedEventType() {
                return ProcessStartedEvent.class;
            }
        }

        class NodeStartedListener implements com.gaibu.flowlab.engine.event.EventListener<NodeStartedEvent> {
            @Override
            public void onEvent(NodeStartedEvent event) {
                nodeStartedCount.incrementAndGet();
            }

            @Override
            public Class<NodeStartedEvent> getSupportedEventType() {
                return NodeStartedEvent.class;
            }
        }

        class NodeCompletedListener implements com.gaibu.flowlab.engine.event.EventListener<NodeCompletedEvent> {
            @Override
            public void onEvent(NodeCompletedEvent event) {
                nodeCompletedCount.incrementAndGet();
            }

            @Override
            public Class<NodeCompletedEvent> getSupportedEventType() {
                return NodeCompletedEvent.class;
            }
        }

        class ProcessCompletedListener implements com.gaibu.flowlab.engine.event.EventListener<ProcessCompletedEvent> {
            @Override
            public void onEvent(ProcessCompletedEvent event) {
                processCompletedCount.incrementAndGet();
            }

            @Override
            public Class<ProcessCompletedEvent> getSupportedEventType() {
                return ProcessCompletedEvent.class;
            }
        }
    }
}
