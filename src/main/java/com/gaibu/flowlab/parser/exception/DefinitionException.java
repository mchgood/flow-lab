package com.gaibu.flowlab.parser.exception;

/**
 * 流程定义在解析或校验阶段出现非法结构时抛出的异常。
 */
public class DefinitionException extends RuntimeException {

    public DefinitionException(String message) {
        super(message);
    }
}
