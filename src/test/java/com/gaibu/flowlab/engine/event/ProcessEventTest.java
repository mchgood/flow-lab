package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import com.gaibu.flowlab.engine.enums.ProcessEventType;
import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessEvent 测试类
 */
@DisplayName("ProcessEvent 测试")
class ProcessEventTest {

    private ProcessInstance instance;

    @BeforeEach
    void setUp() {
        // 创建执行上下文
        ExecutionContext context = new ExecutionContext();
        context.setId("ctx-001");
        context.setVariable("orderId", "ORDER_001");
        context.setVariable("amount", 1500);

        // 创建流程实例
        instance = new ProcessInstance();
        instance.setId("inst-001");
        instance.setProcessDefinitionId("def-001");
        instance.setBusinessKey("ORDER_001");
        instance.setStatus(ProcessInstanceStatus.RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setContext(context);
    }

    @Test
    @DisplayName("PE-001: 创建流程启动事件")
    void testCreateProcessStartedEvent() {
        // 创建流程启动事件
        ProcessStartedEvent event = new ProcessStartedEvent(instance);

        // 验证事件属性
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo("inst-001");
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getEventType()).isEqualTo(ProcessEventType.PROCESS_STARTED);
        assertThat(event.getProcessDefinitionId()).isEqualTo("def-001");
        assertThat(event.getInitialVariables()).containsEntry("orderId", "ORDER_001");
        assertThat(event.getInitialVariables()).containsEntry("amount", 1500);
    }

    @Test
    @DisplayName("PE-002: 创建流程完成事件")
    void testCreateProcessCompletedEvent() {
        // 添加最终变量
        instance.getContext().setVariable("result", "success");

        // 创建流程完成事件
        ProcessCompletedEvent event = new ProcessCompletedEvent(instance);

        // 验证事件包含最终变量
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo("inst-001");
        assertThat(event.getEventType()).isEqualTo(ProcessEventType.PROCESS_COMPLETED);
        assertThat(event.getFinalVariables()).containsEntry("orderId", "ORDER_001");
        assertThat(event.getFinalVariables()).containsEntry("amount", 1500);
        assertThat(event.getFinalVariables()).containsEntry("result", "success");
    }

    @Test
    @DisplayName("PE-003: 创建节点开始事件")
    void testCreateNodeStartedEvent() {
        // 创建节点开始事件
        NodeStartedEvent event = new NodeStartedEvent(instance, "node-001", "处理订单");

        // 验证事件包含节点ID和标签
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo("inst-001");
        assertThat(event.getEventType()).isEqualTo(ProcessEventType.NODE_STARTED);
        assertThat(event.getNodeId()).isEqualTo("node-001");
        assertThat(event.getNodeLabel()).isEqualTo("处理订单");
    }

    @Test
    @DisplayName("PE-004: 创建节点完成事件")
    void testCreateNodeCompletedEvent() {
        // 创建节点执行结果
        NodeExecutionResult result = NodeExecutionResult.builder()
                .success(true)
                .status(NodeExecutionStatus.SUCCESS)
                .build();

        // 创建节点完成事件
        NodeCompletedEvent event = new NodeCompletedEvent(instance, "node-001", result);

        // 验证事件包含节点ID和执行结果
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getProcessInstanceId()).isEqualTo("inst-001");
        assertThat(event.getEventType()).isEqualTo(ProcessEventType.NODE_COMPLETED);
        assertThat(event.getNodeId()).isEqualTo("node-001");
        assertThat(event.getResult()).isNotNull();
        assertThat(event.getResult().isSuccess()).isTrue();
        assertThat(event.getResult().getStatus()).isEqualTo(NodeExecutionStatus.SUCCESS);
    }
}
