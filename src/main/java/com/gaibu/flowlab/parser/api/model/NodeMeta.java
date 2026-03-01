package com.gaibu.flowlab.parser.api.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 节点语义元数据。
 */
@Getter
@Setter
public class NodeMeta {

    /**
     * 节点 id。
     */
    private String nodeId;
    /**
     * 节点超时（毫秒），null 表示未配置。
     */
    private Long timeout;
    /**
     * 节点重试次数。
     */
    private Integer retry;
    /**
     * 节点所属分组 id，null 表示不属于任何组。
     */
    private String groupId;

    /**
     * 构造NodeMeta实例。
     */
    public NodeMeta() {
    }

    /**
     * 构造NodeMeta实例。
     */
    public NodeMeta(String nodeId, Long timeout, Integer retry, String groupId) {
        this.nodeId = nodeId;
        this.timeout = timeout;
        this.retry = retry;
        this.groupId = groupId;
    }

}
