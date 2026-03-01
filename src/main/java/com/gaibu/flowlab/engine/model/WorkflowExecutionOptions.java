package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.model.enums.CancellationStrategy;
import com.gaibu.flowlab.engine.model.enums.FailureStrategy;
import lombok.Getter;
import lombok.Setter;

/**
 * 执行选项。
 */
@Getter
@Setter
public class WorkflowExecutionOptions {

    /**
     * 失败策略（失败即终止或继续执行）。
     */
    private FailureStrategy failureStrategy = FailureStrategy.FAIL_FAST;
    /**
     * 取消传播策略。
     */
    private CancellationStrategy cancellationStrategy = CancellationStrategy.PROPAGATE_DOWN;
    /**
     * 工作流全局超时（毫秒），null 表示不限制。
     */
    private Long globalTimeoutMs;
    /**
     * 节点默认超时（毫秒），null 表示使用节点自身配置或不限制。
     */
    private Long globalNodeTimeoutMs;

    /**
     * 执行defaults并返回结果。
     * @return 执行结果
     */
    public static WorkflowExecutionOptions defaults() {
        return new WorkflowExecutionOptions();
    }
}
