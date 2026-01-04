package com.gaibu.flowlab.transformer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程图节点模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {

    /**
     * 节点 ID
     */
    private String id;

    /**
     * 节点标签文本
     */
    private String label;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 节点形状
     */
    private String shape;
}
