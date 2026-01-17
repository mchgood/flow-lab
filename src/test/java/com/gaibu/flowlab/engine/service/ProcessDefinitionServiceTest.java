package com.gaibu.flowlab.engine.service;

import com.gaibu.flowlab.engine.enums.ProcessDefinitionStatus;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.repository.ProcessDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ProcessDefinitionService 测试类
 */
@DisplayName("ProcessDefinitionService 测试")
class ProcessDefinitionServiceTest {

    private ProcessDefinitionService service;
    private ProcessDefinitionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ProcessDefinitionRepository();
        service = new ProcessDefinitionService(repository);
    }

    @Test
    @DisplayName("PDS-001: 创建流程定义")
    void testCreateProcessDefinition() {
        // 提供有效的 Mermaid 源码
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B[任务]
                    B --> C((结束))
                """;

        // 创建流程定义
        ProcessDefinition definition = service.create("订单审批流程", "订单审批描述", mermaidSource);

        // 验证流程定义创建成功，状态为 DRAFT，版本为 1
        assertThat(definition).isNotNull();
        assertThat(definition.getId()).isNotNull();
        assertThat(definition.getName()).isEqualTo("订单审批流程");
        assertThat(definition.getDescription()).isEqualTo("订单审批描述");
        assertThat(definition.getStatus()).isEqualTo(ProcessDefinitionStatus.DRAFT);
        assertThat(definition.getVersion()).isEqualTo(1);
        assertThat(definition.getMermaidSource()).isEqualTo(mermaidSource);
        assertThat(definition.getFlowGraphJson()).isNotNull();
    }

    @Test
    @DisplayName("PDS-002: 创建同名流程定义（版本递增）")
    void testCreateProcessDefinitionWithSameNameIncrementsVersion() {
        // 创建第一个流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition1 = service.create("测试流程", mermaidSource);

        // 创建同名的第二个流程定义
        ProcessDefinition definition2 = service.create("测试流程", mermaidSource);

        // 验证版本号递增
        assertThat(definition1.getVersion()).isEqualTo(1);
        assertThat(definition2.getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("PDS-003: 创建流程定义时 Mermaid 源码无效")
    void testCreateProcessDefinitionWithInvalidMermaid() {
        // 提供无效的 Mermaid 源码
        String invalidMermaid = "invalid mermaid syntax";

        // 尝试创建流程定义
        assertThatThrownBy(() -> service.create("测试流程", invalidMermaid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Mermaid source");
    }

    @Test
    @DisplayName("PDS-004: 更新流程定义")
    void testUpdateProcessDefinition() {
        // 创建流程定义
        String originalSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition = service.create("测试流程", originalSource);

        // 更新 Mermaid 源码
        String updatedSource = """
                flowchart TD
                    A((开始)) --> B[任务]
                    B --> C((结束))
                """;
        ProcessDefinition updated = service.update(definition.getId(), updatedSource);

        // 验证更新成功，updatedAt 时间更新
        assertThat(updated.getMermaidSource()).isEqualTo(updatedSource);
        assertThat(updated.getUpdatedAt()).isAfter(definition.getCreatedAt());
    }

    @Test
    @DisplayName("PDS-005: 更新非草稿状态的流程定义")
    void testUpdateNonDraftProcessDefinition() {
        // 创建并部署流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition = service.create("测试流程", mermaidSource);
        service.deploy(definition.getId());

        // 尝试更新已部署的流程定义
        String updatedSource = """
                flowchart TD
                    A((开始)) --> B[任务]
                    B --> C((结束))
                """;
        assertThatThrownBy(() -> service.update(definition.getId(), updatedSource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT process definitions can be updated");
    }

    @Test
    @DisplayName("PDS-006: 部署流程定义")
    void testDeployProcessDefinition() {
        // 创建流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition = service.create("测试流程", mermaidSource);

        // 部署流程定义
        ProcessDefinition deployed = service.deploy(definition.getId());

        // 验证状态变为 ACTIVE
        assertThat(deployed.getStatus()).isEqualTo(ProcessDefinitionStatus.ACTIVE);
    }

    @Test
    @DisplayName("PDS-007: 归档流程定义")
    void testArchiveProcessDefinition() {
        // 创建并部署流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition = service.create("测试流程", mermaidSource);
        service.deploy(definition.getId());

        // 归档流程定义
        service.archive(definition.getId());

        // 验证状态变为 ARCHIVED
        ProcessDefinition archived = service.getById(definition.getId());
        assertThat(archived.getStatus()).isEqualTo(ProcessDefinitionStatus.ARCHIVED);
    }

    @Test
    @DisplayName("PDS-008: 根据ID查询流程定义")
    void testGetProcessDefinitionById() {
        // 创建流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        ProcessDefinition definition = service.create("测试流程", mermaidSource);

        // 根据ID查询
        ProcessDefinition found = service.getById(definition.getId());

        // 验证返回正确的流程定义
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(definition.getId());
        assertThat(found.getName()).isEqualTo("测试流程");
    }

    @Test
    @DisplayName("PDS-009: 查询不存在的流程定义")
    void testGetNonExistentProcessDefinition() {
        // 根据不存在的ID查询
        ProcessDefinition found = service.getById("non-existent-id");

        // 验证返回 null
        assertThat(found).isNull();
    }

    @Test
    @DisplayName("PDS-010: 获取最新版本的流程定义")
    void testGetLatestVersionOfProcessDefinition() {
        // 创建多个版本的流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        service.create("测试流程", mermaidSource);
        service.create("测试流程", mermaidSource);
        ProcessDefinition latest = service.create("测试流程", mermaidSource);

        // 获取最新版本
        ProcessDefinition found = service.getLatestVersion("测试流程");

        // 验证返回版本号最大的流程定义
        assertThat(found).isNotNull();
        assertThat(found.getVersion()).isEqualTo(3);
        assertThat(found.getId()).isEqualTo(latest.getId());
    }

    @Test
    @DisplayName("PDS-011: 列出所有流程定义")
    void testListAllProcessDefinitions() {
        // 创建多个流程定义
        String mermaidSource = """
                flowchart TD
                    A((开始)) --> B((结束))
                """;
        service.create("流程1", mermaidSource);
        service.create("流程2", mermaidSource);
        service.create("流程3", mermaidSource);

        // 列出所有流程定义
        List<ProcessDefinition> allDefinitions = service.listAll();

        // 验证返回所有流程定义的列表
        assertThat(allDefinitions).hasSize(3);
    }
}
