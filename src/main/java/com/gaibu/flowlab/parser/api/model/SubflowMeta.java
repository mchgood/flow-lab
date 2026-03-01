package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 子流程语义元数据。
 */
@Getter
@Setter
public class SubflowMeta {

    /**
     * 挂载子流程的节点 id。
     */
    private String nodeId;
    /**
     * 被引用的子流程 id。
     */
    private String referenceId;
    /**
     * 父流程到子流程的入参映射。
     */
    private Map<String, String> inputMapping = new LinkedHashMap<>();
    /**
     * 子流程到父流程的出参映射。
     */
    private Map<String, String> outputMapping = new LinkedHashMap<>();

    /**
     * 构造SubflowMeta实例。
     */
    public SubflowMeta() {
    }

    /**
     * 构造SubflowMeta实例。
     */
    public SubflowMeta(String nodeId, String referenceId, Map<String, String> inputMapping, Map<String, String> outputMapping) {
        this.nodeId = nodeId;
        this.referenceId = referenceId;
        this.inputMapping = new LinkedHashMap<>(inputMapping);
        this.outputMapping = new LinkedHashMap<>(outputMapping);
    }

}
