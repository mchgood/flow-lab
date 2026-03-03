package com.gaibu.flowlab.engine.task.context;

import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.Token;

import java.util.Map;

/**
 * 任务执行上下文。
 */
public interface TaskContext {

    /**
     * 获取流程实例。
     *
     * @return 流程实例
     */
    ProcessInstance instance();

    /**
     * 获取当前 Token。
     *
     * @return 当前 Token
     */
    Token token();

    /**
     * 读取变量。
     *
     * @param key 变量名
     * @return 变量值
     */
    Object getVariable(String key);

    /**
     * 按指定类型读取变量。
     *
     * @param key 变量名
     * @param type 目标类型
     * @param <T> 类型参数
     * @return 变量值，不存在返回 null
     */
    <T> T getVariable(String key, Class<T> type);

    /**
     * 按指定类型读取变量，不存在时返回默认值。
     *
     * @param key 变量名
     * @param type 目标类型
     * @param defaultValue 默认值
     * @param <T> 类型参数
     * @return 变量值或默认值
     */
    <T> T getVariableOrDefault(String key, Class<T> type, T defaultValue);

    /**
     * 写入变量。
     *
     * @param key 变量名
     * @param value 变量值
     */
    void setVariable(String key, Object value);

    /**
     * 变量快照。
     *
     * @return 变量只读快照
     */
    Map<String, Object> variables();

    /**
     * 主动中断当前流程实例。
     *
     * @param reason 中断原因
     */
    void interruptProcess(String reason);

    /**
     * 当前流程是否已被中断。
     *
     * @return true 表示已中断
     */
    boolean interrupted();
}
