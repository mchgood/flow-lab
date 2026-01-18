package com.gaibu.flowlab.parser.lexer;

import com.gaibu.flowlab.exception.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mermaid 词法分析器
 * 将 Mermaid 文本转换为 Token 序列
 */
public class MermaidLexer {

    private final String input;
    private int position;
    private int line;
    private int column;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("flowchart", TokenType.FLOWCHART);
        KEYWORDS.put("graph", TokenType.GRAPH);
        KEYWORDS.put("subgraph", TokenType.SUBGRAPH);
        KEYWORDS.put("end", TokenType.END);
        KEYWORDS.put("TD", TokenType.TD);
        KEYWORDS.put("TB", TokenType.TB);
        KEYWORDS.put("LR", TokenType.LR);
        KEYWORDS.put("RL", TokenType.RL);
        KEYWORDS.put("BT", TokenType.BT);
    }

    public MermaidLexer(String input) {
        this.input = input != null ? input : "";
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * 词法分析主方法
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) {
                break;
            }

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
            }
        }

        // 添加 EOF token
        tokens.add(Token.builder()
                .type(TokenType.EOF)
                .value("")
                .line(line)
                .column(column)
                .build());

        return tokens;
    }

    /**
     * 获取下一个 Token
     */
    private Token nextToken() {
        int startLine = line;
        int startColumn = column;

        char current = peek();

        // 换行符
        if (current == '\n') {
            advance();
            return Token.builder()
                    .type(TokenType.NEWLINE)
                    .value("\n")
                    .line(startLine)
                    .column(startColumn)
                    .build();
        }

        // 箭头 -->
        if (current == '-' && peekNext() == '-' && peekAhead(2) == '>') {
            advance();
            advance();
            advance();
            return Token.builder()
                    .type(TokenType.ARROW)
                    .value("-->")
                    .line(startLine)
                    .column(startColumn)
                    .build();
        }

        // 单字符符号
        switch (current) {
            case '[':
                advance();
                return Token.builder()
                        .type(TokenType.BRACKET_OPEN)
                        .value("[")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case ']':
                advance();
                return Token.builder()
                        .type(TokenType.BRACKET_CLOSE)
                        .value("]")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case '{':
                advance();
                return Token.builder()
                        .type(TokenType.BRACE_OPEN)
                        .value("{")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case '}':
                advance();
                return Token.builder()
                        .type(TokenType.BRACE_CLOSE)
                        .value("}")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case '(':
                advance();
                return Token.builder()
                        .type(TokenType.PAREN_OPEN)
                        .value("(")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case ')':
                advance();
                return Token.builder()
                        .type(TokenType.PAREN_CLOSE)
                        .value(")")
                        .line(startLine)
                        .column(startColumn)
                        .build();
            case '|':
                advance();
                return Token.builder()
                        .type(TokenType.PIPE)
                        .value("|")
                        .line(startLine)
                        .column(startColumn)
                        .build();
        }

        // 数字（用于边标签表达式等）
        if (Character.isDigit(current)) {
            return numberToken(startLine, startColumn);
        }

        // 标识符或关键字
        if (Character.isLetter(current) || current == '_') {
            return identifierOrKeyword(startLine, startColumn);
        }

        // 允许表达式中的符号字符
        if (isTextSymbol(current)) {
            advance();
            return Token.builder()
                    .type(TokenType.TEXT)
                    .value(String.valueOf(current))
                    .line(startLine)
                    .column(startColumn)
                    .build();
        }

        // 未识别字符
        throw new ParseException("未识别的字符: " + current, startLine, startColumn);
    }

    /**
     * 解析标识符或关键字
     */
    private Token identifierOrKeyword(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(peek());
            advance();
        }

        String value = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(value, TokenType.IDENTIFIER);

        return Token.builder()
                .type(type)
                .value(value)
                .line(startLine)
                .column(startColumn)
                .build();
    }

    /**
     * 解析数字文本
     */
    private Token numberToken(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && Character.isDigit(peek())) {
            sb.append(peek());
            advance();
        }
        return Token.builder()
                .type(TokenType.TEXT)
                .value(sb.toString())
                .line(startLine)
                .column(startColumn)
                .build();
    }

    /**
     * 表达式中允许的符号字符
     */
    private boolean isTextSymbol(char current) {
        return current == '#' || current == '>' || current == '<' || current == '='
                || current == '!' || current == '+' || current == '-' || current == '*'
                || current == '/' || current == '.' || current == ':' || current == '?';
    }

    /**
     * 跳过空白字符（不包括换行符）
     */
    private void skipWhitespace() {
        while (!isAtEnd() && (peek() == ' ' || peek() == '\t' || peek() == '\r')) {
            advance();
        }
    }

    /**
     * 查看当前字符
     */
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return input.charAt(position);
    }

    /**
     * 查看下一个字符
     */
    private char peekNext() {
        if (position + 1 >= input.length()) {
            return '\0';
        }
        return input.charAt(position + 1);
    }

    /**
     * 向前查看 n 个字符
     */
    private char peekAhead(int n) {
        if (position + n >= input.length()) {
            return '\0';
        }
        return input.charAt(position + n);
    }

    /**
     * 前进一个字符
     */
    private void advance() {
        if (isAtEnd()) {
            return;
        }

        char current = input.charAt(position);
        position++;

        if (current == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
    }

    /**
     * 是否到达末尾
     */
    private boolean isAtEnd() {
        return position >= input.length();
    }
}
