package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Workflow 执行结果。
 */
@Getter
@Setter
public class WorkflowExecutionResult {

    /**
     * 工作流 id。
     */
    private String workflowId;
    /**
     * 工作流最终状态。
     */
    private ExecutionState state;
    /**
     * 执行结束后的变量快照。
     */
    private Map<String, Object> variables = new LinkedHashMap<>();
    /**
     * 节点状态快照（nodeId -> state）。
     */
    private Map<String, ExecutionState> unitStates = new LinkedHashMap<>();
    /**
     * 结果说明消息。
     */
    private String message;
    /**
     * 失败或超时时的异常对象。
     */
    private Throwable error;

}
