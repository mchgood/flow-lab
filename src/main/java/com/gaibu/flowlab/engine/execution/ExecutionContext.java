package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.graph.ExecutableEdge;
import com.gaibu.flowlab.engine.graph.ExecutableGraph;
import com.gaibu.flowlab.engine.graph.ExecutableNode;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.store.VariableStore;

import java.util.List;

/**
 * 节点执行上下文。
 */
public interface ExecutionContext {

    /**
     * 当前流程实例。
     *
     * @return 实例对象
     */
    ProcessInstance instance();

    /**
     * 当前执行 Token。
     *
     * @return Token
     */
    Token token();

    /**
     * 流程变量视图。
     *
     * @return 变量存储
     */
    VariableStore variables();

    /**
     * 可执行图。
     *
     * @return 图对象
     */
    ExecutableGraph graph();

    /**
     * 当前节点。
     *
     * @return 节点对象
     */
    ExecutableNode node();

    /**
     * 当前节点出边。
     *
     * @return 出边列表
     */
    List<ExecutableEdge> outgoing();

    /**
     * 当前节点入边。
     *
     * @return 入边列表
     */
    List<ExecutableEdge> incoming();

    /**
     * 发布引擎内部事件。
     *
     * @param event 事件对象
     */
    void publishEvent(Object event);
}
