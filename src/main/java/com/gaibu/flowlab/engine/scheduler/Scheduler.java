package com.gaibu.flowlab.engine.scheduler;

import com.gaibu.flowlab.engine.runtime.Token;

/**
 * Token 调度器。
 */
public interface Scheduler {

    /**
     * 将 Token 放入调度队列。
     *
     * @param token Token
     */
    void schedule(Token token);

    /**
     * 获取下一个待执行 Token。
     *
     * @return Token，不存在返回 null
     */
    Token poll();

    /**
     * 是否存在待调度 Token。
     *
     * @return true 表示有待执行 Token
     */
    boolean hasNext();
}
