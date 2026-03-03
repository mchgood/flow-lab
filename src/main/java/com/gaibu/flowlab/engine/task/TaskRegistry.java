package com.gaibu.flowlab.engine.task;

/**
 * 任务注册表，负责根据节点 ID 解析任务实现。
 */
public interface TaskRegistry {

    /**
     * 通过节点 ID 获取任务。
     *
     * @param nodeId 节点 ID
     * @return 对应任务，不存在返回 null
     */
    FlowTask getTask(String nodeId);
}
