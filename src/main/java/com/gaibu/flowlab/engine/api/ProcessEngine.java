package com.gaibu.flowlab.engine.api;

import com.gaibu.flowlab.engine.runtime.ProcessInstance;

import java.util.Map;

/**
 * 流程引擎对外门面。
 */
public interface ProcessEngine {

    /**
     * 启动流程实例。
     *
     * @param processId 流程定义 ID
     * @param variables 启动变量
     * @return 新实例
     */
    ProcessInstance start(String processId, Map<String, Object> variables);

}
