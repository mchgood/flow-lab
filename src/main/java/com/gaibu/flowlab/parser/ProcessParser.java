package com.gaibu.flowlab.parser;

import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;

/**
 * 流程定义解析器接口。
 */
public interface ProcessParser {

    /**
     * 将 Mermaid DSL 文本解析为流程定义。
     *
     * @param processId 流程定义 ID（外部传入）
     * @param dsl Mermaid 文本
     * @return 结构化流程定义
     */
    ProcessDefinition parse(String processId, String dsl);

    /**
     * 对流程定义执行结构与约束校验。
     *
     * @param definition 流程定义
     */
    void validate(ProcessDefinition definition);
}
