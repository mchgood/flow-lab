package com.gaibu.flowlab.parser.api.model;

import com.gaibu.flowlab.parser.api.enums.DirectiveType;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指令模型。
 */
@Getter
@Setter
public class Directive {

    /**
     * 指令类型（timeout/retry/subflow 等）。
     */
    private DirectiveType type;
    /**
     * 指令参数键值对。
     */
    private Map<String, String> arguments = new LinkedHashMap<>();
    /**
     * 指令所在源码行号（1-based）。
     */
    private int lineNumber;
    /**
     * 指令作用节点 id。
     */
    private String scopedNodeId;

    /**
     * 构造Directive实例。
     */
    public Directive() {
    }

    /**
     * 构造Directive实例。
     */
    public Directive(DirectiveType type, Map<String, String> arguments, int lineNumber, String scopedNodeId) {
        this.type = type;
        this.arguments = new LinkedHashMap<>(arguments);
        this.lineNumber = lineNumber;
        this.scopedNodeId = scopedNodeId;
    }

}
