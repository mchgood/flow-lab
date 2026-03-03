package com.gaibu.flowlab.engine.task;

import com.gaibu.flowlab.engine.task.context.TaskContext;

/**
 * 流程任务节点执行接口。
 */
public interface FlowTask {

    /**
     * 执行任务逻辑。
     *
     * @param context 任务上下文
     * @throws Exception 任务异常
     */
    void execute(TaskContext context) throws Exception;
}
