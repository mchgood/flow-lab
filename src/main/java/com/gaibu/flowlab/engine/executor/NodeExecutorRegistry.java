package com.gaibu.flowlab.engine.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点执行器注册表 - 管理所有节点执行器
 */
public class NodeExecutorRegistry {
    private final Map<String, NodeExecutor> executors = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 自动注册所有节点执行器
     */
    public NodeExecutorRegistry(List<NodeExecutor> executorList) {
        for (NodeExecutor executor : executorList) {
            register(executor);
        }
    }

    /**
     * 注册节点执行器
     */
    public void register(NodeExecutor executor) {
        executors.put(executor.getSupportedShape(), executor);
    }

    /**
     * 获取节点执行器
     */
    public NodeExecutor getExecutor(String shape) {
        NodeExecutor executor = executors.get(shape);
        if (executor == null) {
            throw new IllegalArgumentException("No executor found for shape: " + shape);
        }
        return executor;
    }

    /**
     * 判断是否有对应的执行器
     */
    public boolean hasExecutor(String shape) {
        return executors.containsKey(shape);
    }
}
