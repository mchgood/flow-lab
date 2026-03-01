package com.gaibu.flowlab.parser.api;

import com.gaibu.flowlab.parser.api.model.Graph;

/**
 * Flowchart 结构解析接口。
 */
public interface FlowchartParser {

    /**
     * 解析 Mermaid flowchart 文本为图结构。
     *
     * @param mermaidSource mermaid 文本
     * @return 图结构
     */
    Graph parse(String mermaidSource);
}
