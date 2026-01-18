# Mermaid 流程图解析器技术方案

## 1. 目标与范围
将 Mermaid `flowchart/graph` 语法解析为点线结构的 `FlowGraph`，为流程引擎提供结构化数据。当前支持：
- 节点形状：`[文本]`（矩形）、`{文本}`（菱形）、`((文本))`（圆形）、`([文本])`（圆角矩形）
- 连接线：`-->` 与 `-->|标签|`
- 方向：`TD/TB/LR/RL/BT`
- 子图：`subgraph ... end`

未支持：虚线/粗线、样式定义（classDef/style）、点击事件、注释语法等。

## 2. 解析流程
```
Mermaid 文本
  -> Lexer 生成 Token 序列
  -> Parser 构建 AST
  -> Transformer 生成 FlowGraph
  -> Service 输出对象/JSON
```

## 3. 模块与关键类
- `parser/lexer`: `MermaidLexer`, `Token`, `TokenType`
- `parser/syntax`: `MermaidParser`
- `parser/ast`: `FlowchartAST`, `FlowchartNode`, `EdgeNode`, `SubgraphNode`
- `transformer`: `MermaidTransformer`, `FlowGraph/Node/Edge`
- `service`: `FlowParserService` 作为统一入口

## 4. 词法规则（简化）
- 关键字：`flowchart`, `graph`, `subgraph`, `end`
- 方向：`TD/TB/LR/RL/BT`
- 标识符：以字母或 `_` 开头，后续字母/数字/`_`（支持中文等 Unicode 字母）
- 符号：`[ ] { } ( ) |`，箭头 `-->`
- 文本片段：数字与表达式符号（如 `#`, `>`, `<`, `=`, `?`, `+`, `-` 等）会作为 `TEXT` 参与标签拼接
- 空白：空格/制表符跳过，换行作为 `NEWLINE`

## 5. 语法与 AST（核心规则）
- 入口：`flowchart|graph` + 方向（可省略，默认 `TD`） + 语句列表
- 语句：节点定义、边定义、子图定义
- 边标签：`-->|标签|`，标签为原样文本拼接（不做表达式求值）
- 隐式节点：边中出现但未定义形状的节点会被创建为矩形节点
- 子图：`subgraph` 标题 + 若干语句 + `end`

## 6. 转换规则
- AST 节点映射为 `FlowGraph.nodes`：
  - `rectangle`, `diamond`, `circle`, `round_rectangle`
- AST 边映射为 `FlowGraph.edges`：
  - `from`, `to`, `label`
- 输出：`FlowParserService.parse()` 返回 `FlowGraph`；`parseToJson()` 返回格式化 JSON。

## 7. 约束与扩展点
- 解析器仅负责结构解析，不校验业务语义。
- 边标签可用于表达式，但表达式求值在流程引擎中完成。
- 扩展建议：支持更多连线类型、样式、注释以及更复杂的 Mermaid 语法。
