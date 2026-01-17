# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

FlowLab 是一个 Mermaid 流程图解析器，将 Mermaid 语法的流程图转换为点线结构 JSON。项目基于经典的编译原理技术栈实现，包含词法分析、语法分析、AST 构建和转换四个阶段。

**核心能力**：
- 解析 Mermaid flowchart 语法（支持多种节点形状、连接线、子图）
- 输出标准化的点线结构 JSON（nodes + edges）
- 为流程引擎提供基础解析能力

## 构建和测试命令

### 编译项目
```bash
./mvnw clean compile
```

### 运行所有测试
```bash
./mvnw test
```

### 运行单个测试类
```bash
./mvnw test -Dtest=MermaidLexerTest
./mvnw test -Dtest=FlowParserServiceTest
```

### 运行单个测试方法
```bash
./mvnw test -Dtest=MermaidLexerTest#testTokenizeBasicFlowchart
```

### 打包项目
```bash
./mvnw clean package
```

## 核心架构

### 编译流程（四阶段）

```
Mermaid 文本 → 词法分析 → 语法分析 → AST 转换 → 点线 JSON
```

1. **词法分析（Lexer）**：`MermaidLexer` 将文本转换为 Token 序列
2. **语法分析（Parser）**：`MermaidParser` 使用递归下降法构建 AST
3. **AST 表示**：`FlowchartAST` 包含节点、边、子图的树形结构
4. **转换器（Transformer）**：`MermaidTransformer` 将 AST 转换为 `FlowGraph`（点线结构）

### 关键设计模式

- **递归下降解析**：`MermaidParser` 使用 LL(1) 向前看 1 个 Token
- **节点注册表模式**：Parser 维护 `nodeRegistry` 用于节点去重和管理
- **两遍遍历策略**：Transformer 第一遍收集节点，第二遍处理边和子图
- **服务门面模式**：`FlowParserService` 提供统一的解析入口

### 包结构说明

- `parser.lexer`：词法分析，Token 定义（18 种 TokenType）
- `parser.syntax`：语法分析，递归下降解析器
- `parser.ast`：AST 节点定义（FlowchartAST、FlowchartNode、EdgeNode、SubgraphNode）
- `transformer`：AST 到 FlowGraph 的转换逻辑
- `transformer.model`：输出数据模型（Node、Edge、FlowGraph）
- `service`：统一服务入口（FlowParserService）
- `exception`：自定义异常（ParseException、ValidationException）

## 重要实现细节

### 节点形状映射

| Mermaid 语法 | NodeShape 枚举 | 输出 type/shape |
|-------------|---------------|----------------|
| `[文本]` | RECTANGLE | rectangle |
| `{文本}` | DIAMOND | diamond |
| `((文本))` | CIRCLE | circle |
| `([文本])` | ROUND_RECTANGLE | round_rectangle |

### 支持的语法特性

- **流程方向**：TD/TB/LR/RL/BT
- **连接线**：`-->` 和 `-->|标签|`
- **子图**：`subgraph 标题 ... end`
- **节点定义**：支持内联定义和引用

### 不支持的特性

- 虚线、粗线等其他连接线类型
- 样式定义（classDef、style）
- 点击事件和注释

## 扩展开发指南

### 添加新的节点形状

1. 在 `NodeShape` 枚举中添加新形状
2. 在 `MermaidLexer` 中添加对应的 Token 识别逻辑
3. 在 `MermaidParser.parseNodeDefinition()` 中添加解析逻辑
4. 在 `MermaidTransformer` 中添加形状映射

### 添加新的连接线类型

1. 在 `TokenType` 中定义新的 Token 类型
2. 在 `MermaidLexer.tokenize()` 中添加识别逻辑
3. 在 `MermaidParser.parseEdgeDefinition()` 中添加解析逻辑

### 测试策略

- 词法分析器测试：验证 Token 序列的正确性
- 服务层集成测试：端到端验证解析结果
- 使用 AssertJ 进行流式断言

## 技术栈

- Java 17
- Spring Boot 4.0.1（仅用于依赖管理，未使用 Web 功能）
- Jackson（JSON 序列化）
- Lombok（减少样板代码）
- JUnit 5 + AssertJ（测试）

## 相关文档

- `doc/Mermaid流程图解析器技术方案.md`：详细技术设计文档
- `doc/流程引擎设计文档.md`：基于解析器的流程引擎设计方案
- `doc/子图测试用例文档.md`：子图功能测试用例
