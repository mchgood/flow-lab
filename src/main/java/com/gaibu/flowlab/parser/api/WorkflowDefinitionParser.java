package com.gaibu.flowlab.parser.api;

import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;

import java.util.List;

/**
 * WorkflowDefinition 解析入口接口。
 */
public interface WorkflowDefinitionParser {

    /**
     * 将 markdown 文本解析为 workflow 定义列表。
     *
     * @param markdownContent markdown 原文
     * @return workflow 定义列表
     */
    List<WorkflowDefinition> parse(String markdownContent);
}
