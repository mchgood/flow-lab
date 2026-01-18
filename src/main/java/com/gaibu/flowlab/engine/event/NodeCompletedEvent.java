package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessEventType;
import com.gaibu.flowlab.engine.model.NodeExecutionResult;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import lombok.Getter;

/**
 * 节点完成事件
 */
@Getter
public class NodeCompletedEvent extends ProcessEvent {
    /**
     * 节点ID
     */
    private final String nodeId;

    /**
     * 节点执行结果
     */
    private final NodeExecutionResult result;

    public NodeCompletedEvent(ProcessInstance instance, String nodeId, NodeExecutionResult result) {
        super(instance.getId(), ProcessEventType.NODE_COMPLETED);
        this.nodeId = nodeId;
        this.result = result;
    }
}
