package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例 - 流程定义的一次具体执行
 */
@Data
public class ProcessInstance {
    /**
     * 流程实例ID
     */
    private String id;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 业务键 - 用于关联业务数据
     */
    private String businessKey;

    /**
     * 状态：RUNNING, SUSPENDED, COMPLETED, TERMINATED
     */
    private ProcessInstanceStatus status;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 启动用户
     */
    private String startUserId;

    /**
     * 执行上下文
     */
    private ExecutionContext context;
}
