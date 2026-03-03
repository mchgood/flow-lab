package com.gaibu.flowlab.engine.task.context;

import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;

import java.util.ArrayList;
import java.util.Map;

/**
 * 默认任务上下文实现。
 */
public class DefaultTaskContext implements TaskContext {

    /**
     * 当前流程实例。
     */
    private final ProcessInstance instance;

    /**
     * 当前 Token。
     */
    private final Token token;

    public DefaultTaskContext(ProcessInstance instance, Token token) {
        this.instance = instance;
        this.token = token;
    }

    @Override
    public ProcessInstance instance() {
        return instance;
    }

    @Override
    public Token token() {
        return token;
    }

    @Override
    public Object getVariable(String key) {
        return instance.getVariables().get(key);
    }

    @Override
    public <T> T getVariable(String key, Class<T> type) {
        Object value = getVariable(key);
        if (value == null) {
            return null;
        }
        Class<?> targetType = wrapPrimitive(type);
        if (!targetType.isInstance(value)) {
            throw new IllegalArgumentException("Variable type mismatch, key=" + key
                    + ", expected=" + targetType.getName()
                    + ", actual=" + value.getClass().getName());
        }
        @SuppressWarnings("unchecked")
        T casted = (T) targetType.cast(value);
        return casted;
    }

    @Override
    public <T> T getVariableOrDefault(String key, Class<T> type, T defaultValue) {
        T value = getVariable(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public void setVariable(String key, Object value) {
        instance.getVariables().put(key, value);
    }

    @Override
    public Map<String, Object> variables() {
        return instance.getVariables().snapshot();
    }

    @Override
    public void interruptProcess(String reason) {
        instance.setInterruptReason(reason);
        instance.setStatus(InstanceStatus.INTERRUPTED);

        for (Token active : new ArrayList<>(instance.getActiveTokens())) {
            active.setStatus(TokenStatus.COMPLETED);
        }
        instance.getActiveTokens().clear();
    }

    @Override
    public boolean interrupted() {
        return instance.getStatus() == InstanceStatus.INTERRUPTED;
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
