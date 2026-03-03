package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.store.VariableStore;

/**
 * 子流程启动器。
 */
@FunctionalInterface
public interface SubProcessLauncher {

    /**
     * 同步执行子流程。
     *
     * @param processId 子流程定义 ID
     * @param variables 子流程变量存储（默认复用父流程上下文）
     */
    void launch(String processId, VariableStore variables);
}
