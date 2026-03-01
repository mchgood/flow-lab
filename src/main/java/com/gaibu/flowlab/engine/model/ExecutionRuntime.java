package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 执行期状态容器。
 *
 * <p>该对象是调度器的单线程可变状态：记录执行计划、上下文变量以及每个执行单元的状态。
 */
public class ExecutionRuntime {

    /**
     * 该次调度绑定的执行计划。
     */
    @Getter
    private final ExecutionPlan plan;
    /**
     * 该次调度使用的执行上下文。
     */
    @Getter
    private final ExecutionContext context;
    /**
     * 执行单元状态表（unitId -> state）。
     */
    private final Map<String, ExecutionState> stateByUnit = new LinkedHashMap<>();
    /**
     * 调度线程 id，用于限制状态更新只能由调度线程执行。
     */
    private final long schedulerThreadId;

    /**
     * 创建运行时状态对象，并将所有执行单元初始化为 {@link ExecutionState#CREATED}。
     */
    public ExecutionRuntime(ExecutionPlan plan, ExecutionContext context) {
        this.plan = plan;
        this.context = context;
        this.schedulerThreadId = Thread.currentThread().getId();
        for (String unitId : plan.getUnitMap().keySet()) {
            stateByUnit.put(unitId, ExecutionState.CREATED);
        }
    }

    /**
     * 获取state。
     * @return state
     */
    public ExecutionState getState(String unitId) {
        return stateByUnit.get(unitId);
    }

    /**
     * 获取allStates。
     * @return allStates
     */
    public Map<String, ExecutionState> getAllStates() {
        return new LinkedHashMap<>(stateByUnit);
    }

    /**
     * 更新执行单元状态。
     *
     * <p>仅允许调度线程调用，且终态不可逆（终态后再次修改会抛出异常）。
     */
    public void updateState(String unitId, ExecutionState state) {
        if (Thread.currentThread().getId() != schedulerThreadId) {
            throw new IllegalStateException("updateState 仅允许 Scheduler 线程调用");
        }
        ExecutionState current = stateByUnit.get(unitId);
        if (current != null && current.isTerminal()) {
            throw new IllegalStateException("终态不可逆: " + unitId + " -> " + current);
        }
        stateByUnit.put(unitId, state);
    }
}
