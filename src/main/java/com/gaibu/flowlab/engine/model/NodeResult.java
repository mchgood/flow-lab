package com.gaibu.flowlab.engine.model;

import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 节点执行结果。
 *
 * <p>执行器通过该对象接收 Worker 的标准化返回值，并据此更新节点状态、
 * 合并输出变量或传播失败原因。
 */
@Getter
@Setter
public class NodeResult {

    /**
     * 节点执行状态。
     */
    private ExecutionState state;
    /**
     * 节点输出变量（将被合并到工作流上下文）。
     */
    private Map<String, Object> outputs = new LinkedHashMap<>();
    /**
     * 执行说明消息（通常用于失败/取消原因）。
     */
    private String message;
    /**
     * 执行异常对象。
     */
    private Throwable error;

    /**
     * 创建空结果对象，通常给反序列化或框架构造使用。
     */
    public NodeResult() {
    }

    /**
     * 创建节点结果。
     *
     * @param state 节点状态
     * @param outputs 节点输出变量
     * @param message 人类可读消息
     * @param error 异常对象
     */
    public NodeResult(ExecutionState state, Map<String, Object> outputs, String message, Throwable error) {
        this.state = state;
        if (outputs != null) {
            this.outputs.putAll(outputs);
        }
        this.message = message;
        this.error = error;
    }

    /**
     * 创建成功结果。
     *
     * @param outputs 输出变量
     * @return 成功状态的节点结果
     */
    public static NodeResult success(Map<String, Object> outputs) {
        return new NodeResult(ExecutionState.SUCCESS, outputs, null, null);
    }

    /**
     * 创建失败结果。
     *
     * @param message 失败说明
     * @param error 失败异常
     * @return 失败状态的节点结果
     */
    public static NodeResult failed(String message, Throwable error) {
        return new NodeResult(ExecutionState.FAILED, Map.of(), message, error);
    }

    /**
     * 创建取消结果。
     *
     * @param message 取消原因
     * @return 取消状态的节点结果
     */
    public static NodeResult cancelled(String message) {
        return new NodeResult(ExecutionState.CANCELLED, Map.of(), message, null);
    }

    /**
     * 创建超时结果。
     *
     * @param message 超时说明
     * @return 超时状态的节点结果
     */
    public static NodeResult timeout(String message) {
        return new NodeResult(ExecutionState.TIMEOUT, Map.of(), message, null);
    }

}
