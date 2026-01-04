package com.gaibu.flowlab.parser.lexer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token（词法单元）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    /**
     * Token 类型
     */
    private TokenType type;

    /**
     * Token 值
     */
    private String value;

    /**
     * 所在行号（从 1 开始）
     */
    private int line;

    /**
     * 所在列号（从 1 开始）
     */
    private int column;

    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', line=%d, column=%d}",
                type, value, line, column);
    }
}
