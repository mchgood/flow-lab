package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessEventType;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import lombok.Getter;

/**
 * 节点开始事件
 */
@Getter
public class NodeStartedEvent extends ProcessEvent {
    /**
     * 节点ID
     */
    private final String nodeId;

    /**
     * 节点标签
     */
    private final String nodeLabel;

    public NodeStartedEvent(ProcessInstance instance, String nodeId, String nodeLabel) {
        super(instance.getId(), ProcessEventType.NODE_STARTED);
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
    }
}
