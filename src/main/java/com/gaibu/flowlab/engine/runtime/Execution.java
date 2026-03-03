package com.gaibu.flowlab.engine.runtime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行容器，用于承载 Token 的作用域关系。
 */
@Getter
@Setter
@NoArgsConstructor
public class Execution {

    /**
     * Execution 唯一标识。
     */
    private ExecutionId id;

    /**
     * 父 Execution，根节点为 null。
     */
    private Execution parent;

    /**
     * 子 Execution 列表。
     */
    private final List<Execution> children = new ArrayList<>();

    /**
     * 当前 Execution 绑定的作用域 ID。
     */
    private ScopeId scopeId;
}
