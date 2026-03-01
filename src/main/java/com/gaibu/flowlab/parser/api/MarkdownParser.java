package com.gaibu.flowlab.parser.api;

import com.gaibu.flowlab.parser.api.model.MermaidDocument;

import java.util.List;

/**
 * Markdown 解析接口。
 */
public interface MarkdownParser {

    /**
     * 解析 markdown 文本中的流程定义片段。
     *
     * @param markdownContent markdown 原文
     * @return 解析得到的 Mermaid 文档列表
     */
    List<MermaidDocument> parse(String markdownContent);
}
