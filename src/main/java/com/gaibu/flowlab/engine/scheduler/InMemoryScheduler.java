package com.gaibu.flowlab.engine.scheduler;

import com.gaibu.flowlab.engine.runtime.Token;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 基于内存队列的调度器实现。
 */
public class InMemoryScheduler implements Scheduler {

    /**
     * FIFO 调度队列。
     */
    private final Deque<Token> queue = new ArrayDeque<>();

    @Override
    public void schedule(Token token) {
        queue.offer(token);
    }

    @Override
    public Token poll() {
        return queue.poll();
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }
}
