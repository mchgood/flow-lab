package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.WorkerExecutor;
import com.gaibu.flowlab.engine.api.interceptor.NodeInterceptor;
import com.gaibu.flowlab.engine.api.interceptor.WorkflowInterceptor;
import com.gaibu.flowlab.engine.exception.NoRouteMatchedException;
import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import com.gaibu.flowlab.engine.model.NodeExecutionContext;
import com.gaibu.flowlab.engine.model.NodeResult;
import com.gaibu.flowlab.engine.model.WorkflowExecutionResult;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultWorkflowExecutor} 行为测试。
 *
 * <p>覆盖分支路由、并行、子流程、重试、超时及拦截器触发。
 */
class DefaultWorkflowExecutorTest {

    private final com.gaibu.flowlab.parser.impl.DefaultWorkflowDefinitionParser definitionParser =
            new com.gaibu.flowlab.parser.impl.DefaultWorkflowDefinitionParser();

    /**
     * 条件路由应按 First-Match-Wins，未命中分支节点应被取消。
     */
    @Test
    void executeConditionalWithFirstMatchWins() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                A{Judge}
                B[High]
                C[Low]
                D[Done]
                A -->|amount > 100| B
                A -->|default| C
                B --> D
                C --> D
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(definitions, worker);

        WorkflowExecutionResult result = executor.execute("wf", Map.of("amount", 120));

        assertThat(result.getState()).isEqualTo(ExecutionState.SUCCESS);
        assertThat(result.getUnitStates().get("A")).isEqualTo(ExecutionState.SUCCESS);
        assertThat(result.getUnitStates().get("B")).isEqualTo(ExecutionState.SUCCESS);
        assertThat(result.getUnitStates().get("C")).isEqualTo(ExecutionState.CANCELLED);
        assertThat(result.getUnitStates().get("D")).isEqualTo(ExecutionState.SUCCESS);
        assertThat(worker.callCount("C")).isEqualTo(0);
    }

    /**
     * 无 default 且条件全部不命中时，流程应失败并给出无路由异常。
     */
    @Test
    void failWhenNoRouteMatched() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                A{Judge}
                B[High]
                C[Low]
                A -->|amount > 100| B
                A -->|amount < 0| C
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(definitions, worker);

        WorkflowExecutionResult result = executor.execute("wf", Map.of("amount", 10));

        assertThat(result.getState()).isEqualTo(ExecutionState.FAILED);
        assertThat(result.getError()).isInstanceOf(NoRouteMatchedException.class);
    }

    /**
     * 并行 ANY 模式命中首个成功后，其余成员应被取消。
     */
    @Test
    void executeParallelAnyAndCancelOthers() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                S[Start]
                %% @parallel_group node=A group=g1
                %% @parallel node=A group=g1 mode=ANY any_complete_to=E
                A[Fast]
                %% @parallel_group node=B group=g1
                B[Slow]
                E[End]
                S --> A
                S --> B
                A --> E
                B --> E
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(definitions, worker);

        WorkflowExecutionResult result = executor.execute("wf", Map.of());

        assertThat(result.getState()).isEqualTo(ExecutionState.SUCCESS);
        assertThat(result.getUnitStates().get("A")).isEqualTo(ExecutionState.SUCCESS);
        assertThat(result.getUnitStates().get("B")).isEqualTo(ExecutionState.CANCELLED);
        assertThat(result.getUnitStates().get("E")).isEqualTo(ExecutionState.SUCCESS);
        assertThat(worker.callCount("B")).isEqualTo(0);
    }

    /**
     * 子流程应可执行，且上游节点失败后可按 retry 重试成功。
     */
    @Test
    void executeSubflowWithRetry() {
        String markdown = """
                # child
                ```mermaid
                flowchart TD
                C[ChildTask]
                ```
                # main
                ```mermaid
                flowchart TD
                %% @retry node=S retry=1
                S[Start]
                %% @subflow node=CALL ref=child in.input=seed out.answer=childAnswer
                CALL[[CallChild]]
                E[End]
                S --> CALL
                CALL --> E
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(definitions, worker);
        worker.script("S",
                NodeResult.failed("first failed", null),
                NodeResult.success(Map.of()));
        worker.handler("C", req -> NodeResult.success(Map.of(
                "childAnswer", "OK",
                "echoInput", req.getVariables().get("input")
        )));

        WorkflowExecutionResult result = executor.execute("main", Map.of("seed", 7));

        assertThat(result.getState()).isEqualTo(ExecutionState.SUCCESS);
        assertThat(worker.callCount("S")).isEqualTo(2);
        assertThat(result.getVariables().get("answer")).isEqualTo("OK");
    }

    /**
     * 节点超时应转为 TIMEOUT，并在默认策略下导致流程超时终止。
     */
    @Test
    void timeoutShouldFailWorkflow() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                %% @timeout node=A ms=5
                A[Task]
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(definitions, worker);
        worker.handler("A", request -> {
            try {
                Thread.sleep(20L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return NodeResult.success(Map.of());
        });

        WorkflowExecutionResult result = executor.execute("wf", Map.of());

        assertThat(result.getState()).isEqualTo(ExecutionState.TIMEOUT);
        assertThat(result.getUnitStates().get("A")).isEqualTo(ExecutionState.TIMEOUT);
    }

    /**
     * 流程拦截器应触发 before/success 回调。
     */
    @Test
    void invokeWorkflowInterceptorCallbacks() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                A[Task]
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        CaptureWorkflowInterceptor interceptor = new CaptureWorkflowInterceptor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(
                definitions,
                worker,
                null,
                List.of(interceptor),
                List.of()
        );

        WorkflowExecutionResult result = executor.execute("wf", Map.of("k", "v"));

        assertThat(result.getState()).isEqualTo(ExecutionState.SUCCESS);
        assertThat(interceptor.beforeEvents).containsExactly("wf");
        assertThat(interceptor.successEvents).containsExactly("wf");
        assertThat(interceptor.failureEvents).isEmpty();
    }

    /**
     * 节点拦截器应触发 before/success/failure，并携带 attempt 信息。
     */
    @Test
    void invokeNodeInterceptorCallbacks() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                A[Task A]
                B[Task B]
                A --> B
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        worker.script("A", NodeResult.success(Map.of()));
        worker.script("B", NodeResult.failed("b failed", null));
        CaptureNodeInterceptor interceptor = new CaptureNodeInterceptor();
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(
                definitions,
                worker,
                null,
                List.of(),
                List.of(interceptor)
        );

        WorkflowExecutionResult result = executor.execute("wf", Map.of());

        assertThat(result.getState()).isEqualTo(ExecutionState.FAILED);
        assertThat(interceptor.beforeEvents).containsExactly("A#0", "B#0");
        assertThat(interceptor.successEvents).containsExactly("A#0");
        assertThat(interceptor.failureEvents).containsExactly("B#0");
    }

    /**
     * 流程拦截器可按 workflowId 过滤作用范围。
     */
    @Test
    void workflowInterceptorCanFilterByWorkflowId() {
        String markdown = """
                # wfA
                ```mermaid
                flowchart TD
                A[Task A]
                ```
                # wfB
                ```mermaid
                flowchart TD
                B[Task B]
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        CaptureWorkflowInterceptor interceptor = new CaptureWorkflowInterceptor();
        interceptor.allowWorkflow("wfB");
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(
                definitions,
                worker,
                null,
                List.of(interceptor),
                List.of()
        );

        executor.execute("wfA", Map.of());
        executor.execute("wfB", Map.of());

        assertThat(interceptor.beforeEvents).containsExactly("wfB");
        assertThat(interceptor.successEvents).containsExactly("wfB");
        assertThat(interceptor.failureEvents).isEmpty();
    }

    /**
     * 节点拦截器可按 workflowId + nodeId 双维度过滤。
     */
    @Test
    void nodeInterceptorCanFilterByWorkflowAndNode() {
        String markdown = """
                # wf
                ```mermaid
                flowchart TD
                A[Task A]
                B[Task B]
                A --> B
                ```
                """;

        List<WorkflowDefinition> definitions = definitionParser.parse(markdown);
        StubWorkerExecutor worker = new StubWorkerExecutor();
        worker.script("A", NodeResult.success(Map.of()));
        worker.script("B", NodeResult.success(Map.of()));
        CaptureNodeInterceptor interceptor = new CaptureNodeInterceptor();
        interceptor.allow("wf", "B");
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(
                definitions,
                worker,
                null,
                List.of(),
                List.of(interceptor)
        );

        executor.execute("wf", Map.of());

        assertThat(interceptor.beforeEvents).containsExactly("B#0");
        assertThat(interceptor.successEvents).containsExactly("B#0");
        assertThat(interceptor.failureEvents).isEmpty();
    }

    /**
     * 测试用 worker：支持脚本结果与自定义 handler，便于构造执行路径。
     */
    private static final class StubWorkerExecutor implements WorkerExecutor {

        private final Map<String, ArrayDeque<NodeResult>> scriptedResults = new HashMap<>();
        private final Map<String, Function<NodeExecutionContext, NodeResult>> handlers = new HashMap<>();
        private final Map<String, Integer> callCountByNode = new HashMap<>();

        /**
         * 先走 handler，再走脚本队列，最后返回默认 success。
         */
        @Override
        public NodeResult execute(NodeExecutionContext context) {
            String nodeId = context.getNodeId();
            callCountByNode.put(nodeId, callCountByNode.getOrDefault(nodeId, 0) + 1);

            Function<NodeExecutionContext, NodeResult> handler = handlers.get(nodeId);
            if (handler != null) {
                return handler.apply(context);
            }

            ArrayDeque<NodeResult> queue = scriptedResults.get(nodeId);
            if (queue != null && !queue.isEmpty()) {
                return queue.removeFirst();
            }
            return NodeResult.success(Map.of());
        }

        private void script(String nodeId, NodeResult... results) {
            ArrayDeque<NodeResult> queue = scriptedResults.computeIfAbsent(nodeId, key -> new ArrayDeque<>());
            for (NodeResult result : results) {
                queue.addLast(result);
            }
        }

        private void handler(String nodeId, Function<NodeExecutionContext, NodeResult> handler) {
            handlers.put(nodeId, handler);
        }

        private int callCount(String nodeId) {
            return callCountByNode.getOrDefault(nodeId, 0);
        }
    }

    /**
     * 流程拦截器测试桩：记录回调触发轨迹。
     */
    private static final class CaptureWorkflowInterceptor implements WorkflowInterceptor {
        private final List<String> beforeEvents = new ArrayList<>();
        private final List<String> successEvents = new ArrayList<>();
        private final List<String> failureEvents = new ArrayList<>();
        private final List<String> allowedWorkflows = new ArrayList<>();

        /**
         * 用于配置仅命中特定流程。
         */
        private void allowWorkflow(String workflowId) {
            allowedWorkflows.add(workflowId);
        }

        @Override
        public boolean supportsWorkflow(String workflowId) {
            if (allowedWorkflows.isEmpty()) {
                return true;
            }
            return allowedWorkflows.contains(workflowId);
        }

        @Override
        public void before(String workflowId, Map<String, Object> variables) {
            beforeEvents.add(workflowId);
        }

        @Override
        public void onSuccess(WorkflowExecutionResult result) {
            successEvents.add(result.getWorkflowId());
        }

        @Override
        public void onFailure(String workflowId, Map<String, Object> variables, ExecutionState state, Throwable error) {
            failureEvents.add(workflowId);
        }
    }

    /**
     * 节点拦截器测试桩：记录回调触发轨迹。
     */
    private static final class CaptureNodeInterceptor implements NodeInterceptor {
        private final List<String> beforeEvents = new ArrayList<>();
        private final List<String> successEvents = new ArrayList<>();
        private final List<String> failureEvents = new ArrayList<>();
        private final List<String> allowedPairs = new ArrayList<>();

        /**
         * 配置允许触发的 workflow-node 组合。
         */
        private void allow(String workflowId, String nodeId) {
            allowedPairs.add(pair(workflowId, nodeId));
        }

        @Override
        public boolean supportsNode(String workflowId, String nodeId) {
            if (allowedPairs.isEmpty()) {
                return true;
            }
            return allowedPairs.contains(pair(workflowId, nodeId));
        }

        @Override
        public void before(NodeExecutionContext context) {
            beforeEvents.add(key(context));
        }

        @Override
        public void onSuccess(NodeExecutionContext context, NodeResult result) {
            successEvents.add(key(context));
        }

        @Override
        public void onFailure(NodeExecutionContext context, NodeResult result) {
            failureEvents.add(key(context));
        }

        private String key(NodeExecutionContext context) {
            return context.getNodeId() + "#" + context.getAttempt();
        }

        private String pair(String workflowId, String nodeId) {
            return workflowId + "::" + nodeId;
        }
    }
}
