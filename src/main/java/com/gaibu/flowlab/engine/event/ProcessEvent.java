package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessEventType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 流程事件基类 - 所有流程事件的父类
 */
@Data
public abstract class ProcessEvent {
    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 事件时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 事件类型
     */
    private ProcessEventType eventType;

    /**
     * 构造函数
     */
    public ProcessEvent(String processInstanceId, ProcessEventType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = processInstanceId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
}
