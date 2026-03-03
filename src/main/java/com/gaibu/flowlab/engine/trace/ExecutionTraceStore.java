package com.gaibu.flowlab.engine.trace;

import java.util.List;

/**
 * 执行轨迹存储接口。
 */
public interface ExecutionTraceStore {

    /**
     * 追加轨迹步骤。
     *
     * @param step 轨迹步骤
     */
    void append(TraceStep step);

    /**
     * 查询实例轨迹。
     *
     * @param instanceId 实例 ID
     * @return 步骤列表
     */
    List<TraceStep> getByInstanceId(String instanceId);
}
