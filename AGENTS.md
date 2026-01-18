# Repository Guidelines

## 项目结构与模块组织
- `src/main/java/com/gaibu/flowlab/` 为生产代码。主要目录：`parser/`（词法/语法/AST）、`transformer/`（AST 到 JSON 模型）、`engine/`（流程引擎）、`service/`（对外 API）、`exception/`（异常定义）。
- `src/main/resources/application.properties` 为运行时配置。
- `src/test/java/com/gaibu/flowlab/` 为测试代码，包结构与主代码一致。
- `doc/` 放置设计文档与测试设计说明。
- `target/` 为 Maven 产物目录，不要编辑或提交生成文件。

## 构建、测试与本地运行
- `./mvnw clean compile` — 清理并编译代码。
- `./mvnw test` — 运行全部 JUnit 5 测试。
- `./mvnw test -Dtest=MermaidLexerTest` — 运行指定测试类。
- `./mvnw spring-boot:run` — 本地启动 Spring Boot 应用。
- `./mvnw clean package` — 打包生成可运行 JAR。

## 编码风格与命名约定
- Java 17，4 空格缩进，一类一文件。
- 包名遵循 `com.gaibu.flowlab.*`，新代码放入最贴近的现有包。
- 类名使用 `UpperCamelCase`，方法/字段使用 `lowerCamelCase`。
- 测试类以 `*Test` 结尾，保持与现有测试一致。
- 未配置格式化或 Lint 工具，请与相邻文件保持风格一致。

## 测试指南
- 使用 JUnit 5、AssertJ、Spring Boot 测试依赖（见 `pom.xml`）。
- 修改 lexer/parser/transformer 时补充单元测试；变更 engine/service 行为时补充集成测试。
- 保持测试可重复、无外部依赖。

## 提交与 PR 规范
- 现有提交信息简短、祈使语气（中英文皆可）。提交应聚焦且可读，如“增加流程引擎测试代码”或“Add parser validation”。
- PR 建议包含：简要说明、问题/目标、已运行的测试命令、以及变更解析行为时的示例 Mermaid 与期望 JSON。

## 配置提示
- 避免在 `application.properties` 中提交敏感信息；可通过环境变量或本地覆盖配置处理。
