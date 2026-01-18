package com.gaibu.flowlab.engine;

import com.gaibu.flowlab.engine.core.ProcessEngine;
import com.gaibu.flowlab.engine.event.*;
import com.gaibu.flowlab.engine.executor.*;
import com.gaibu.flowlab.engine.expression.SpELExpressionEngine;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import com.gaibu.flowlab.engine.repository.ProcessDefinitionRepository;
import com.gaibu.flowlab.engine.repository.ProcessInstanceRepository;
import com.gaibu.flowlab.engine.service.ProcessDefinitionService;
import com.gaibu.flowlab.engine.service.ProcessInstanceService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程引擎使用示例 - 演示订单审批流程
 */
public class ProcessEngineExample {

    public static void main(String[] args) {
        System.out.println("=== 流程引擎示例：订单审批流程 ===\n");

        // 1. 初始化流程引擎组件
        System.out.println("1. 初始化流程引擎组件...");

        // 创建节点执行器
        List<NodeExecutor> executors = Arrays.asList(
                new StartNodeExecutor(),
                new TaskNodeExecutor(),
                new DecisionNodeExecutor(),
                new EndNodeExecutor()
        );
        NodeExecutorRegistry executorRegistry = new NodeExecutorRegistry(executors);

        // 创建表达式引擎
        SpELExpressionEngine expressionEngine = new SpELExpressionEngine();

        // 创建事件监听器
        List<EventListener> listeners = Arrays.asList(
                new ProcessLogListener(),
                new NodeLogListener()
        );
        EventPublisher eventPublisher = new EventPublisher(listeners);

        // 创建流程执行引擎
        ProcessEngine processEngine = new ProcessEngine(executorRegistry, expressionEngine, eventPublisher);

        // 创建存储仓库
        ProcessDefinitionRepository definitionRepository = new ProcessDefinitionRepository();
        ProcessInstanceRepository instanceRepository = new ProcessInstanceRepository();

        // 创建服务
        ProcessDefinitionService definitionService = new ProcessDefinitionService(definitionRepository);
        ProcessInstanceService instanceService = new ProcessInstanceService(
                instanceRepository, definitionService, processEngine);

        System.out.println("✓ 流程引擎组件初始化完成\n");

        // 2. 创建流程定义
        System.out.println("2. 创建订单审批流程定义...");
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

        ProcessDefinition definition = definitionService.create(
                "订单审批流程",
                "订单金额超过1000需要经理审批",
                mermaidSource
        );
        System.out.println("✓ 流程定义创建成功，ID: " + definition.getId());
        System.out.println("  版本: " + definition.getVersion());
        System.out.println("  状态: " + definition.getStatus() + "\n");

        // 3. 部署流程定义
        System.out.println("3. 部署流程定义...");
        definitionService.deploy(definition.getId());
        System.out.println("✓ 流程定义部署成功，状态: ACTIVE\n");

        // 4. 创建并启动流程实例（金额 > 1000）
        System.out.println("4. 创建并启动流程实例（订单金额 1500）...");
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("orderId", "ORDER_001");
        variables1.put("amount", 1500);
        variables1.put("userId", "user_123");

        ProcessInstance instance1 = instanceService.createAndStart(
                definition.getId(),
                "ORDER_001",
                variables1
        );
        System.out.println("✓ 流程实例创建并执行完成");
        System.out.println("  实例ID: " + instance1.getId());
        System.out.println("  状态: " + instance1.getStatus());
        System.out.println("  最终变量: " + instance1.getContext().getAllVariables() + "\n");

        // 5. 创建并启动流程实例（金额 <= 1000）
        System.out.println("5. 创建并启动流程实例（订单金额 500）...");
        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("orderId", "ORDER_002");
        variables2.put("amount", 500);
        variables2.put("userId", "user_456");

        ProcessInstance instance2 = instanceService.createAndStart(
                definition.getId(),
                "ORDER_002",
                variables2
        );
        System.out.println("✓ 流程实例创建并执行完成");
        System.out.println("  实例ID: " + instance2.getId());
        System.out.println("  状态: " + instance2.getStatus());
        System.out.println("  最终变量: " + instance2.getContext().getAllVariables() + "\n");

        // 6. 查询流程实例
        System.out.println("6. 查询所有流程实例...");
        List<ProcessInstance> allInstances = instanceService.listAll();
        System.out.println("✓ 共有 " + allInstances.size() + " 个流程实例");
        for (ProcessInstance instance : allInstances) {
            System.out.println("  - " + instance.getId() + " [" + instance.getStatus() + "]");
        }

        System.out.println("\n=== 流程引擎示例执行完成 ===");
    }

    /**
     * 流程日志监听器 - 监听流程启动和完成事件
     */
    static class ProcessLogListener implements EventListener<ProcessStartedEvent> {
        @Override
        public void onEvent(ProcessStartedEvent event) {
            System.out.println("  [事件] 流程启动: " + event.getProcessInstanceId());
            System.out.println("    初始变量: " + event.getInitialVariables());
        }

        @Override
        public Class<ProcessStartedEvent> getSupportedEventType() {
            return ProcessStartedEvent.class;
        }
    }

    /**
     * 节点日志监听器 - 监听节点开始和完成事件
     */
    static class NodeLogListener implements EventListener<NodeStartedEvent> {
        @Override
        public void onEvent(NodeStartedEvent event) {
            System.out.println("  [事件] 节点开始: " + event.getNodeId() + " (" + event.getNodeLabel() + ")");
        }

        @Override
        public Class<NodeStartedEvent> getSupportedEventType() {
            return NodeStartedEvent.class;
        }
    }
}
