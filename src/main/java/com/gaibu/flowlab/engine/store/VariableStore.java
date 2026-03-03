package com.gaibu.flowlab.engine.store;

import java.util.Map;

/**
 * 流程变量存储抽象。
 */
public interface VariableStore {

    /**
     * 读取变量。
     *
     * @param key 变量名
     * @return 变量值，不存在返回 null
     */
    Object get(String key);

    /**
     * 写入变量。
     *
     * @param key 变量名
     * @param value 变量值
     */
    void put(String key, Object value);

    /**
     * 删除变量。
     *
     * @param key 变量名
     */
    void remove(String key);

    /**
     * 返回变量快照。
     *
     * @return 当前变量只读视图
     */
    Map<String, Object> snapshot();
}
