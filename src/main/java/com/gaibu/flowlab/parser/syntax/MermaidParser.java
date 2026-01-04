package com.gaibu.flowlab.parser.syntax;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.parser.ast.*;
import com.gaibu.flowlab.parser.lexer.Token;
import com.gaibu.flowlab.parser.lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mermaid 语法分析器
 * 使用递归下降方法将 Token 序列转换为 AST
 */
public class MermaidParser {

    private final List<Token> tokens;
    private int current;
    private final Map<String, FlowchartNode> nodeRegistry;

    public MermaidParser(List<Token> tokens) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.current = 0;
        this.nodeRegistry = new HashMap<>();
    }

    /**
     * 解析入口
     */
    public FlowchartAST parse() {
        skipNewlines();

        // 解析 flowchart 或 graph 关键字
        if (!match(TokenType.FLOWCHART, TokenType.GRAPH)) {
            throw new ParseException("期望 'flowchart' 或 'graph' 关键字", getCurrentLine(), getCurrentColumn());
        }

        skipNewlines();

        // 解析方向
        String direction = parseDirection();

        skipNewlines();

        // 解析语句列表
        List<ASTNode> statements = parseStatements();

        return FlowchartAST.builder()
                .direction(direction)
                .statements(statements)
                .build();
    }

    /**
     * 解析方向
     */
    private String parseDirection() {
        if (match(TokenType.TD, TokenType.TB, TokenType.LR, TokenType.RL, TokenType.BT)) {
            return previous().getValue();
        }
        // 默认方向 TD
        return "TD";
    }

    /**
     * 解析语句列表
     */
    private List<ASTNode> parseStatements() {
        List<ASTNode> statements = new ArrayList<>();

        while (!isAtEnd() && !check(TokenType.END)) {
            skipNewlines();
            if (isAtEnd() || check(TokenType.END)) {
                break;
            }

            ASTNode statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }

            skipNewlines();
        }

        return statements;
    }

    /**
     * 解析单个语句
     */
    private ASTNode parseStatement() {
        // 子图
        if (match(TokenType.SUBGRAPH)) {
            return parseSubgraph();
        }

        // 节点定义或边定义
        return parseNodeOrEdge();
    }

    /**
     * 解析子图
     */
    private SubgraphNode parseSubgraph() {
        skipNewlines();

        // 解析子图标题
        StringBuilder title = new StringBuilder();
        while (!check(TokenType.NEWLINE) && !isAtEnd()) {
            title.append(peek().getValue());
            if (!match(TokenType.BRACKET_OPEN, TokenType.BRACKET_CLOSE,
                    TokenType.BRACE_OPEN, TokenType.BRACE_CLOSE,
                    TokenType.PAREN_OPEN, TokenType.PAREN_CLOSE)) {
                advance();
            }
        }

        skipNewlines();

        // 解析子图内的语句
        List<ASTNode> statements = new ArrayList<>();
        while (!isAtEnd() && !check(TokenType.END)) {
            skipNewlines();
            if (check(TokenType.END)) {
                break;
            }
            ASTNode statement = parseStatement();
            if (statement != null) {
                statements.add(statement);
            }
            skipNewlines();
        }

        // 消费 end
        if (!match(TokenType.END)) {
            throw new ParseException("子图缺少 'end' 关键字", getCurrentLine(), getCurrentColumn());
        }

        return SubgraphNode.builder()
                .title(title.toString().trim())
                .statements(statements)
                .build();
    }

    /**
     * 解析节点或边
     */
    private ASTNode parseNodeOrEdge() {
        // 必须以标识符开始
        if (!check(TokenType.IDENTIFIER)) {
            throw new ParseException("期望节点标识符", getCurrentLine(), getCurrentColumn());
        }

        String nodeId = advance().getValue();

        skipWhitespaceTokens();

        // 检查是否有形状定义
        if (checkNodeShape()) {
            FlowchartNode node = parseNodeShape(nodeId);
            nodeRegistry.put(nodeId, node);

            skipWhitespaceTokens();

            // 检查是否后面跟着箭头（定义边）
            if (check(TokenType.ARROW)) {
                return parseEdge(nodeId);
            }

            return node;
        }

        // 没有形状定义，可能是简单的边定义
        if (check(TokenType.ARROW)) {
            // 确保节点已经定义过
            if (!nodeRegistry.containsKey(nodeId)) {
                // 隐式定义为矩形节点
                FlowchartNode node = FlowchartNode.builder()
                        .id(nodeId)
                        .label(nodeId)
                        .shape(NodeShape.RECTANGLE)
                        .build();
                nodeRegistry.put(nodeId, node);
            }
            return parseEdge(nodeId);
        }

        throw new ParseException("期望节点形状或箭头", getCurrentLine(), getCurrentColumn());
    }

    /**
     * 检查是否是节点形状
     */
    private boolean checkNodeShape() {
        return check(TokenType.BRACKET_OPEN) ||
                check(TokenType.BRACE_OPEN) ||
                check(TokenType.PAREN_OPEN);
    }

    /**
     * 解析节点形状
     */
    private FlowchartNode parseNodeShape(String nodeId) {
        NodeShape shape;
        String label;

        if (match(TokenType.BRACKET_OPEN)) {
            // [文本] - 矩形
            label = parseTextUntil(TokenType.BRACKET_CLOSE);
            if (!match(TokenType.BRACKET_CLOSE)) {
                throw new ParseException("期望 ']'", getCurrentLine(), getCurrentColumn());
            }
            shape = NodeShape.RECTANGLE;

        } else if (match(TokenType.BRACE_OPEN)) {
            // {文本} - 菱形
            label = parseTextUntil(TokenType.BRACE_CLOSE);
            if (!match(TokenType.BRACE_CLOSE)) {
                throw new ParseException("期望 '}'", getCurrentLine(), getCurrentColumn());
            }
            shape = NodeShape.DIAMOND;

        } else if (match(TokenType.PAREN_OPEN)) {
            // 检查是否是 (( 或 ([
            if (match(TokenType.PAREN_OPEN)) {
                // ((文本)) - 圆形
                label = parseTextUntil(TokenType.PAREN_CLOSE);
                if (!match(TokenType.PAREN_CLOSE)) {
                    throw new ParseException("期望 ')'", getCurrentLine(), getCurrentColumn());
                }
                if (!match(TokenType.PAREN_CLOSE)) {
                    throw new ParseException("期望 ')'", getCurrentLine(), getCurrentColumn());
                }
                shape = NodeShape.CIRCLE;

            } else if (match(TokenType.BRACKET_OPEN)) {
                // ([文本]) - 圆角矩形
                label = parseTextUntil(TokenType.BRACKET_CLOSE);
                if (!match(TokenType.BRACKET_CLOSE)) {
                    throw new ParseException("期望 ']'", getCurrentLine(), getCurrentColumn());
                }
                if (!match(TokenType.PAREN_CLOSE)) {
                    throw new ParseException("期望 ')'", getCurrentLine(), getCurrentColumn());
                }
                shape = NodeShape.ROUND_RECTANGLE;

            } else {
                throw new ParseException("未识别的节点形状", getCurrentLine(), getCurrentColumn());
            }

        } else {
            throw new ParseException("期望节点形状", getCurrentLine(), getCurrentColumn());
        }

        return FlowchartNode.builder()
                .id(nodeId)
                .label(label)
                .shape(shape)
                .build();
    }

    /**
     * 解析文本直到指定的结束 token
     */
    private String parseTextUntil(TokenType endType) {
        StringBuilder text = new StringBuilder();

        while (!check(endType) && !isAtEnd()) {
            Token token = peek();
            text.append(token.getValue());
            advance();
        }

        return text.toString().trim();
    }

    /**
     * 解析边
     */
    private EdgeNode parseEdge(String fromId) {
        if (!match(TokenType.ARROW)) {
            throw new ParseException("期望箭头 '-->'", getCurrentLine(), getCurrentColumn());
        }

        skipWhitespaceTokens();

        // 检查是否有标签 -->|label|
        String edgeLabel = "";
        if (match(TokenType.PIPE)) {
            edgeLabel = parseTextUntil(TokenType.PIPE);
            if (!match(TokenType.PIPE)) {
                throw new ParseException("期望 '|'", getCurrentLine(), getCurrentColumn());
            }
            skipWhitespaceTokens();
        }

        // 解析目标节点
        if (!check(TokenType.IDENTIFIER)) {
            throw new ParseException("期望目标节点标识符", getCurrentLine(), getCurrentColumn());
        }

        String toId = advance().getValue();

        // 检查目标节点是否有形状定义
        skipWhitespaceTokens();
        if (checkNodeShape()) {
            FlowchartNode targetNode = parseNodeShape(toId);
            nodeRegistry.put(toId, targetNode);
        } else {
            // 隐式定义为矩形节点
            if (!nodeRegistry.containsKey(toId)) {
                FlowchartNode node = FlowchartNode.builder()
                        .id(toId)
                        .label(toId)
                        .shape(NodeShape.RECTANGLE)
                        .build();
                nodeRegistry.put(toId, node);
            }
        }

        return EdgeNode.builder()
                .fromId(fromId)
                .toId(toId)
                .label(edgeLabel)
                .build();
    }

    /**
     * 跳过换行符
     */
    private void skipNewlines() {
        while (match(TokenType.NEWLINE)) {
            // 继续消费换行符
        }
    }

    /**
     * 跳过空白 token（不包括换行）
     */
    private void skipWhitespaceTokens() {
        // 当前词法分析器已经处理了空白，这里暂时不需要
    }

    /**
     * 检查当前 token 类型
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().getType() == type;
    }

    /**
     * 匹配并消费 token
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前 token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * 获取前一个 token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 前进到下一个 token
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    /**
     * 是否到达末尾
     */
    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    /**
     * 获取当前行号
     */
    private int getCurrentLine() {
        return isAtEnd() ? -1 : peek().getLine();
    }

    /**
     * 获取当前列号
     */
    private int getCurrentColumn() {
        return isAtEnd() ? -1 : peek().getColumn();
    }

    /**
     * 获取节点注册表（用于语义分析）
     */
    public Map<String, FlowchartNode> getNodeRegistry() {
        return nodeRegistry;
    }
}
