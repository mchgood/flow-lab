package com.gaibu.flowlab.engine.execution;

import com.gaibu.flowlab.engine.runtime.Execution;
import com.gaibu.flowlab.engine.runtime.NodeId;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.runtime.TokenId;
import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Token 工厂。
 */
public class TokenFactory {

    /**
     * Token ID 生成器。
     */
    private final AtomicLong tokenSeq;

    public TokenFactory(AtomicLong tokenSeq) {
        this.tokenSeq = tokenSeq;
    }

    /**
     * 创建一个活跃 Token。
     *
     * @param nodeId 节点 ID
     * @param execution 所属 execution
     * @return 新 token
     */
    public Token create(NodeId nodeId, Execution execution) {
        Token token = new Token();
        token.setId(new TokenId("TK-" + tokenSeq.incrementAndGet()));
        token.setCurrentNode(nodeId);
        token.setExecution(execution);
        token.setStatus(TokenStatus.ACTIVE);
        return token;
    }
}
