package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.enums.ProcessDefinitionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessDefinition 测试类
 */
@DisplayName("ProcessDefinition 测试")
class ProcessDefinitionTest {

    @Test
    @DisplayName("PD-001: 创建流程定义")
    void testCreateProcessDefinition() {
        // 创建流程定义
        ProcessDefinition definition = new ProcessDefinition();
        definition.setId("def-001");
        definition.setName("订单审批流程");
        definition.setDescription("订单金额超过1000需要经理审批");
        definition.setVersion(1);
        definition.setMermaidSource("flowchart TD\n    A((开始)) --> B[任务]\n    B --> C((结束))");
        definition.setFlowGraphJson("{\"nodes\":[],\"edges\":[]}");
        definition.setStatus(ProcessDefinitionStatus.DRAFT);
        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());

        // 验证属性设置正确
        assertThat(definition.getId()).isEqualTo("def-001");
        assertThat(definition.getName()).isEqualTo("订单审批流程");
        assertThat(definition.getDescription()).isEqualTo("订单金额超过1000需要经理审批");
        assertThat(definition.getVersion()).isEqualTo(1);
        assertThat(definition.getMermaidSource()).contains("开始");
        assertThat(definition.getFlowGraphJson()).isNotNull();
        assertThat(definition.getStatus()).isEqualTo(ProcessDefinitionStatus.DRAFT);
        assertThat(definition.getCreatedAt()).isNotNull();
        assertThat(definition.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PD-002: 验证状态枚举")
    void testProcessDefinitionStatus() {
        // 验证 DRAFT 状态
        ProcessDefinition draft = new ProcessDefinition();
        draft.setStatus(ProcessDefinitionStatus.DRAFT);
        assertThat(draft.getStatus()).isEqualTo(ProcessDefinitionStatus.DRAFT);

        // 验证 ACTIVE 状态
        ProcessDefinition active = new ProcessDefinition();
        active.setStatus(ProcessDefinitionStatus.ACTIVE);
        assertThat(active.getStatus()).isEqualTo(ProcessDefinitionStatus.ACTIVE);

        // 验证 ARCHIVED 状态
        ProcessDefinition archived = new ProcessDefinition();
        archived.setStatus(ProcessDefinitionStatus.ARCHIVED);
        assertThat(archived.getStatus()).isEqualTo(ProcessDefinitionStatus.ARCHIVED);

        // 验证所有状态枚举
        assertThat(ProcessDefinitionStatus.values()).hasSize(3);
        assertThat(ProcessDefinitionStatus.values()).contains(
                ProcessDefinitionStatus.DRAFT,
                ProcessDefinitionStatus.ACTIVE,
                ProcessDefinitionStatus.ARCHIVED
        );
    }
}
