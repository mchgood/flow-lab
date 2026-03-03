package com.gaibu.flowlab.engine.execution.instruction;

import com.gaibu.flowlab.engine.runtime.NodeId;

/**
 * 汇聚指令。
 *
 * @param joinNode 汇聚节点
 */
public record JoinInstruction(NodeId joinNode) implements Instruction {
}
