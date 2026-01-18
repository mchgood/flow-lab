package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessEventType;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import lombok.Getter;

import java.util.Map;

/**
 * 流程启动事件
 */
@Getter
public class ProcessStartedEvent extends ProcessEvent {
    /**
     * 流程定义ID
     */
    private final String processDefinitionId;

    /**
     * 初始变量
     */
    private final Map<String, Object> initialVariables;

    public ProcessStartedEvent(ProcessInstance instance) {
        super(instance.getId(), ProcessEventType.PROCESS_STARTED);
        this.processDefinitionId = instance.getProcessDefinitionId();
        this.initialVariables = instance.getContext().getAllVariables();
    }
}
