package com.gaibu.flowlab.parser.lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mermaid 词法分析器测试
 */
class MermaidLexerTest {

    @Test
    void testSimpleFlowchart() {
        String input = "flowchart TD";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens).hasSize(3); // flowchart, TD, EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FLOWCHART);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.TD);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.EOF);
    }

    @Test
    void testNodeDefinition() {
        String input = "A";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens).hasSize(2); // A, EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(0).getValue()).isEqualTo("A");
    }

    @Test
    void testBrackets() {
        String input = "[]{}()";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.BRACKET_OPEN);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.BRACKET_CLOSE);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.BRACE_OPEN);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.BRACE_CLOSE);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.PAREN_OPEN);
        assertThat(tokens.get(5).getType()).isEqualTo(TokenType.PAREN_CLOSE);
    }

    @Test
    void testArrow() {
        String input = "-->";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens).hasSize(2); // -->, EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.ARROW);
        assertThat(tokens.get(0).getValue()).isEqualTo("-->");
    }

    @Test
    void testNewline() {
        String input = "flowchart\nTD";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FLOWCHART);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.NEWLINE);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.TD);
    }

    @Test
    void testComplexInput() {
        String input = """
                flowchart TD
                    A[开始] --> B
                """;
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FLOWCHART);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.TD);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.NEWLINE);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(3).getValue()).isEqualTo("A");
    }

    @Test
    void testKeywords() {
        String input = "flowchart graph subgraph end";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FLOWCHART);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.GRAPH);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.SUBGRAPH);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.END);
    }

    @Test
    void testDirections() {
        String input = "TD TB LR RL BT";
        MermaidLexer lexer = new MermaidLexer(input);
        List<Token> tokens = lexer.tokenize();

        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.TD);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.TB);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.LR);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.RL);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.BT);
    }
}
