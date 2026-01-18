package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessEventType;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import lombok.Getter;

import java.util.Map;

/**
 * 流程完成事件
 */
@Getter
public class ProcessCompletedEvent extends ProcessEvent {
    /**
     * 最终变量
     */
    private final Map<String, Object> finalVariables;

    public ProcessCompletedEvent(ProcessInstance instance) {
        super(instance.getId(), ProcessEventType.PROCESS_COMPLETED);
        this.finalVariables = instance.getContext().getAllVariables();
    }
}
