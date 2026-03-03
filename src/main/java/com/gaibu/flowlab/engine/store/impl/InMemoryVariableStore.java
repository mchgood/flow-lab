package com.gaibu.flowlab.engine.store.impl;

import com.gaibu.flowlab.engine.store.VariableStore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于内存 Map 的变量存储实现。
 */
public class InMemoryVariableStore implements VariableStore {

    /**
     * 变量容器，单线程调度模型下无需额外并发结构。
     */
    private final Map<String, Object> values = new LinkedHashMap<>();

    @Override
    public Object get(String key) {
        return values.get(key);
    }

    @Override
    public void put(String key, Object value) {
        values.put(key, value);
    }

    @Override
    public void remove(String key) {
        values.remove(key);
    }

    @Override
    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(values);
    }
}
