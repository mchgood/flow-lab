package com.gaibu.flowlab.engine.execution.instruction;

/**
 * 标记 Token 失败的指令。
 *
 * @param reason 失败原因
 */
public record FailInstruction(String reason) implements Instruction {
}
