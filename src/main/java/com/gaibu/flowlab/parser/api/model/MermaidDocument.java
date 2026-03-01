package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Markdown 中的单个 Mermaid 文档片段。
 */
@Getter
@Setter
public class MermaidDocument {

    /**
     * workflow 标识（来自一级标题，按规范解析）。
     */
    private String id;

    /**
     * workflow 说明（标题中 > 之后的描述，可为空）。
     */
    private String description;

    /**
     * mermaid 原文。
     */
    private String source;

    /**
     * 构造MermaidDocument实例。
     */
    public MermaidDocument() {
    }

    /**
     * 构造MermaidDocument实例。
     */
    public MermaidDocument(String id, String description, String source) {
        this.id = id;
        this.description = description;
        this.source = source;
    }

}
