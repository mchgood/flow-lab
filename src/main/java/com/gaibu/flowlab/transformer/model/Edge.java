package com.gaibu.flowlab.transformer.model;

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
}
