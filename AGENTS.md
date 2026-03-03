# Flow Lab Agent Conventions

本文件用于约束本仓库内的实现风格。后续任务默认遵循，除非用户明确覆盖。

## 通用

1. 新增代码前先遵循 `doc/` 下技术文档和约束文档。
2. 修改时保持模型命名与 `doc/技术方案.md`、`doc/类图.md` 一致。

## parser 包规范

1. `parser` 必须分包，不允许所有类平铺在根包。
2. 推荐结构：
   - `parser.model`：定义模型（Node/Edge/ProcessDefinition/枚举）
   - `parser.impl`：解析实现（如 MermaidProcessParser）
   - `parser.exception`：解析相关异常

## 实体类规范

1. 实体类默认使用 Lombok（如 `@Getter`、`@Setter`、`@NoArgsConstructor`）。
2. 禁止在实体类中保留重复的手写 getter/setter（除非有特殊逻辑）。
3. 类注释必须存在，字段注释必须存在，说明语义和使用边界。

## 枚举规范

1. 业务枚举需包含 `code` 与 `desc` 字段。
2. `desc` 用于描述枚举项语义，便于阅读、日志和对外映射。
3. 枚举建议使用 Lombok `@Getter` 暴露只读属性。
