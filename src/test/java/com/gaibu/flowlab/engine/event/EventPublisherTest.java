package com.gaibu.flowlab.engine.event;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EventPublisher 测试类
 */
@DisplayName("EventPublisher 测试")
class EventPublisherTest {

    private ProcessInstance instance;

    @BeforeEach
    void setUp() {
        // 创建执行上下文
        ExecutionContext context = new ExecutionContext();
        context.setId("ctx-001");

        // 创建流程实例
        instance = new ProcessInstance();
        instance.setId("inst-001");
        instance.setProcessDefinitionId("def-001");
        instance.setStatus(ProcessInstanceStatus.RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setContext(context);
    }

    @Test
    @DisplayName("EP-001: 注册事件监听器")
    void testRegisterEventListener() {
        // 创建监听器
        TestProcessStartedListener listener = new TestProcessStartedListener();

        // 创建事件发布器并注册监听器
        EventPublisher publisher = new EventPublisher(Arrays.asList(listener));

        // 验证监听器注册成功（通过发布事件验证）
        ProcessStartedEvent event = new ProcessStartedEvent(instance);
        publisher.publish(event);

        // 验证监听器被调用
        assertThat(listener.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("EP-002: 发布事件并触发监听器")
    void testPublishEventTriggersListener() {
        // 创建监听器
        TestProcessStartedListener listener = new TestProcessStartedListener();

        // 创建事件发布器
        EventPublisher publisher = new EventPublisher(Arrays.asList(listener));

        // 发布事件
        ProcessStartedEvent event = new ProcessStartedEvent(instance);
        publisher.publish(event);

        // 验证监听器的 onEvent 方法被调用
        assertThat(listener.getCallCount()).isEqualTo(1);
        assertThat(listener.getLastEvent()).isNotNull();
        assertThat(listener.getLastEvent().getProcessInstanceId()).isEqualTo("inst-001");
    }

    @Test
    @DisplayName("EP-003: 发布事件给多个监听器")
    void testPublishEventToMultipleListeners() {
        // 创建多个监听器
        TestProcessStartedListener listener1 = new TestProcessStartedListener();
        TestProcessStartedListener listener2 = new TestProcessStartedListener();
        TestProcessStartedListener listener3 = new TestProcessStartedListener();

        // 创建事件发布器
        EventPublisher publisher = new EventPublisher(Arrays.asList(listener1, listener2, listener3));

        // 发布事件
        ProcessStartedEvent event = new ProcessStartedEvent(instance);
        publisher.publish(event);

        // 验证所有监听器都被调用
        assertThat(listener1.getCallCount()).isEqualTo(1);
        assertThat(listener2.getCallCount()).isEqualTo(1);
        assertThat(listener3.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("EP-004: 监听器抛出异常不影响其他监听器")
    void testListenerExceptionDoesNotAffectOthers() {
        // 创建两个监听器，第一个会抛出异常
        TestProcessStartedListener throwingListener = new TestProcessStartedListener(true);
        TestProcessStartedListener normalListener = new TestProcessStartedListener();

        // 创建事件发布器
        EventPublisher publisher = new EventPublisher(Arrays.asList(throwingListener, normalListener));

        // 发布事件
        ProcessStartedEvent event = new ProcessStartedEvent(instance);
        publisher.publish(event);

        // 验证第二个监听器仍然被调用
        assertThat(throwingListener.getCallCount()).isEqualTo(1);
        assertThat(normalListener.getCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("EP-005: 发布未注册类型的事件")
    void testPublishUnregisteredEventType() {
        // 创建只监听 ProcessStartedEvent 的监听器
        TestProcessStartedListener listener = new TestProcessStartedListener();

        // 创建事件发布器
        EventPublisher publisher = new EventPublisher(Arrays.asList(listener));

        // 发布 ProcessCompletedEvent（未注册的事件类型）
        ProcessCompletedEvent event = new ProcessCompletedEvent(instance);
        publisher.publish(event);

        // 验证不抛出异常，监听器未被调用
        assertThat(listener.getCallCount()).isEqualTo(0);
    }

    /**
     * 测试用的 ProcessStartedEvent 监听器
     */
    static class TestProcessStartedListener implements EventListener<ProcessStartedEvent> {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private ProcessStartedEvent lastEvent;
        private final boolean shouldThrowException;

        public TestProcessStartedListener() {
            this(false);
        }

        public TestProcessStartedListener(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }

        @Override
        public void onEvent(ProcessStartedEvent event) {
            callCount.incrementAndGet();
            lastEvent = event;
            if (shouldThrowException) {
                throw new RuntimeException("Test exception");
            }
        }

        @Override
        public Class<ProcessStartedEvent> getSupportedEventType() {
            return ProcessStartedEvent.class;
        }

        public int getCallCount() {
            return callCount.get();
        }

        public ProcessStartedEvent getLastEvent() {
            return lastEvent;
        }
    }
}
