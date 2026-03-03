package com.gaibu.flowlab.engine.execution.instruction;

import com.gaibu.flowlab.engine.runtime.NodeId;

import java.util.List;

/**
 * 分叉指令。
 *
 * @param nextNodes 分叉后的目标节点列表
 */
public record ForkInstruction(List<NodeId> nextNodes) implements Instruction {
}
