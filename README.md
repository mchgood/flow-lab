# Mermaid 流程图解析器

将 Mermaid 流程图解析为点线 JSON，并提供内存版流程执行引擎。

## 目录
- [功能特性](#功能特性)
- [架构与组件](#架构与组件)
- [快速开始](#快速开始)
- [输出示例](#输出示例)
- [条件与标签约定](#条件与标签约定)
- [测试](#测试)
- [项目结构](#项目结构)
- [设计文档](#设计文档)
- [Roadmap](#roadmap)

## 功能特性
- **节点形状**：`[ ]` 矩形，`{ }` 菱形（决策），`(( ))` 圆形，`([ ])` 圆角矩形
- **连接线**：`-->` 箭头；`-->|标签|` 展示标签
- **流程方向**：`TD/TB`、`LR`、`RL`、`BT`
- **子图**：`subgraph ... end`

## 架构与组件
编译链路：Lexer → Parser → AST → Transformer → FlowGraph → Engine
- `parser.lexer`：`MermaidLexer`
- `parser.syntax`：`MermaidParser`
- `parser.ast`：`FlowchartAST/Node/Edge/Subgraph`
- `transformer`：`MermaidTransformer`（统一收集节点、生成点线结构）
- `service`：`FlowParserService`（解析入口，提供 JSON/校验）
- `engine`：内存流程引擎（执行器注册表、表达式、事件、仓库）

## 快速开始

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

## 输出示例

```json
{
  "nodes": [
    { "id": "A", "label": "开始", "type": "rectangle", "shape": "rectangle" },
    { "id": "B", "label": "判断", "type": "diamond", "shape": "diamond" },
    { "id": "C", "label": "处理A", "type": "rectangle", "shape": "rectangle" },
    { "id": "D", "label": "处理B", "type": "rectangle", "shape": "rectangle" },
    { "id": "E", "label": "结束", "type": "circle", "shape": "circle" }
  ],
  "edges": [
    { "from": "A", "to": "B", "label": "" },
    { "from": "B", "to": "C", "label": "是" },
    { "from": "B", "to": "D", "label": "否" },
    { "from": "C", "to": "E", "label": "" },
    { "from": "D", "to": "E", "label": "" }
  ]
}
```

## 条件与标签约定
- 展示标签：`-->|是|` → `label="是"`，不会求值。
- 条件表达式：`-->|? #amount > 1000|` → `condition="#amount > 1000"`，由表达式引擎判定。
- 未标记条件的边视为无条件分支。

## 测试
```bash
# 运行所有测试
./mvnw test

# 运行特定测试
./mvnw test -Dtest=MermaidLexerTest
./mvnw test -Dtest=MermaidParserTest
./mvnw test -Dtest=MermaidTransformerTest
./mvnw test -Dtest=FlowParserServiceTest
./mvnw test -Dtest=ProcessEngineTest
```

## 项目结构
```
flow-lab/
├── doc/                          # 设计与测试文档
├── mvnw*                         # Maven wrapper
├── pom.xml
├── src
│   ├── main/java/com/gaibu/flowlab
│   │   ├── FlowLabApplication.java
│   │   ├── parser/               # 词法/语法/AST
│   │   │   ├── lexer/            # MermaidLexer, Token, TokenType
│   │   │   ├── ast/              # FlowchartAST/Node/Edge/Subgraph, NodeShape
│   │   │   └── syntax/           # MermaidParser
│   │   ├── transformer/          # AST → FlowGraph
│   │   │   ├── MermaidTransformer.java
│   │   │   └── model/            # Node, Edge(condition/label), FlowGraph
│   │   ├── service/              # FlowParserService
│   │   ├── engine/               # 内存流程引擎
│   │   │   ├── core/             # ProcessEngine
│   │   │   ├── executor/         # Start/Task/Decision/End 及注册表
│   │   │   ├── expression/       # SpELExpressionEngine
│   │   │   ├── event/            # 事件与发布器
│   │   │   ├── model/            # ProcessDefinition/Instance/ExecutionContext...
│   │   │   ├── repository/       # 内存仓库
│   │   │   └── service/          # 定义/实例服务
│   │   └── exception/            # ParseException, ValidationException
│   ├── main/resources
│   │   └── application.properties
│   └── test/java/com/gaibu/flowlab
│       ├── parser/lexer|syntax   # MermaidLexerTest, MermaidParserTest
│       ├── transformer/          # MermaidTransformerTest
│       ├── service/              # FlowParserServiceTest
│       └── engine/               # 执行器、引擎、事件、仓库、集成测试
└── target/                       # Maven 编译产物（不需提交）
```

## 设计文档
- `doc/流程引擎设计文档.md`：执行引擎设计与演进
- `doc/测试用例设计方案.md`：测试分层与用例索引
- `doc/Mermaid流程图解析器技术方案.md`：解析与转换细节
### 流程引擎快速概览
- **执行模型**：内存同步执行，迭代调度（含最大步数与循环检测）
- **起止节点规则**：优先识别 `start`/`end` 标记；次优为无入边的圆形起点
- **条件分支**：边的 `condition`（来源 `|? expr|`）由 SpEL 评估；`label` 仅展示
- **执行器分发**：`NodeExecutorRegistry` 支持同形状多执行器，按 `validate` 选择
- **事件流**：`ProcessStarted/NodeStarted/NodeCompleted/ProcessCompleted`，监听器异常不阻断流程
- **仓库实现**：内存版 `ProcessDefinitionRepository`、`ProcessInstanceRepository`
- 详情参见 `doc/流程引擎设计文档.md`

## Roadmap
- 统一节点形状语法与枚举的支持策略
- start/end 显式标记与执行器注册完善
- 条件/标签配置开关与表达式异常降级
- 引擎并发/异步调度与持久化仓库接口化
