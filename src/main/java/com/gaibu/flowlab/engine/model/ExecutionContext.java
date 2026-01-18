package com.gaibu.flowlab.engine.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 执行上下文 - 存储流程执行过程中的变量和状态
 */
@Data
public class ExecutionContext {
    /**
     * 上下文ID
     */
    private String id;

    /**
     * 流程变量
     */
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 设置变量
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    /**
     * 获取变量
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }

    /**
     * 获取变量（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String name, T defaultValue) {
        Object value = variables.get(name);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 判断变量是否存在
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * 移除变量
     */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * 获取所有变量
     */
    public Map<String, Object> getAllVariables() {
        return new HashMap<>(variables);
    }

    /**
     * 批量设置变量
     */
    public void setVariables(Map<String, Object> vars) {
        if (vars != null) {
            variables.putAll(vars);
        }
    }
}
