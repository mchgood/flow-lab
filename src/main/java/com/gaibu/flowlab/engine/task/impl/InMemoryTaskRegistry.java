package com.gaibu.flowlab.engine.task.impl;

import com.gaibu.flowlab.engine.task.FlowTask;
import com.gaibu.flowlab.engine.task.TaskRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 内存版任务注册表。
 */
public class InMemoryTaskRegistry implements TaskRegistry {

    /**
     * 节点任务映射。
     */
    private final Map<String, FlowTask> tasks = new LinkedHashMap<>();

    @Override
    public FlowTask getTask(String nodeId) {
        return tasks.get(nodeId);
    }

    /**
     * 注册任务实现。
     *
     * @param nodeId 节点 ID
     * @param task 任务实现
     */
    public void register(String nodeId, FlowTask task) {
        tasks.put(nodeId, task);
    }
}
