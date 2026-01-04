package com.gaibu.flowlab.parser.ast;

/**
 * 节点形状枚举
 */
public enum NodeShape {
    RECTANGLE("rectangle"),          // [文本]
    ROUND_RECTANGLE("round_rectangle"),  // ([文本])
    DIAMOND("diamond"),              // {文本}
    CIRCLE("circle"),                // ((文本))
    HEXAGON("hexagon"),              // {{文本}}
    PARALLELOGRAM("parallelogram"),  // [/文本/]
    TRAPEZOID("trapezoid");          // [\\文本\\]

    private final String value;

    NodeShape(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
