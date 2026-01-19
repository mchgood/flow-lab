package com.gaibu.flowlab.engine.executor;

import com.gaibu.flowlab.transformer.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点执行器注册表 - 管理所有节点执行器
 */
public class NodeExecutorRegistry {
    private final Map<String, List<NodeExecutor>> executors = new ConcurrentHashMap<>();

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
        executors.computeIfAbsent(executor.getSupportedShape(), k -> new ArrayList<>())
                .add(executor);
    }

    /**
     * 获取节点执行器
     */
    public NodeExecutor getExecutor(Node node) {
        List<NodeExecutor> candidates = executors.get(node.getShape());
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("No executor found for shape: " + node.getShape());
        }
        for (NodeExecutor candidate : candidates) {
            if (candidate.validate(node)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    /**
    * 兼容旧接口：仅按形状返回首个执行器
    */
    public NodeExecutor getExecutor(String shape) {
        List<NodeExecutor> candidates = executors.get(shape);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("No executor found for shape: " + shape);
        }
        return candidates.get(0);
    }

    /**
     * 判断是否有对应的执行器
     */
    public boolean hasExecutor(String shape) {
        return executors.containsKey(shape);
    }
}
