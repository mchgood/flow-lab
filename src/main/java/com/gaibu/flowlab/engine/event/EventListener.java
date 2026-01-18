package com.gaibu.flowlab.engine.event;

/**
 * 事件监听器接口 - 用于监听和处理流程事件
 * @param <T> 事件类型
 */
public interface EventListener<T extends ProcessEvent> {
    /**
     * 处理事件
     * @param event 事件对象
     */
    void onEvent(T event);

    /**
     * 获取支持的事件类型
     * @return 事件类型的 Class 对象
     */
    Class<T> getSupportedEventType();
}
