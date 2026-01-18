package com.gaibu.flowlab.engine.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件发布器 - 管理事件监听器并发布事件
 */
public class EventPublisher {
    private final Map<Class<? extends ProcessEvent>, List<EventListener>> listeners = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 自动注册所有事件监听器
     */
    public EventPublisher(List<EventListener> listenerList) {
        for (EventListener listener : listenerList) {
            register(listener);
        }
    }

    /**
     * 注册事件监听器
     */
    public void register(EventListener listener) {
        Class<? extends ProcessEvent> eventType = listener.getSupportedEventType();
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * 发布事件
     */
    @SuppressWarnings("unchecked")
    public void publish(ProcessEvent event) {
        List<EventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    // 记录日志，但不影响流程执行
                    System.err.println("Error handling event: " + event.getEventType() + ", error: " + e.getMessage());
                }
            }
        }
    }
}
