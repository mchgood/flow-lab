package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.enums.NodeExecutionStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 节点执行结果
 */
@Data
@Builder
public class NodeExecutionResult {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 输出变量
     */
    private Map<String, Object> outputs;

    /**
     * 执行状态：SUCCESS, FAILED, WAITING
     */
    private NodeExecutionStatus status;
}
