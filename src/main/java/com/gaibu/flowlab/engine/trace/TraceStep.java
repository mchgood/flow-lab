package com.gaibu.flowlab.engine.trace;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行轨迹步骤。
 */
@Getter
@Setter
@NoArgsConstructor
public class TraceStep {

    /**
     * 流程实例 ID。
     */
    private String instanceId;

    /**
     * Token ID。
     */
    private String tokenId;

    /**
     * 当前节点 ID。
     */
    private String fromNodeId;

    /**
     * 指令类型。
     */
    private String instructionType;

    /**
     * 目标节点列表。
     */
    private final List<String> toNodeIds = new ArrayList<>();

    /**
     * 是否成功。
     */
    private boolean success;

    /**
     * 失败原因。
     */
    private String errorMessage;

    /**
     * 记录时间戳。
     */
    private long timestamp;
}
