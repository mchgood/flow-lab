package com.gaibu.flowlab.engine.trace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内存轨迹存储。
 */
public class InMemoryExecutionTraceStore implements ExecutionTraceStore {

    /**
     * 实例轨迹索引。
     */
    private final Map<String, List<TraceStep>> stepsByInstance = new LinkedHashMap<>();

    @Override
    public void append(TraceStep step) {
        stepsByInstance.computeIfAbsent(step.getInstanceId(), key -> new ArrayList<>()).add(step);
    }

    @Override
    public List<TraceStep> getByInstanceId(String instanceId) {
        return List.copyOf(stepsByInstance.getOrDefault(instanceId, List.of()));
    }
}
