# Mermaid Workflow Engine

## 设计约束 QA 文档（v1.1）

---

# Q1：Execution 是否需要显式状态机？

### 问题

当前 Execution 仅有 `active:boolean`，但事件网关涉及 WAITING 状态，是否需要完整状态机？

---

### 结论

必须引入显式状态机。

---

### 设计规范

```java
enum ExecutionStatus {
    ACTIVE,
    WAITING,
    COMPLETED,
    CANCELLED,
    FAILED
}
```

---

### 规则

* WAITING：事件 / 异步等待
* COMPLETED：路径正常结束
* CANCELLED：被 Scope 或外部取消
* FAILED：未捕获异常

没有显式状态机，将导致竞态与双触发问题。

---

# Q2：EventSubscription 的 Map Key 如何设计？

### 问题

一个 Execution 可能订阅多个事件，如何避免覆盖？

---

### 结论

Key 必须是 subscriptionId，而非 executionId。

---

### 正确模型

```java
Map<String /*subscriptionId*/, EventSubscription> eventSubscriptionsById;
Map<String /*eventKey*/, Set<String> /*subscriptionId*/> eventSubscriptionsByKey;
```

EventSubscription 必须包含：

* subscriptionId
* executionId
* scopeId

---

### 规则

* 一个 Execution 可注册多个订阅
* 主存储按 subscriptionId 管理
* 按 eventKey 通过二级索引查找订阅
* 触发后执行逻辑 CAS
* 删除其他订阅

---

# Q3：OR Join 的 expectedExecutionIds 记录什么？

### 问题

记录“分支首节点”还是“Join前驱节点”？嵌套如何判定？

---

### 结论

记录 childExecutionId，而不是 nodeId。

---

### 规则

* OR Split 时记录生成的 childExecutionId 集合
* Join 时判断 childExecutionId 是否全部到达
* 不使用 nodeId 判定

避免嵌套误判。

---

# Q4：AND Join 的完成条件是什么？

### 问题

等待“到达 Join 节点”还是“分支全路径结束”？

---

### 结论

等待 scope 下所有 Execution 到达 Join 且状态为 COMPLETED。

---

### 判定逻辑

```java
scope.executions
    .allMatch(e -> 
        e.status == COMPLETED 
        && e.currentNodeId.equals(joinNodeId)
    );
```

子流程 / 异步任务未完成，不允许 Join。

---

# Q5：XOR default 与多 true 的规则？

### 结论

* 按出边定义顺序评估
* 命中第一个 true 即停止
* default 必须放最后
* 多 true → 选首个

---

### 约束

* Parser 必须保留出边顺序
* outgoingEdges 使用 List 而非 Map

---

# Q6：Scope timeout cancelStrategy=flow 指向哪个节点？

### 问题

timeout Execution 跳转目标未定义，且 Scope 状态存在语义冲突。

---

### 统一结论

* Scope 超时触发时，ScopeStatus 必须进入 `TIMED_OUT`
* cancelStrategy=flow/terminate 仅影响子 execution 的处理方式
* 不允许在 flow 情况下写成 CANCELLED

---

### 统一状态语义

| 触发原因  | ScopeStatus |
| ----- | ----------- |
| 正常汇聚  | COMPLETED   |
| 超时触发  | TIMED_OUT   |
| 外部取消  | CANCELLED   |
| 未捕获异常 | FAILED（可选）  |

---

### DSL 要求

```mermaid
G1 -->|timeout| TimeoutNode
```

若无 timeout 出边 → 抛异常。

---

### 执行流程（flow）

1. Scope timeout 触发
2. ScopeRuntime.status = TIMED_OUT
3. 取消所有 active execution
4. 创建新的 execution 指向 timeout 出边
5. 新 execution 继承父 scopeId

---

### 原则

> 超时是“原因”，Scope 状态必须反映真实原因。

---

# Q7：子流程变量作用域规则？

### 结论

共享变量 + 局部覆盖（copy-on-write）

---

### 模型

```java
class ExecutionContext {
    Map<String, Object> localVariables;
    ExecutionContext parent;
}
```

---

### 规则

* 读取：先 local，再 parent
* 子流程 FAILED → 传递至父 Scope
* ScopeBehavior 决定是否 cancelAll

---

# Q8：NodeBehavior 与 ScopeBehavior 冲突优先级？

### 结论

Scope 优先级高于 Node。

---

### 优先级矩阵

| 优先级 | 行为            |
| --- | ------------- |
| 1   | Scope timeout |
| 2   | Scope cancel  |
| 3   | Node timeout  |
| 4   | Node retry    |
| 5   | 正常完成          |

---

### 原则

> Scope 行为永远高于 Node 行为。

---

# Q9：SpEL 安全策略？

### 允许

* 变量访问
* 基本算术
* 比较运算
* 逻辑运算
* 集合访问

---

### 禁止

* T() 类型引用
* new
* Runtime
* 任意反射
* Bean 引用

---

### 实现

* 禁用 TypeLocator
* 禁用 BeanResolver
* 使用受限 EvaluationContext

---

# Q10：并发执行下 Map 是否需要线程安全？

### 结论

必须保证状态修改单线程。

---

### 推荐模式

* 单线程 Dispatcher
* 任务线程池执行 Task
* 状态变更统一回调调度线程

不允许多线程直接修改 ProcessInstance。

---

# Q11：事件竞速如何保证幂等？

### 统一结论

* 不要求使用 AtomicReference 或 compareAndSet 原语
* 必须实现“逻辑 CAS”（状态校验 + 条件转移）
* 所有触发逻辑必须运行在 Dispatcher 单线程内

---

### 正确实现方式

```java
if (execution.getStatus() != WAITING) {
    return; // 忽略重复或晚到事件
}

execution.setStatus(ACTIVE);
removeOtherSubscriptions(scopeId);
continueExecution(execution);
```

---

### 原则

> 单线程保证并发安全；
> 逻辑 CAS 保证幂等安全。

---

# Q12：Edge 查询性能问题？

### 结论

必须预建邻接索引。

---

### 结构

```java
Map<String, List<Edge>> outgoingIndex;
Map<String, List<Edge>> incomingIndex;
```

避免每次 O(n) 扫描。

---

# Q13：Execution / ScopeRuntime 状态存储是否使用 Atomic？

### 结论

* 使用普通字段
* 所有状态变更发生在 Dispatcher 单线程内
* 不使用 AtomicReference

---

### 原则

> 单线程调度模型是唯一一致性保障机制。

---

# Q14：SubProcess 定义引用方式

### 结论

* 使用 processId 引用外部 ProcessDefinition
* 不内嵌 DSL

---

### 示例

```
task1[[subProcessA]]
```

引擎通过：

```
ProcessDefinitionRepository.get("subProcessA")
```

加载定义。

---

### 原则

> 子流程独立实例，定义解耦。

---

# Q15：节点行为链组合顺序

### 固定顺序

```
NodeInterceptor.before
        ↓
async dispatch（可选）
        ↓
timeout guard 启动
        ↓
执行 FlowTask
        ↓
异常 → retry
        ↓
NodeInterceptor.onSuccess / onFailure
```

---

### 原则

> Scope 行为优先，Node 行为次之。

---

# Q16：首版引擎范围界定

### 结论

* 首版仅支持内存态
* 无持久化
* 无恢复
* 定时器使用本地调度器

---

### 不支持

* 崩溃恢复
* 分布式执行
* 外部持久化
* 多节点调度

---

# 总体统一原则（v1.1）

1. Scope 超时永远导致 ScopeStatus = TIMED_OUT
2. cancelStrategy 仅决定子 execution 处理策略
3. 单线程调度模型为一致性基础
4. 不使用物理 CAS，但必须实现逻辑 CAS
5. 所有事件注册/删除必须在 Dispatcher 内完成
6. Join 判定必须基于 executionId
7. Scope 行为优先级最高