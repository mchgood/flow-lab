package com.gaibu.flowlab.engine.runtime;

import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 真正流动的执行单元。
 */
@Getter
@Setter
@NoArgsConstructor
public class Token {

    /**
     * Token 唯一标识。
     */
    private TokenId id;

    /**
     * Token 当前所在节点。
     */
    private NodeId currentNode;

    /**
     * Token 所属 Execution 容器。
     */
    private Execution execution;

    /**
     * Token 当前状态。
     */
    private TokenStatus status;
}
