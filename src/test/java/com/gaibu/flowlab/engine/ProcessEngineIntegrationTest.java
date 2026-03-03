package com.gaibu.flowlab.engine;

import com.gaibu.flowlab.engine.api.impl.DefaultProcessEngine;
import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.interceptor.NodeInterceptor;
import com.gaibu.flowlab.engine.interceptor.ProcessInterceptor;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.task.FlowTask;
import com.gaibu.flowlab.engine.task.context.TaskContext;
import com.gaibu.flowlab.parser.ProcessParser;
import com.gaibu.flowlab.parser.impl.MermaidProcessParser;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessEngineIntegrationTest {

    private final ProcessParser parser = new MermaidProcessParser();

    @Test
    void shouldCompleteFullFlowThroughXorMatchedRoute() {
        String dsl = """
                flowchart TD
                S(Start) --> T1[Prepare]
                T1 --> G1{XOR}
                G1 -->|approved| A[Approve]
                G1 -->|default| R[Reject]
                A --> E(End)
                R --> E
                """;

        ProcessDefinition definition = parser.parse("p-xor-matched", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        RecordingProcessInterceptor processInterceptor = new RecordingProcessInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.addProcessInterceptor(processInterceptor);
        engine.deploy(definition);

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("approved", true);

        ProcessInstance instanceId = engine.start("p-xor-matched", vars);

        assertThat(instanceId.getId()).isNotBlank();
        assertThat(nodeInterceptor.byInstance.get(instanceId.getId())).containsExactly("S", "T1", "G1", "A", "E");
        assertThat(processInterceptor.beforeStartCount.get()).isEqualTo(1);
        assertThat(processInterceptor.completedCount.get()).isEqualTo(1);
        assertThat(processInterceptor.failedCount.get()).isEqualTo(0);
    }

    @Test
    void shouldRouteBySpelExpression() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{XOR}
                G1 -->|amount > 1000| A[Approve]
                G1 -->|default| R[Reject]
                A --> E(End)
                R --> E
                """;

        ProcessDefinition definition = parser.parse("p-xor-spel", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.deploy(definition);

        ProcessInstance i1 = engine.start("p-xor-spel", Map.of("amount", 1500));
        ProcessInstance i2 = engine.start("p-xor-spel", Map.of("amount", 100));

        assertThat(nodeInterceptor.byInstance.get(i1.getId())).containsExactly("S", "G1", "A", "E");
        assertThat(nodeInterceptor.byInstance.get(i2.getId())).containsExactly("S", "G1", "R", "E");
    }

    @Test
    void shouldCompleteParallelAndJoinFlow() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{AND}
                G1 --> A[TaskA]
                G1 --> B[TaskB]
                A --> J{AND}
                B --> J
                J --> E(End)
                """;

        ProcessDefinition definition = parser.parse("parallel-join", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.deploy(definition);

        ProcessInstance instanceId = engine.start("parallel-join", Map.of());

        List<String> visited = nodeInterceptor.byInstance.get(instanceId.getId());
        assertThat(visited).contains("S", "G1", "A", "B", "J", "E");
        assertThat(countOf(visited, "J")).isEqualTo(1);
    }

    @Test
    void shouldFailProcessWhenNodeInterceptorThrows() {
        String dsl = """
                flowchart TD
                S(Start) --> T1[RiskTask]
                T1 --> E(End)
                """;

        ProcessDefinition definition = parser.parse("exception-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        engine.addNodeInterceptor(new ThrowingNodeInterceptor("T1"));
        RecordingProcessInterceptor processInterceptor = new RecordingProcessInterceptor();
        engine.addProcessInterceptor(processInterceptor);
        engine.deploy(definition);

        assertThatThrownBy(() -> engine.start("exception-flow", Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom at node T1");

        assertThat(processInterceptor.beforeStartCount.get()).isEqualTo(1);
        assertThat(processInterceptor.completedCount.get()).isEqualTo(0);
        assertThat(processInterceptor.failedCount.get()).isEqualTo(1);
    }

    @Test
    void shouldIsolateMultipleInstances() {
        String dsl = """
                flowchart TD
                S(Start) --> G1{XOR}
                G1 -->|approved| A[Approve]
                G1 -->|default| R[Reject]
                A --> E(End)
                R --> E
                """;

        ProcessDefinition definition = parser.parse("multi-instance", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.deploy(definition);

        ProcessInstance i1 = engine.start("multi-instance", Map.of("approved", true));
        ProcessInstance i2 = engine.start("multi-instance", Map.of("approved", false));

        assertThat(i1.getId()).isNotEqualTo(i2.getId());
        assertThat(nodeInterceptor.byInstance.get(i1.getId())).containsExactly("S", "G1", "A", "E");
        assertThat(nodeInterceptor.byInstance.get(i2.getId())).containsExactly("S", "G1", "R", "E");
    }

    @Test
    void shouldInvokeNodeInterceptorsInRegistrationOrder() {
        String dsl = """
                flowchart TD
                S(Start) --> E(End)
                """;

        ProcessDefinition definition = parser.parse("interceptor-order", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        List<String> events = new ArrayList<>();
        engine.addNodeInterceptor(new OrderedNodeInterceptor("I1", events));
        engine.addNodeInterceptor(new OrderedNodeInterceptor("I2", events));
        engine.deploy(definition);

        engine.start("interceptor-order", Map.of());

        assertThat(events).containsExactly(
                "I1-before-S", "I2-before-S", "I1-success-S", "I2-success-S",
                "I1-before-E", "I2-before-E", "I1-success-E", "I2-success-E"
        );
    }

    @Test
    void shouldExecuteComplexFlowWithAllGateways() {
        String dsl = """
                flowchart TD
                S(Start) --> GX{XOR}
                GX -->|goComplex| GP{AND}
                GX -->|default| F[Fallback]
                GP --> A[TaskA]
                GP --> B[TaskB]
                A --> JP{AND}
                B --> JP
                JP --> GO{OR}
                GO -->|c1| O1[OrTask1]
                GO -->|c2| O2[OrTask2]
                GO -->|c3| O3[OrTask3]
                O1 --> JO{OR}
                O2 --> JO
                O3 --> JO
                JO --> OK[Approved]
                OK --> E(End)
                F --> E
                """;

        ProcessDefinition definition = parser.parse("complex-all-gateways", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.deploy(definition);

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("goComplex", true);
        vars.put("c1", true);
        vars.put("c2", true);
        vars.put("c3", true);

        ProcessInstance instanceId = engine.start("complex-all-gateways", vars);

        assertThat(engine.getInstanceStatus(instanceId.getId())).isEqualTo(InstanceStatus.COMPLETED);
        List<String> visited = nodeInterceptor.byInstance.get(instanceId.getId());
        assertThat(visited).contains("GX", "GP", "JP", "GO", "JO");
        assertThat(nodeInterceptor.byInstance.get(instanceId.getId())).contains("OK", "E");
    }

    @Test
    void shouldParseAndExecuteFlowWithAnnotationDirectives() {
        String dsl = """
                flowchart TD
                %% @node:T1 timeout=5s retry=2 async=true
                %% @scope:G1 timeout=10s cancelStrategy=flow onChildError=cancelAll
                S(Start) --> T1[Prepare]
                T1 --> G1{AND}
                G1 --> A[TaskA]
                G1 --> B[TaskB]
                G1 -->|timeout| TO[TimeoutTask]
                A --> J{AND}
                B --> J
                J --> E(End)
                TO --> E
                """;

        ProcessDefinition definition = parser.parse("annotation-flow", dsl);
        assertThat(definition.getNodes().get("T1").getMetadata())
                .containsEntry("timeout", "5s")
                .containsEntry("retry", 2)
                .containsEntry("async", true);
        assertThat(definition.getNodes().get("G1").getMetadata())
                .containsEntry("scope.timeout", "10s")
                .containsEntry("scope.cancelStrategy", "flow")
                .containsEntry("scope.onChildError", "cancelAll");

        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.deploy(definition);

        ProcessInstance instanceId = engine.start("annotation-flow", Map.of());

        assertThat(instanceId.getId()).isNotBlank();
        assertThat(engine.getInstanceStatus(instanceId.getId())).isEqualTo(InstanceStatus.COMPLETED);
        assertThat(nodeInterceptor.byInstance.get(instanceId.getId())).contains("S", "T1", "G1", "J", "E");
    }

    @Test
    void shouldExecuteTaskBySpringBeanIdMappedFromNodeId() {
        String dsl = """
                flowchart TD
                S(Start) --> prepareTask[Prepare]
                prepareTask --> G1{XOR}
                G1 -->|approved| A[Approved]
                G1 -->|default| R[Rejected]
                A --> E(End)
                R --> E
                """;

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TaskTestConfig.class)) {
            ProcessDefinition definition = parser.parse("spring-task-mapping", dsl);
            DefaultProcessEngine engine = new DefaultProcessEngine(new com.gaibu.flowlab.engine.store.impl.InMemoryProcessDefinitionStore(), context);
            RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
            engine.addNodeInterceptor(nodeInterceptor);
            engine.deploy(definition);

            ProcessInstance instanceId = engine.start("spring-task-mapping", Map.of());

            assertThat(engine.getInstanceStatus(instanceId.getId())).isEqualTo(InstanceStatus.COMPLETED);
            assertThat(nodeInterceptor.byInstance.get(instanceId.getId())).containsExactly("S", "prepareTask", "G1", "A", "E");
        }
    }

    @Test
    void shouldExecuteFlowWithComplexMermaidStyleNodeIds() {
        String dsl = """
                flowchart TD
                %% @node:task_001_prepare retry=1
                %% @node:sub_proc_node_01 subProcessId=child_complex_01
                _startNode(Start) --> task_001_prepare[Prepare]
                task_001_prepare --> gate_01{XOR}
                gate_01 -->|approved| sub_proc_node_01[[Child]]
                gate_01 -->|default| reject_01[Reject]
                sub_proc_node_01 --> _endNode(End)
                reject_01 --> _endNode
                """;
        String childDsl = """
                flowchart TD
                c_start(Start) --> child_task_01[ChildTask]
                child_task_01 --> c_end(End)
                """;

        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.registerTask("task_001_prepare", context -> context.setVariable("approved", true));
        engine.registerTask("child_task_01", context -> context.setVariable("fromChild", true));
        engine.deploy(parser.parse("child_complex_01", childDsl));
        engine.deploy(parser.parse("complex-node-id-flow", dsl));

        ProcessInstance instance = engine.start("complex-node-id-flow", Map.of());

        assertThat(engine.getInstanceStatus(instance.getId())).isEqualTo(InstanceStatus.COMPLETED);
        assertThat(instance.getVariables().snapshot()).containsEntry("fromChild", true);
        assertThat(nodeInterceptor.byInstance.get(instance.getId()))
                .contains("_startNode", "task_001_prepare", "gate_01", "sub_proc_node_01", "_endNode");
    }

    @Test
    void shouldInterruptWholeProcessWhenFlowTaskRequestsInterrupt() {
        String dsl = """
                flowchart TD
                S(Start) --> interruptTask[Interrupt]
                interruptTask --> A[AfterTask]
                A --> E(End)
                """;

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TaskTestConfig.class)) {
            ProcessDefinition definition = parser.parse("task-interrupt-flow", dsl);
            DefaultProcessEngine engine = new DefaultProcessEngine(new com.gaibu.flowlab.engine.store.impl.InMemoryProcessDefinitionStore(), context);
            RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
            engine.addNodeInterceptor(nodeInterceptor);
            engine.deploy(definition);

            ProcessInstance instanceId = engine.start("task-interrupt-flow", Map.of());

            assertThat(engine.getInstanceStatus(instanceId.getId())).isEqualTo(InstanceStatus.INTERRUPTED);
            assertThat(nodeInterceptor.byInstance.get(instanceId.getId())).containsExactly("S", "interruptTask");
        }
    }

    @Test
    void shouldReuseParentContextWhenCallingSubProcessNode() {
        String parentDsl = """
                flowchart TD
                %% @node:CallChild subProcessId=childFlow
                S(Start) --> CallChild[[AnyName]]
                CallChild --> G1{XOR}
                G1 -->|approved| A[Approved]
                G1 -->|default| R[Rejected]
                A --> E(End)
                R --> E
                """;
        String childDsl = """
                flowchart TD
                C0(Start) --> setApprovedTask[SetApproved]
                setApprovedTask --> C1(End)
                """;

        ProcessDefinition parent = parser.parse("parentFlow", parentDsl);
        ProcessDefinition child = parser.parse("childFlow", childDsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        RecordingNodeInterceptor nodeInterceptor = new RecordingNodeInterceptor();
        engine.addNodeInterceptor(nodeInterceptor);
        engine.registerTask("setApprovedTask", context -> context.setVariable("approved", true));
        engine.deploy(child);
        engine.deploy(parent);

        ProcessInstance parentInstanceId = engine.start("parentFlow", Map.of());

        assertThat(engine.getInstanceStatus(parentInstanceId.getId())).isEqualTo(InstanceStatus.COMPLETED);
        assertThat(nodeInterceptor.byInstance.get(parentInstanceId.getId())).contains("CallChild", "G1", "A", "E");
    }

    @Test
    void shouldRenderExecutionTraceAsMermaid() {
        String dsl = """
                flowchart TD
                S(Start) --> T1[Task1]
                T1 --> G1{XOR}
                G1 -->|approved| A[Approve]
                G1 -->|default| R[Reject]
                A --> E(End)
                R --> E
                """;

        ProcessDefinition definition = parser.parse("trace-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        engine.deploy(definition);

        ProcessInstance instance = engine.start("trace-flow", Map.of("approved", true));
        String mermaid = engine.renderExecutionTraceMermaid(instance.getId());

        assertThat(mermaid).contains("flowchart TD");
        assertThat(mermaid).contains("\"S\"");
        assertThat(mermaid).contains("\"T1\"");
        assertThat(mermaid).contains("\"G1\"");
        assertThat(mermaid).contains("\"A\"");
        assertThat(mermaid).contains("\"E\"");
    }

    @Test
    void shouldRetryTaskUntilSuccess() {
        String dsl = """
                flowchart TD
                %% @node:retryTask retry=2
                S(Start) --> retryTask[RetryTask]
                retryTask --> E(End)
                """;

        ProcessDefinition definition = parser.parse("retry-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        AtomicInteger attempts = new AtomicInteger();
        engine.registerTask("retryTask", ctx -> {
            int current = attempts.incrementAndGet();
            if (current < 3) {
                throw new IllegalStateException("fail at attempt " + current);
            }
            ctx.setVariable("retrySuccess", true);
        });
        engine.deploy(definition);

        ProcessInstance instance = engine.start("retry-flow", Map.of());

        assertThat(instance.getStatus()).isEqualTo(InstanceStatus.COMPLETED);
        assertThat(instance.getVariables().get("retrySuccess")).isEqualTo(true);
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldFailWhenTaskTimeoutExceeded() {
        String dsl = """
                flowchart TD
                %% @node:slowTask timeout=50ms
                S(Start) --> slowTask[SlowTask]
                slowTask --> E(End)
                """;

        ProcessDefinition definition = parser.parse("timeout-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        engine.registerTask("slowTask", ctx -> Thread.sleep(120));
        engine.deploy(definition);

        assertThatThrownBy(() -> engine.start("timeout-flow", Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FlowTask execute failed");
    }

    @Test
    void shouldExecuteTaskOnAsyncPoolWhenAsyncEnabled() {
        String dsl = """
                flowchart TD
                %% @node:asyncTask async=true
                S(Start) --> asyncTask[AsyncTask]
                asyncTask --> E(End)
                """;

        ProcessDefinition definition = parser.parse("async-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        String callerThread = Thread.currentThread().getName();
        engine.registerTask("asyncTask", ctx -> ctx.setVariable("taskThread", Thread.currentThread().getName()));
        engine.deploy(definition);

        ProcessInstance instance = engine.start("async-flow", Map.of());

        assertThat(instance.getStatus()).isEqualTo(InstanceStatus.COMPLETED);
        String taskThread = (String) instance.getVariables().get("taskThread");
        assertThat(taskThread).startsWith("flow-task-");
        assertThat(taskThread).isNotEqualTo(callerThread);
    }

    @Test
    void shouldFailWhenTaskTimeoutFormatIsNotSimpleUnit() {
        String dsl = """
                flowchart TD
                %% @node:invalidTimeoutTask timeout=PT5S
                S(Start) --> invalidTimeoutTask[Task]
                invalidTimeoutTask --> E(End)
                """;

        ProcessDefinition definition = parser.parse("invalid-timeout-format-flow", dsl);
        DefaultProcessEngine engine = new DefaultProcessEngine();
        engine.registerTask("invalidTimeoutTask", ctx -> ctx.setVariable("ok", true));
        engine.deploy(definition);

        assertThatThrownBy(() -> engine.start("invalid-timeout-format-flow", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid timeout format");
    }

    private int countOf(List<String> nodes, String nodeId) {
        return (int) nodes.stream().filter(nodeId::equals).count();
    }

    private static class RecordingNodeInterceptor implements NodeInterceptor {

        private final Map<String, List<String>> byInstance = new LinkedHashMap<>();

        @Override
        public void before(ExecutionContext ctx) {
            byInstance.computeIfAbsent(ctx.instance().getId(), key -> new ArrayList<>())
                    .add(ctx.node().getId().value());
        }

        @Override
        public void afterSuccess(ExecutionContext ctx, Instruction instruction) {
        }

        @Override
        public void afterFailure(ExecutionContext ctx, Throwable ex) {
        }
    }

    private static class RecordingProcessInterceptor implements ProcessInterceptor {

        private final AtomicInteger beforeStartCount = new AtomicInteger();
        private final AtomicInteger completedCount = new AtomicInteger();
        private final AtomicInteger failedCount = new AtomicInteger();

        @Override
        public void beforeStart(ProcessInstance instance) {
            beforeStartCount.incrementAndGet();
        }

        @Override
        public void onCompleted(ProcessInstance instance) {
            completedCount.incrementAndGet();
        }

        @Override
        public void onFailed(ProcessInstance instance, Throwable ex) {
            failedCount.incrementAndGet();
        }
    }

    private static class ThrowingNodeInterceptor implements NodeInterceptor {

        private final String targetNodeId;

        private ThrowingNodeInterceptor(String targetNodeId) {
            this.targetNodeId = targetNodeId;
        }

        @Override
        public void before(ExecutionContext ctx) {
            if (targetNodeId.equals(ctx.node().getId().value())) {
                throw new IllegalStateException("boom at node " + targetNodeId);
            }
        }

        @Override
        public void afterSuccess(ExecutionContext ctx, Instruction instruction) {
        }

        @Override
        public void afterFailure(ExecutionContext ctx, Throwable ex) {
        }
    }

    private static class OrderedNodeInterceptor implements NodeInterceptor {

        private final String name;
        private final List<String> events;

        private OrderedNodeInterceptor(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public void before(ExecutionContext ctx) {
            events.add(name + "-before-" + ctx.node().getId().value());
        }

        @Override
        public void afterSuccess(ExecutionContext ctx, Instruction instruction) {
            events.add(name + "-success-" + ctx.node().getId().value());
        }

        @Override
        public void afterFailure(ExecutionContext ctx, Throwable ex) {
            events.add(name + "-failure-" + ctx.node().getId().value());
        }
    }

    @Configuration
    static class TaskTestConfig {

        @Bean("prepareTask")
        FlowTask prepareTask() {
            return new PrepareTask();
        }

        @Bean("interruptTask")
        FlowTask interruptTask() {
            return new InterruptTask();
        }
    }

    private static class PrepareTask implements FlowTask {

        @Override
        public void execute(TaskContext context) {
            context.setVariable("approved", true);
        }
    }

    private static class InterruptTask implements FlowTask {

        @Override
        public void execute(TaskContext context) {
            context.interruptProcess("risk-control-blocked");
        }
    }
}
