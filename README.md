# Mermaid 流程图解析器

一个将 Mermaid 语法的流程图转换为点线结构 JSON 的 Java 解析器。

## 📋 功能特性

### ✅ 已支持的特性

- **节点形状**
  - `[文本]` - 矩形
  - `{文本}` - 菱形（决策节点）
  - `((文本))` - 圆形
  - `([文本])` - 圆角矩形

- **连接线**
  - `-->` - 实线箭头
  - `-->|标签|` - 带标签的箭头

- **流程图方向**
  - `TD` / `TB` - 从上到下
  - `LR` - 从左到右
  - `RL` - 从右到左
  - `BT` - 从下到上

- **子图**
  - `subgraph ... end` - 子图定义

## 🏗️ 技术架构

基于经典的编译原理技术栈：

```
Mermaid 文本
    ↓
词法分析器 (Lexer) - 将文本转换为 Token 序列
    ↓
语法分析器 (Parser) - 构建抽象语法树 (AST)
    ↓
转换器 (Transformer) - 转换为点线结构 JSON
    ↓
点线 JSON
```

### 核心组件

- **com.gaibu.flowlab.parser.lexer** - 词法分析
  - `MermaidLexer` - 词法分析器
  - `Token` / `TokenType` - Token 定义

- **com.gaibu.flowlab.parser.syntax** - 语法分析
  - `MermaidParser` - 递归下降语法分析器

- **com.gaibu.flowlab.parser.ast** - 抽象语法树
  - `FlowchartAST` - 流程图 AST
  - `FlowchartNode` - 节点
  - `EdgeNode` - 边
  - `SubgraphNode` - 子图

- **com.gaibu.flowlab.transformer** - 转换器
  - `MermaidTransformer` - AST 到 JSON 转换

- **com.gaibu.flowlab.service** - 服务层
  - `FlowParserService` - 统一解析服务入口

## 🚀 快速开始

### 基本使用

```java
import com.gaibu.flowlab.service.FlowParserService;
import com.gaibu.flowlab.transformer.model.FlowGraph;

// 创建解析服务
FlowParserService service = new FlowParserService();

// Mermaid 流程图文本
String mermaid = """
    flowchart TD
        A[开始] --> B{判断}
        B -->|是| C[处理A]
        B -->|否| D[处理B]
        C --> E((结束))
        D --> E
    """;

// 方式 1：解析为 FlowGraph 对象
FlowGraph graph = service.parse(mermaid);
System.out.println("节点数: " + graph.getNodes().size());
System.out.println("边数: " + graph.getEdges().size());

// 方式 2：解析为格式化 JSON 字符串
String json = service.parseToJson(mermaid);
System.out.println(json);

// 方式 3：解析为紧凑 JSON 字符串
String compactJson = service.parseToCompactJson(mermaid);

// 方式 4：验证语法
boolean isValid = service.validate(mermaid);
```

### 输出示例

对于上面的 Mermaid 流程图，输出的 JSON 结构如下：

```json
{
  "nodes": [
    {
      "id": "A",
      "label": "开始",
      "type": "rectangle",
      "shape": "rectangle"
    },
    {
      "id": "B",
      "label": "判断",
      "type": "diamond",
      "shape": "diamond"
    },
    {
      "id": "C",
      "label": "处理A",
      "type": "rectangle",
      "shape": "rectangle"
    },
    {
      "id": "D",
      "label": "处理B",
      "type": "rectangle",
      "shape": "rectangle"
    },
    {
      "id": "E",
      "label": "结束",
      "type": "circle",
      "shape": "circle"
    }
  ],
  "edges": [
    {
      "from": "A",
      "to": "B",
      "label": ""
    },
    {
      "from": "B",
      "to": "C",
      "label": "是"
    },
    {
      "from": "B",
      "to": "D",
      "label": "否"
    },
    {
      "from": "C",
      "to": "E",
      "label": ""
    },
    {
      "from": "D",
      "to": "E",
      "label": ""
    }
  ]
}
```

## 🧪 测试

项目包含完整的单元测试：

```bash
# 运行所有测试
./mvnw test

# 运行特定测试
./mvnw test -Dtest=MermaidLexerTest
./mvnw test -Dtest=FlowParserServiceTest
```

测试覆盖：
- ✅ 词法分析器测试（8个测试用例）
- ✅ 服务层集成测试（13个测试用例）
- ✅ 所有测试通过率：100%

## 📦 项目结构

```
src/main/java/com/gaibu/flowlab/
├── exception/              # 异常定义
│   ├── ParseException.java
│   └── ValidationException.java
├── parser/                 # 解析器
│   ├── lexer/             # 词法分析
│   │   ├── Token.java
│   │   ├── TokenType.java
│   │   └── MermaidLexer.java
│   ├── ast/               # 抽象语法树
│   │   ├── ASTNode.java
│   │   ├── FlowchartAST.java
│   │   ├── FlowchartNode.java
│   │   ├── EdgeNode.java
│   │   ├── SubgraphNode.java
│   │   └── NodeShape.java
│   └── syntax/            # 语法分析
│       └── MermaidParser.java
├── transformer/           # 转换器
│   ├── model/            # 输出数据模型
│   │   ├── Node.java
│   │   ├── Edge.java
│   │   └── FlowGraph.java
│   └── MermaidTransformer.java
└── service/              # 服务层
    └── FlowParserService.java
```

## 🔧 技术栈

- **Java 17**
- **Spring Boot 4.0.1**
- **Jackson** - JSON 序列化
- **Lombok** - 减少样板代码
- **JUnit 5** - 单元测试
- **AssertJ** - 测试断言

## 📚 技术方案

详细的技术设计文档请查看：[doc/Mermaid流程图解析器技术方案.md](doc/Mermaid流程图解析器技术方案.md)

## ⚠️ 当前限制

暂不支持的 Mermaid 特性：
- ❌ 虚线、粗线等其他连接线类型
- ❌ 样式定义（classDef、style）
- ❌ 点击事件
- ❌ 注释

## 🎯 使用场景

这个解析器适合以下场景：
1. **流程引擎** - 将 Mermaid 流程图转换为可执行的流程定义
2. **可视化编辑器** - 为流程图编辑器提供数据支持
3. **流程分析** - 分析流程图结构、节点关系等
4. **文档生成** - 自动化生成流程文档

## 📄 许可证

本项目遵循 MIT 许可证。
