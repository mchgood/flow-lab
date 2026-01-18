package com.gaibu.flowlab.engine.service;

import com.gaibu.flowlab.engine.enums.ProcessDefinitionStatus;
import com.gaibu.flowlab.engine.model.ProcessDefinition;
import com.gaibu.flowlab.engine.repository.ProcessDefinitionRepository;
import com.gaibu.flowlab.service.FlowParserService;
import com.gaibu.flowlab.transformer.model.FlowGraph;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 流程定义管理服务
 */
public class ProcessDefinitionService {
    private final ProcessDefinitionRepository repository;
    private final FlowParserService flowParserService;

    public ProcessDefinitionService(ProcessDefinitionRepository repository) {
        this.repository = repository;
        this.flowParserService = new FlowParserService();
    }

    /**
     * 创建流程定义
     */
    public ProcessDefinition create(String name, String mermaidSource) {
        return create(name, null, mermaidSource);
    }

    /**
     * 创建流程定义（带描述）
     */
    public ProcessDefinition create(String name, String description, String mermaidSource) {
        // 验证 Mermaid 源码
        if (!flowParserService.validate(mermaidSource)) {
            throw new IllegalArgumentException("Invalid Mermaid source");
        }

        // 解析 Mermaid 源码
        FlowGraph flowGraph = flowParserService.parse(mermaidSource);
        String flowGraphJson = flowParserService.parseToJson(mermaidSource);

        // 获取版本号
        List<ProcessDefinition> existingDefinitions = repository.findByName(name);
        int version = existingDefinitions.stream()
                .map(ProcessDefinition::getVersion)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;

        // 创建流程定义
        ProcessDefinition definition = new ProcessDefinition();
        definition.setId(UUID.randomUUID().toString());
        definition.setName(name);
        definition.setDescription(description);
        definition.setVersion(version);
        definition.setMermaidSource(mermaidSource);
        definition.setFlowGraphJson(flowGraphJson);
        definition.setStatus(ProcessDefinitionStatus.DRAFT);
        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());

        return repository.save(definition);
    }

    /**
     * 更新流程定义
     */
    public ProcessDefinition update(String id, String mermaidSource) {
        ProcessDefinition definition = repository.findById(id);
        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + id);
        }

        // 只有草稿状态的流程定义可以更新
        if (definition.getStatus() != ProcessDefinitionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT process definitions can be updated");
        }

        // 验证 Mermaid 源码
        if (!flowParserService.validate(mermaidSource)) {
            throw new IllegalArgumentException("Invalid Mermaid source");
        }

        // 解析 Mermaid 源码
        String flowGraphJson = flowParserService.parseToJson(mermaidSource);

        // 更新流程定义
        definition.setMermaidSource(mermaidSource);
        definition.setFlowGraphJson(flowGraphJson);
        definition.setUpdatedAt(LocalDateTime.now());

        return repository.save(definition);
    }

    /**
     * 部署流程定义（激活）
     */
    public ProcessDefinition deploy(String id) {
        ProcessDefinition definition = repository.findById(id);
        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + id);
        }

        definition.setStatus(ProcessDefinitionStatus.ACTIVE);
        definition.setUpdatedAt(LocalDateTime.now());

        return repository.save(definition);
    }

    /**
     * 归档流程定义
     */
    public void archive(String id) {
        ProcessDefinition definition = repository.findById(id);
        if (definition == null) {
            throw new IllegalArgumentException("Process definition not found: " + id);
        }

        definition.setStatus(ProcessDefinitionStatus.ARCHIVED);
        definition.setUpdatedAt(LocalDateTime.now());

        repository.save(definition);
    }

    /**
     * 根据ID查询流程定义
     */
    public ProcessDefinition getById(String id) {
        return repository.findById(id);
    }

    /**
     * 获取最新版本的流程定义
     */
    public ProcessDefinition getLatestVersion(String name) {
        List<ProcessDefinition> definitions = repository.findByName(name);
        return definitions.stream()
                .max(Comparator.comparing(ProcessDefinition::getVersion))
                .orElse(null);
    }

    /**
     * 列出所有流程定义
     */
    public List<ProcessDefinition> listAll() {
        return repository.findAll();
    }
}
