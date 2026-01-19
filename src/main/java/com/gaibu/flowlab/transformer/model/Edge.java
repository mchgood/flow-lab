package com.gaibu.flowlab.transformer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程图边模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    /**
     * 起始节点 ID
     */
    private String from;

    /**
     * 目标节点 ID
     */
    private String to;

    /**
     * 边标签
     */
    private String label;

    /**
     * 条件表达式（可选，SpEL）
     * 留空时表示无条件
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private String condition = "";
}
