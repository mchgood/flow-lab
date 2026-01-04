package com.gaibu.flowlab.exception;

/**
 * 解析异常
 * 当 Mermaid 流程图解析失败时抛出
 */
public class ParseException extends RuntimeException {

    private final int line;
    private final int column;

    public ParseException(String message) {
        super(message);
        this.line = -1;
        this.column = -1;
    }

    public ParseException(String message, int line, int column) {
        super(String.format("解析错误 [行:%d, 列:%d]: %s", line, column, message));
        this.line = line;
        this.column = column;
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.line = -1;
        this.column = -1;
    }

    public ParseException(String message, int line, int column, Throwable cause) {
        super(String.format("解析错误 [行:%d, 列:%d]: %s", line, column, message), cause);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
