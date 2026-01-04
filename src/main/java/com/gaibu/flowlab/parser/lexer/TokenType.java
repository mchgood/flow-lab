package com.gaibu.flowlab.parser.lexer;

/**
 * Token 类型枚举
 */
public enum TokenType {

    // 关键字
    FLOWCHART,          // flowchart
    GRAPH,              // graph
    SUBGRAPH,           // subgraph
    END,                // end

    // 方向
    TD,                 // Top Down
    TB,                 // Top to Bottom (同 TD)
    LR,                 // Left to Right
    RL,                 // Right to Left
    BT,                 // Bottom to Top

    // 节点形状标记
    BRACKET_OPEN,       // [
    BRACKET_CLOSE,      // ]
    BRACE_OPEN,         // {
    BRACE_CLOSE,        // }
    PAREN_OPEN,         // (
    PAREN_CLOSE,        // )

    // 连接符
    ARROW,              // -->
    ARROW_WITH_TEXT,    // -->|text|
    PIPE,               // |

    // 其他
    IDENTIFIER,         // 标识符（节点 ID）
    TEXT,               // 文本内容
    NEWLINE,            // 换行
    EOF                 // 文件结束
}
