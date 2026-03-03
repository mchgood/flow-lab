package com.gaibu.flowlab.engine.api.impl;

import com.gaibu.flowlab.engine.api.ProcessEngine;
import com.gaibu.flowlab.engine.behavior.NodeBehaviorFactory;
import com.gaibu.flowlab.engine.execution.ExecutionLoop;
import com.gaibu.flowlab.engine.execution.InstructionHandler;
import com.gaibu.flowlab.engine.execution.TokenFactory;
import com.gaibu.flowlab.engine.expression.impl.SpelExpressionEngine;
import com.gaibu.flowlab.engine.graph.ExecutableGraph;
import com.gaibu.flowlab.engine.graph.GraphCompiler;
import com.gaibu.flowlab.engine.interceptor.NodeInterceptor;
import com.gaibu.flowlab.engine.interceptor.ProcessInterceptor;
import com.gaibu.flowlab.engine.interceptor.ProcessInterceptorChain;
import com.gaibu.flowlab.engine.runtime.Execution;
import com.gaibu.flowlab.engine.runtime.ExecutionId;
import com.gaibu.flowlab.engine.runtime.ProcessInstance;
import com.gaibu.flowlab.engine.runtime.ScopeId;
import com.gaibu.flowlab.engine.runtime.Token;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.runtime.enums.TokenStatus;
import com.gaibu.flowlab.engine.scheduler.InMemoryScheduler;
import com.gaibu.flowlab.engine.store.ProcessDefinitionStore;
import com.gaibu.flowlab.engine.store.impl.InMemoryProcessDefinitionStore;
import com.gaibu.flowlab.engine.store.impl.InMemoryVariableStore;
import com.gaibu.flowlab.engine.task.FlowTask;
import com.gaibu.flowlab.engine.task.TaskRegistry;
import com.gaibu.flowlab.engine.task.impl.InMemoryTaskRegistry;
import com.gaibu.flowlab.engine.task.impl.SpringBeanTaskRegistry;
import com.gaibu.flowlab.engine.trace.ExecutionTraceMermaidRenderer;
import com.gaibu.flowlab.engine.trace.ExecutionTraceStore;
import com.gaibu.flowlab.engine.trace.InMemoryExecutionTraceStore;
import com.gaibu.flowlab.engine.trace.TraceNodeInterceptor;
import com.gaibu.flowlab.parser.model.entity.ProcessDefinition;
import com.gaibu.flowlab.engine.store.VariableStore;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProcessEngine 默认实现。
 */
public class DefaultProcessEngine implements ProcessEngine {

    /**
     * 流程定义存储。
     */
    private final ProcessDefinitionStore definitionStore;

    /**
     * 编译后图缓存。
     */
    private final Map<String, ExecutableGraph> graphByProcessId = new LinkedHashMap<>();

    /**
     * 实例运行态缓存。
     */
    private final Map<String, ProcessInstance> instances = new LinkedHashMap<>();

    /**
     * 实例所属流程索引。
     */
    private final Map<String, String> processIdByInstance = new LinkedHashMap<>();

    /**
     * 节点拦截器列表。
     */
    private final List<NodeInterceptor> nodeInterceptors = new ArrayList<>();

    /**
     * 流程拦截器列表。
     */
    private final List<ProcessInterceptor> processInterceptors = new ArrayList<>();

    /**
     * ID 生成器。
     */
    private final AtomicLong idSeq = new AtomicLong(1L);

    /**
     * 图编译器。
     */
    private final GraphCompiler graphCompiler;

    /**
     * 任务注册表。
     */
    private final InMemoryTaskRegistry inMemoryTaskRegistry;

    /**
     * 执行循环。
     */
    private final ExecutionLoop executionLoop;

    /**
     * 流程拦截器分发器。
     */
    private final ProcessInterceptorChain processInterceptorChain;

    /**
     * 执行轨迹存储。
     */
    private final ExecutionTraceStore traceStore;

    /**
     * 执行轨迹渲染器。
     */
    private final ExecutionTraceMermaidRenderer traceRenderer;

    public DefaultProcessEngine() {
        this(new InMemoryProcessDefinitionStore(), new InMemoryTaskRegistry());
    }

    public DefaultProcessEngine(ProcessDefinitionStore definitionStore) {
        this(definitionStore, new InMemoryTaskRegistry());
    }

    public DefaultProcessEngine(ProcessDefinitionStore definitionStore, InMemoryTaskRegistry taskRegistry) {
        this.definitionStore = definitionStore;
        this.inMemoryTaskRegistry = taskRegistry;
        this.traceStore = new InMemoryExecutionTraceStore();
        this.traceRenderer = new ExecutionTraceMermaidRenderer(traceStore);
        this.graphCompiler = new GraphCompiler(new NodeBehaviorFactory(new SpelExpressionEngine(), taskRegistry, this::launchSubProcess));
        TokenFactory tokenFactory = new TokenFactory(idSeq);
        this.executionLoop = new ExecutionLoop(new InstructionHandler(tokenFactory));
        this.processInterceptorChain = new ProcessInterceptorChain();
        this.nodeInterceptors.add(new TraceNodeInterceptor(traceStore));
    }

    public DefaultProcessEngine(ProcessDefinitionStore definitionStore, ApplicationContext applicationContext) {
        this.definitionStore = definitionStore;
        this.inMemoryTaskRegistry = null;
        this.traceStore = new InMemoryExecutionTraceStore();
        this.traceRenderer = new ExecutionTraceMermaidRenderer(traceStore);
        TaskRegistry springRegistry = new SpringBeanTaskRegistry(applicationContext);
        this.graphCompiler = new GraphCompiler(new NodeBehaviorFactory(new SpelExpressionEngine(), springRegistry, this::launchSubProcess));
        TokenFactory tokenFactory = new TokenFactory(idSeq);
        this.executionLoop = new ExecutionLoop(new InstructionHandler(tokenFactory));
        this.processInterceptorChain = new ProcessInterceptorChain();
        this.nodeInterceptors.add(new TraceNodeInterceptor(traceStore));
    }

    /**
     * 部署流程定义并编译为可执行图。
     *
     * @param definition 流程定义
     */
    public void deploy(ProcessDefinition definition) {
        definitionStore.put(definition);
        graphByProcessId.put(definition.getId(), graphCompiler.compile(definition));
    }

    /**
     * 注册内存任务实现，用于非 Spring 场景或测试。
     *
     * @param nodeId 节点 ID
     * @param task 任务实现
     */
    public void registerTask(String nodeId, FlowTask task) {
        if (inMemoryTaskRegistry == null) {
            throw new IllegalStateException("Current engine is not using InMemoryTaskRegistry.");
        }
        inMemoryTaskRegistry.register(nodeId, task);
    }

    /**
     * 注册节点拦截器。
     *
     * @param interceptor 拦截器
     */
    public void addNodeInterceptor(NodeInterceptor interceptor) {
        nodeInterceptors.add(interceptor);
    }

    /**
     * 注册流程拦截器。
     *
     * @param interceptor 拦截器
     */
    public void addProcessInterceptor(ProcessInterceptor interceptor) {
        processInterceptors.add(interceptor);
    }

    @Override
    public ProcessInstance start(String processId, Map<String, Object> variables) {
        InMemoryVariableStore store = new InMemoryVariableStore();
        if (variables != null) {
            variables.forEach(store::put);
        }
        return startInternal(processId, store);
    }

    /**
     * 查询实例状态。
     *
     * @param instanceId 实例 ID
     * @return 实例状态
     */
    public InstanceStatus getInstanceStatus(String instanceId) {
        return requireInstance(instanceId).getStatus();
    }

    /**
     * 渲染实例执行链路图（Mermaid）。
     *
     * @param instanceId 实例 ID
     * @return Mermaid 文本
     */
    public String renderExecutionTraceMermaid(String instanceId) {
        requireInstance(instanceId);
        return traceRenderer.render(instanceId);
    }

    private ProcessInstance requireInstance(String instanceId) {
        ProcessInstance instance = instances.get(instanceId);
        if (instance == null) {
            throw new IllegalArgumentException("Process instance not found: " + instanceId);
        }
        return instance;
    }

    private ExecutableGraph requireGraph(String processId) {
        ExecutableGraph graph = graphByProcessId.get(processId);
        if (graph == null) {
            ProcessDefinition definition = definitionStore.get(processId);
            if (definition != null) {
                graph = graphCompiler.compile(definition);
                graphByProcessId.put(processId, graph);
            }
        }
        if (graph == null) {
            throw new IllegalArgumentException("Process definition not found: " + processId);
        }
        return graph;
    }

    private ProcessInstance startInternal(String processId, VariableStore variableStore) {
        ExecutableGraph graph = requireGraph(processId);

        ProcessInstance instance = new ProcessInstance();
        instance.setId("PI-" + idSeq.incrementAndGet());
        instance.setStatus(InstanceStatus.RUNNING);
        instance.setVariables(variableStore);

        Execution rootExecution = new Execution();
        rootExecution.setId(new ExecutionId("EX-" + idSeq.incrementAndGet()));
        rootExecution.setScopeId(new ScopeId("ROOT"));
        instance.setRootExecution(rootExecution);

        Token rootToken = new TokenFactory(idSeq).create(graph.startNodeId(), rootExecution);
        instance.addToken(rootToken);

        processInterceptorChain.beforeStart(processInterceptors, instance);

        InMemoryScheduler scheduler = new InMemoryScheduler();
        scheduler.schedule(rootToken);

        instances.put(instance.getId(), instance);
        processIdByInstance.put(instance.getId(), processId);

        executeProcess(instance, graph, scheduler);
        return instance;
    }

    private void executeProcess(ProcessInstance instance, ExecutableGraph graph, InMemoryScheduler scheduler) {
        try {
            executionLoop.run(instance, graph, scheduler, nodeInterceptors);
            refreshInstanceStatus(instance);
        } catch (Exception ex) {
            instance.setStatus(InstanceStatus.FAILED);
            instance.setFailureCause(ex);
            if (instance.getVariables() != null) {
                instance.getVariables().put("process.error", ex);
                instance.getVariables().put("process.error.message", ex.getMessage());
                instance.getVariables().put("process.error.type", ex.getClass().getName());
            }
        }

        if (instance.getStatus() == InstanceStatus.FAILED) {
            Throwable ex = instance.getFailureCause();
            if (ex == null) {
                ex = new IllegalStateException("Process finished with FAILED status, instanceId=" + instance.getId());
            }
            processInterceptorChain.onFailed(processInterceptors, instance, ex);
            return;
        }
        processInterceptorChain.onCompleted(processInterceptors, instance);
    }

    private void refreshInstanceStatus(ProcessInstance instance) {
        if (instance.getStatus() == InstanceStatus.INTERRUPTED) {
            return;
        }
        boolean hasFailed = instance.getTokensById().values().stream().anyMatch(token -> token.getStatus() == TokenStatus.FAILED);
        if (hasFailed || instance.getStatus() == InstanceStatus.FAILED) {
            instance.setStatus(InstanceStatus.FAILED);
            return;
        }
        if (!instance.getActiveTokens().isEmpty()) {
            instance.setStatus(InstanceStatus.RUNNING);
            return;
        }
        instance.setStatus(InstanceStatus.COMPLETED);
    }

    private void launchSubProcess(String subProcessId, VariableStore sharedVariables) {
        ProcessInstance childInstance = startInternal(subProcessId, sharedVariables);
        InstanceStatus status = childInstance.getStatus();
        if (status != InstanceStatus.COMPLETED) {
            throw new IllegalStateException("Sub process did not complete successfully, processId="
                    + subProcessId + ", status=" + status);
        }
    }
}
