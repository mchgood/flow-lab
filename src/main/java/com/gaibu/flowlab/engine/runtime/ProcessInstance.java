package com.gaibu.flowlab.engine.runtime;

import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.scope.ScopeRuntime;
import com.gaibu.flowlab.engine.store.VariableStore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例运行时根对象。
 */
@Getter
@Setter
@NoArgsConstructor
public class ProcessInstance {

    /**
     * 实例唯一标识。
     */
    private String id;

    /**
     * 根 Execution 容器。
     */
    private Execution rootExecution;

    /**
     * 当前活跃 Token 列表。
     */
    private final List<Token> activeTokens = new ArrayList<>();

    /**
     * 实例变量存储。
     */
    private VariableStore variables;

    /**
     * 实例状态。
     */
    private InstanceStatus status;

    /**
     * 中断原因，仅 status=INTERRUPTED 时有效。
     */
    private String interruptReason;

    /**
     * 流程失败原因，仅 status=FAILED 时有效。
     */
    private Throwable failureCause;

    /**
     * Token 索引（key=tokenId），用于快速定位 Token。
     */
    private final Map<TokenId, Token> tokensById = new LinkedHashMap<>();

    /**
     * Scope 运行时存储（key=scopeId.value）。
     */
    private final Map<String, ScopeRuntime> scopes = new LinkedHashMap<>();

    /**
     * 追加活跃 Token 并同步索引。
     *
     * @param token 需要添加的 Token
     */
    public void addToken(Token token) {
        tokensById.put(token.getId(), token);
        activeTokens.add(token);
    }

    /**
     * 移除活跃 Token，不删除总索引。
     *
     * @param token 需要移除的 Token
     */
    public void removeActiveToken(Token token) {
        activeTokens.remove(token);
    }

}
