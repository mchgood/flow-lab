package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessInstance 测试类
 */
@DisplayName("ProcessInstance 测试")
class ProcessInstanceTest {

    @Test
    @DisplayName("PI-001: 创建流程实例")
    void testCreateProcessInstance() {
        // 创建执行上下文
        ExecutionContext context = new ExecutionContext();
        context.setId("ctx-001");
        context.setVariable("orderId", "ORDER_001");
        context.setVariable("amount", 1500);

        // 创建流程实例
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-001");
        instance.setProcessDefinitionId("def-001");
        instance.setBusinessKey("ORDER_001");
        instance.setStatus(ProcessInstanceStatus.RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setContext(context);

        // 验证属性设置正确
        assertThat(instance.getId()).isEqualTo("inst-001");
        assertThat(instance.getProcessDefinitionId()).isEqualTo("def-001");
        assertThat(instance.getBusinessKey()).isEqualTo("ORDER_001");
        assertThat(instance.getStatus()).isEqualTo(ProcessInstanceStatus.RUNNING);
        assertThat(instance.getStartTime()).isNotNull();
        assertThat(instance.getContext()).isNotNull();
        assertThat(instance.getContext().getVariable("orderId")).isEqualTo("ORDER_001");
        assertThat(instance.getContext().getVariable("amount")).isEqualTo(1500);
    }

    @Test
    @DisplayName("PI-002: 验证状态枚举")
    void testProcessInstanceStatus() {
        // 验证 RUNNING 状态
        ProcessInstance running = new ProcessInstance();
        running.setStatus(ProcessInstanceStatus.RUNNING);
        assertThat(running.getStatus()).isEqualTo(ProcessInstanceStatus.RUNNING);

        // 验证 SUSPENDED 状态
        ProcessInstance suspended = new ProcessInstance();
        suspended.setStatus(ProcessInstanceStatus.SUSPENDED);
        assertThat(suspended.getStatus()).isEqualTo(ProcessInstanceStatus.SUSPENDED);

        // 验证 COMPLETED 状态
        ProcessInstance completed = new ProcessInstance();
        completed.setStatus(ProcessInstanceStatus.COMPLETED);
        assertThat(completed.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);

        // 验证 TERMINATED 状态
        ProcessInstance terminated = new ProcessInstance();
        terminated.setStatus(ProcessInstanceStatus.TERMINATED);
        assertThat(terminated.getStatus()).isEqualTo(ProcessInstanceStatus.TERMINATED);

        // 验证所有状态枚举
        assertThat(ProcessInstanceStatus.values()).hasSize(4);
        assertThat(ProcessInstanceStatus.values()).contains(
                ProcessInstanceStatus.RUNNING,
                ProcessInstanceStatus.SUSPENDED,
                ProcessInstanceStatus.COMPLETED,
                ProcessInstanceStatus.TERMINATED
        );
    }
}
