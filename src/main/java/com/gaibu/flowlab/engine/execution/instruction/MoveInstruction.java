package com.gaibu.flowlab.engine.execution.instruction;

import com.gaibu.flowlab.engine.runtime.NodeId;

/**
 * 单路径移动指令。
 *
 * @param nextNode 下一节点
 */
public record MoveInstruction(NodeId nextNode) implements Instruction {
}
