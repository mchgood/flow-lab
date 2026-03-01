package com.gaibu.flowlab.engine.impl;

import com.gaibu.flowlab.engine.api.ExecutionPlanBuilder;
import com.gaibu.flowlab.engine.api.ExpressionEngine;
import com.gaibu.flowlab.engine.api.ExpressionEngineProvider;
import com.gaibu.flowlab.engine.api.WorkflowExecutor;
import com.gaibu.flowlab.engine.api.WorkerExecutor;
import com.gaibu.flowlab.engine.api.interceptor.NodeInterceptor;
import com.gaibu.flowlab.engine.api.interceptor.WorkflowInterceptor;
import com.gaibu.flowlab.engine.exception.ExpressionEvaluationException;
import com.gaibu.flowlab.engine.exception.NoRouteMatchedException;
import com.gaibu.flowlab.engine.exception.WorkflowExecutionException;
import com.gaibu.flowlab.engine.model.ExecutionContext;
import com.gaibu.flowlab.engine.model.ExecutionPlan;
import com.gaibu.flowlab.engine.model.ExecutionRuntime;
import com.gaibu.flowlab.engine.model.enums.ExecutionState;
import com.gaibu.flowlab.engine.model.enums.FailureStrategy;
import com.gaibu.flowlab.engine.model.FlowContext;
import com.gaibu.flowlab.engine.model.NodeExecutionContext;
import com.gaibu.flowlab.engine.model.NodeResult;
import com.gaibu.flowlab.engine.model.enums.ParallelMode;
import com.gaibu.flowlab.engine.model.WorkflowExecutionOptions;
import com.gaibu.flowlab.engine.model.WorkflowExecutionResult;
import com.gaibu.flowlab.parser.api.model.Edge;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.GraphMeta;
import com.gaibu.flowlab.parser.api.model.GroupMeta;
import com.gaibu.flowlab.parser.api.model.NodeMeta;
import com.gaibu.flowlab.parser.api.model.SubflowMeta;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 默认 Workflow 执行器。
 *
 * <p>实现特点：
 * <p>1. 调度线程单线程推进，所有状态更新都由同一线程完成，保证结果可复现。
 * <p>2. 执行路径由边决策驱动：顺序边全选，条件边按 First-Match-Wins。
 * <p>3. 子流程通过递归执行复用同一套调度逻辑，输入输出由 mapping 显式映射。
 * <p>4. 拦截器作为横切能力集成在执行链路，不改变核心状态机语义。
 */
public class DefaultWorkflowExecutor implements WorkflowExecutor {

    private final Map<String, WorkflowDefinition> definitions;
    private final WorkerExecutor workerExecutor;
    private final WorkflowExecutionOptions options;
    private final List<WorkflowInterceptor> workflowInterceptors;
    private final List<NodeInterceptor> nodeInterceptors;
    private final ExecutionPlanBuilder planBuilder;
    private final ExpressionEngine expressionEngine;

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor() {
        this(List.of(), new DefaultWorkerExecutor(), WorkflowExecutionOptions.defaults(), List.of(), List.of());
    }

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor(List<WorkflowDefinition> definitions) {
        this(definitions, new DefaultWorkerExecutor(), WorkflowExecutionOptions.defaults(), List.of(), List.of());
    }

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor(List<WorkflowDefinition> definitions, WorkerExecutor workerExecutor) {
        this(definitions, workerExecutor, WorkflowExecutionOptions.defaults(), List.of(), List.of());
    }

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor(List<WorkflowDefinition> definitions,
                                   WorkerExecutor workerExecutor,
                                   WorkflowExecutionOptions options) {
        this(definitions, workerExecutor, options, List.of(), List.of());
    }

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor(List<WorkflowDefinition> definitions,
                                   WorkerExecutor workerExecutor,
                                   WorkflowExecutionOptions options,
                                   List<WorkflowInterceptor> workflowInterceptors,
                                   List<NodeInterceptor> nodeInterceptors) {
        this(definitions,
                workerExecutor,
                options,
                workflowInterceptors,
                nodeInterceptors,
                new DefaultExecutionPlanBuilder(),
                new DefaultExpressionEngineProvider());
    }

    /**
     * 构造DefaultWorkflowExecutor实例。
     */
    public DefaultWorkflowExecutor(List<WorkflowDefinition> definitions,
                                   WorkerExecutor workerExecutor,
                                   WorkflowExecutionOptions options,
                                   List<WorkflowInterceptor> workflowInterceptors,
                                   List<NodeInterceptor> nodeInterceptors,
                                   ExecutionPlanBuilder planBuilder,
                                   ExpressionEngineProvider expressionEngineProvider) {
        this.definitions = safeDefinitions(definitions);
        this.workerExecutor = workerExecutor == null ? new DefaultWorkerExecutor() : workerExecutor;
        this.options = options == null ? WorkflowExecutionOptions.defaults() : options;
        this.workflowInterceptors = safeWorkflowInterceptors(workflowInterceptors);
        this.nodeInterceptors = safeNodeInterceptors(nodeInterceptors);
        this.planBuilder = planBuilder;
        this.expressionEngine = expressionEngineProvider.get();
    }

    @Override
    /**
     * 执行execute并返回结果。
     * @return 执行结果
     */
    public WorkflowExecutionResult execute(String workflowId, Map<String, Object> variables) {
        return executeInternal(workflowId, variables, 0);
    }

    /**
     * 将定义列表整理为按 workflowId 检索的表。
     *
     * <p>这里会过滤空定义与空 id，避免执行期出现 NPE。
     */
    private Map<String, WorkflowDefinition> safeDefinitions(List<WorkflowDefinition> definitions) {
        Map<String, WorkflowDefinition> map = new LinkedHashMap<>();
        if (definitions == null) {
            return map;
        }
        for (WorkflowDefinition definition : definitions) {
            if (definition == null || definition.getId() == null || definition.getId().isBlank()) {
                continue;
            }
            map.put(definition.getId(), definition);
        }
        return map;
    }

    /**
     * 执行safeWorkflowInterceptors并返回结果。
     * @return 执行结果
     */
    private List<WorkflowInterceptor> safeWorkflowInterceptors(List<WorkflowInterceptor> interceptors) {
        if (interceptors == null || interceptors.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(interceptors);
    }

    /**
     * 执行safeNodeInterceptors并返回结果。
     * @return 执行结果
     */
    private List<NodeInterceptor> safeNodeInterceptors(List<NodeInterceptor> interceptors) {
        if (interceptors == null || interceptors.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(interceptors);
    }

    /**
     * 执行executeInternal并返回结果。
     * @return 执行结果
     */
    private WorkflowExecutionResult executeInternal(String workflowId,
                                                    Map<String, Object> variables,
                                                    int depth) {
        // 子流程以递归方式执行；这里做一个深度护栏，避免异常数据导致无限递归。
        if (depth > definitions.size() + 2) {
            throw new WorkflowExecutionException("子流程嵌套深度异常，疑似循环引用: " + workflowId);
        }
        WorkflowDefinition definition = definitions.get(workflowId);
        if (definition == null) {
            throw new WorkflowExecutionException("workflow 不存在: " + workflowId);
        }
        Session session = new Session(definition, variables, depth);
        return session.run();
    }

    private final class Session {

        private final WorkflowDefinition definition;
        private final int depth;
        private final Graph graph;
        private final GraphMeta meta;
        private final ExecutionRuntime runtime;
        private final FlowContext flowContext;
        /**
         * 每个节点的出边（保持源码声明顺序，供条件分支按顺序求值）。
         */
        private final Map<String, List<Edge>> outgoingByNode = new LinkedHashMap<>();
        /**
         * 每个节点的全部父节点集合（结构层原始关系）。
         */
        private final Map<String, Set<String>> incomingParents = new LinkedHashMap<>();
        /**
         * 调度中尚未消费的父节点集合。
         *
         * <p>当某个父节点执行完成后，会从对应子节点的 pendingParents 中移除。
         * 当集合清空时，说明该子节点所有父分支都已“给出决策”。
         */
        private final Map<String, Set<String>> pendingParents = new LinkedHashMap<>();
        /**
         * 记录每个节点被“命中路径”的父边次数。
         *
         * <p>用于区分两类场景：
         * <p>1. 所有父边都处理完且至少一条命中 -> 节点可执行
         * <p>2. 所有父边都处理完但零命中 -> 节点应取消（不可达）
         */
        private final Map<String, Integer> selectedParentCount = new HashMap<>();
        private final Set<String> roots = new LinkedHashSet<>();
        private final Set<String> resolved = new HashSet<>();
        /**
         * 待执行节点队列（调度线程消费）。
         */
        private final ArrayDeque<String> readyQueue = new ArrayDeque<>();
        private final Map<String, String> nodeToGroup = new HashMap<>();
        private final Map<String, GroupRuntime> groupById = new LinkedHashMap<>();
        private final long startedAtMs = System.currentTimeMillis();

        private ExecutionState workflowState;
        private String workflowMessage;
        private Throwable workflowError;

        /**
         * 执行Session并返回结果。
         * @return 执行结果
         */
        private Session(WorkflowDefinition definition,
                        Map<String, Object> variables,
                        int depth) {
            this.definition = definition;
            this.depth = depth;
            this.graph = definition.getGraph();
            this.meta = definition.getMeta();
            ExecutionPlan plan = planBuilder.build(definition);
            this.runtime = new ExecutionRuntime(plan, new ExecutionContext(copyVariables(variables)));
            this.flowContext = new FlowContext(definition.getId(), this.runtime.getContext().getVariables());
        }

        /**
         * 执行会话主循环。
         *
         * <p>阶段说明：
         * <p>1. 触发流程前置拦截
         * <p>2. 构建图索引与初始 ready 节点
         * <p>3. 单线程执行 ready 队列直到终止
         * <p>4. 收敛剩余节点状态并产出最终结果
         */
        private WorkflowExecutionResult run() {
            Map<String, Object> initialVariables = copyVariables(runtime.getContext().getVariables());
            try {
                // 先触发流程前置拦截，再进入调度循环。
                invokeWorkflowBefore(definition.getId(), runtime.getContext().getVariables());
                initStructures();
                // 单线程调度循环：每次只取一个 ready 节点执行，保证状态更新确定性。
                while (!readyQueue.isEmpty() && workflowState == null) {
                    if (isGlobalTimeoutExceeded()) {
                        terminateWorkflow(ExecutionState.TIMEOUT, "workflow 全局超时", null);
                        break;
                    }
                    String nodeId = readyQueue.removeFirst();
                    if (resolved.contains(nodeId)) {
                        continue;
                    }

                    GroupRuntime group = getGroup(nodeId);
                    if (group != null && group.completed && !group.memberStates.containsKey(nodeId)) {
                        // ANY 模式可能提前结束；后续成员直接标记取消，不再真正执行。
                        resolveCancelled(nodeId, "并行组已提前完成");
                        continue;
                    }

                    AttemptOutcome outcome = executeWithRetry(nodeId, group);
                    applyOutcome(nodeId, outcome, group);
                }
            } catch (RuntimeException ex) {
                terminateWorkflow(ExecutionState.FAILED, ex.getMessage(), ex);
            }

            if (workflowState != null) {
                // 流程已经终止时，剩余节点统一取消，保证状态闭合。
                cancelUnresolvedNodes();
            } else {
                // 正常跑完队列后，未命中路径的节点也要落到终态（CANCELLED）。
                resolveLeftNodesAsCancelled();
                workflowState = deriveFinalWorkflowState();
            }

            WorkflowExecutionResult result = new WorkflowExecutionResult();
            result.setWorkflowId(definition.getId());
            result.setState(workflowState);
            result.setMessage(workflowMessage);
            result.setError(workflowError);
            result.setVariables(copyVariables(runtime.getContext().getVariables()));
            result.setUnitStates(extractNodeStates());
            emitWorkflowInterceptors(result, initialVariables);
            return result;
        }

        /**
         * 初始化调度索引与状态。
         *
         * <p>不变量：
         * <p>- 每个节点初始为 PENDING
         * <p>- pendingParents 初始等于 incomingParents
         * <p>- 入度为 0 的节点立即入队
         */
        private void initStructures() {
            // 初始化运行时状态与图索引（出边、入边、入度）。
            for (String nodeId : graph.getNodes().keySet()) {
                outgoingByNode.put(nodeId, new ArrayList<>());
                incomingParents.put(nodeId, new LinkedHashSet<>());
                selectedParentCount.put(nodeId, 0);
                runtime.updateState(nodeId, ExecutionState.PENDING);
            }
            for (Edge edge : graph.getEdges()) {
                outgoingByNode.get(edge.getFrom()).add(edge);
                incomingParents.get(edge.getTo()).add(edge.getFrom());
            }
            for (Map.Entry<String, Set<String>> entry : incomingParents.entrySet()) {
                String nodeId = entry.getKey();
                Set<String> parents = new LinkedHashSet<>(entry.getValue());
                pendingParents.put(nodeId, parents);
                if (parents.isEmpty()) {
                    // 入度为 0 的节点作为启动节点进入 ready 队列。
                    roots.add(nodeId);
                    readyQueue.add(nodeId);
                }
            }
            for (NodeMeta nodeMeta : meta.getNodeMeta().values()) {
                if (nodeMeta.getGroupId() != null && !nodeMeta.getGroupId().isBlank()) {
                    nodeToGroup.put(nodeMeta.getNodeId(), nodeMeta.getGroupId());
                }
            }
            for (GroupMeta groupMeta : meta.getGroupMeta().values()) {
                ParallelMode mode = ParallelMode.from(groupMeta.getAttributes().get("mode"));
                GroupRuntime groupRuntime = new GroupRuntime(groupMeta.getGroupId(), mode, groupMeta.getNodeIds());
                groupById.put(groupMeta.getGroupId(), groupRuntime);
            }
        }

        /**
         * 在 retry 策略下执行节点。
         *
         * <p>仅 FAILED 会触发重试；TIMEOUT/CANCELLED 直接返回。
         */
        private AttemptOutcome executeWithRetry(String nodeId, GroupRuntime group) {
            int maxRetry = resolveRetry(nodeId);
            AttemptOutcome last = null;
            // retry 只在 FAILED 场景继续；TIMEOUT/CANCELLED 不做重试。
            for (int attempt = 0; attempt <= maxRetry; attempt++) {
                last = executeAttempt(nodeId, attempt, group);
                if (last.state != ExecutionState.FAILED || attempt == maxRetry) {
                    return last;
                }
            }
            return last;
        }

        /**
         * 执行单次 attempt（不含重试循环）。
         *
         * <p>这个方法只关注“节点单次执行结果”，不直接做全局失败传播。
         */
        private AttemptOutcome executeAttempt(String nodeId, int attempt, GroupRuntime group) {
            runtime.updateState(nodeId, ExecutionState.RUNNING);
            NodeExecutionContext context = new NodeExecutionContext(
                    flowContext,
                    nodeId,
                    graph.getNode(nodeId),
                    attempt
            );
            invokeNodeBefore(context);
            long timeoutMs = resolveUnitTimeout(nodeId, group);
            long begin = System.currentTimeMillis();
            NodeResult result;
            try {
                // Worker / 子流程执行异常统一转为 FAILED 结果，交由同一条状态链处理。
                result = executeUnit(context);
            } catch (RuntimeException ex) {
                result = NodeResult.failed(ex.getMessage(), ex);
            }
            long elapsed = System.currentTimeMillis() - begin;
            if (timeoutMs > 0 && elapsed > timeoutMs) {
                NodeResult timeoutResult = NodeResult.timeout("节点超时: " + nodeId);
                invokeNodeFailure(context, timeoutResult);
                return AttemptOutcome.timeout(timeoutResult.getMessage());
            }
            ExecutionState state = result.getState() == null ? ExecutionState.SUCCESS : result.getState();
            if (state == ExecutionState.SUCCESS) {
                try {
                    Set<String> selected = resolveSuccessors(nodeId);
                    invokeNodeSuccess(context, result);
                    return AttemptOutcome.success(result.getOutputs(), selected);
                } catch (NoRouteMatchedException | ExpressionEvaluationException | WorkflowExecutionException ex) {
                    // 路由/表达式异常视为业务失败，进入失败拦截与失败传播。
                    NodeResult failedResult = NodeResult.failed(ex.getMessage(), ex);
                    invokeNodeFailure(context, failedResult);
                    return AttemptOutcome.failed(ex.getMessage(), ex);
                }
            }
            if (state == ExecutionState.TIMEOUT) {
                invokeNodeFailure(context, result);
                return AttemptOutcome.timeout(result.getMessage());
            }
            if (state == ExecutionState.CANCELLED) {
                invokeNodeFailure(context, result);
                return AttemptOutcome.cancelled(result.getMessage());
            }
            invokeNodeFailure(context, result);
            return AttemptOutcome.failed(result.getMessage(), result.getError());
        }

        /**
         * 执行单元分派：
         * <p>- 若节点配置了 subflow，走子流程执行
         * <p>- 否则交给 WorkerExecutor 执行
         */
        private NodeResult executeUnit(NodeExecutionContext context) {
            String nodeId = context.getNodeId();
            SubflowMeta subflowMeta = meta.getSubflows().get(nodeId);
            if (subflowMeta != null) {
                return executeSubflow(nodeId, subflowMeta);
            }
            NodeResult result = workerExecutor.execute(context);
            return result == null ? NodeResult.success(Map.of()) : result;
        }

        /**
         * 执行executeSubflow并返回结果。
         * @return 执行结果
         */
        private NodeResult executeSubflow(String nodeId, SubflowMeta subflowMeta) {
            // 子流程变量隔离：只通过 in/out mapping 做显式传值。
            Map<String, Object> subflowInput = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : subflowMeta.getInputMapping().entrySet()) {
                subflowInput.put(entry.getKey(), runtime.getContext().getVariables().get(entry.getValue()));
            }
            WorkflowExecutionResult subflowResult = executeInternal(
                    subflowMeta.getReferenceId(),
                    subflowInput,
                    depth + 1
            );
            if (subflowResult.getState() == ExecutionState.SUCCESS) {
                Map<String, Object> outputs = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : subflowMeta.getOutputMapping().entrySet()) {
                    outputs.put(entry.getKey(), subflowResult.getVariables().get(entry.getValue()));
                }
                return NodeResult.success(outputs);
            }
            if (subflowResult.getState() == ExecutionState.TIMEOUT) {
                return NodeResult.timeout("子流程超时: " + subflowMeta.getReferenceId());
            }
            if (subflowResult.getState() == ExecutionState.CANCELLED) {
                return NodeResult.cancelled("子流程取消: " + subflowMeta.getReferenceId());
            }
            return NodeResult.failed("子流程失败: " + subflowMeta.getReferenceId(), subflowResult.getError());
        }

        /**
         * 将单次执行结果落地到运行时并传播到后继节点。
         *
         * <p>职责边界：
         * <p>- 状态落地：resolved/runtime/group member
         * <p>- 路径传播：成功按 selectedSuccessors，失败按空集合
         * <p>- 失败策略：仅在这里决定是否终止 workflow
         */
        private void applyOutcome(String nodeId, AttemptOutcome outcome, GroupRuntime group) {
            resolved.add(nodeId);
            runtime.updateState(nodeId, outcome.state);
            if (group != null) {
                group.memberStates.put(nodeId, outcome.state);
            }

            if (outcome.state == ExecutionState.SUCCESS) {
                // 只有成功节点才合并输出变量并按路由传播。
                mergeOutputs(outcome.outputs);
                propagate(nodeId, outcome.selectedSuccessors);
            } else {
                // 失败/取消/超时节点不会选择后继，统一传播为“未选中”。
                propagate(nodeId, Set.of());
            }

            if (group != null) {
                handleGroupPost(nodeId, outcome, group);
                return;
            }

            if (outcome.state == ExecutionState.TIMEOUT && isFailFast()) {
                terminateWorkflow(ExecutionState.TIMEOUT, outcome.message, outcome.error);
                return;
            }
            if (outcome.state == ExecutionState.FAILED && isFailFast()) {
                terminateWorkflow(ExecutionState.FAILED, outcome.message, outcome.error);
            }
        }

        /**
         * 处理并行组聚合状态。
         *
         * <p>ANY:
         * <p>- 首个 SUCCESS 立即胜出并取消其余成员
         * <p>- 全部结束仍无 SUCCESS 则组失败
         *
         * <p>ALL:
         * <p>- 任一非 SUCCESS 即组失败
         * <p>- 全部 SUCCESS 才组成功
         */
        private void handleGroupPost(String nodeId, AttemptOutcome outcome, GroupRuntime group) {
            if (group.mode == ParallelMode.ANY) {
                if (outcome.state == ExecutionState.SUCCESS && !group.completed) {
                    // ANY 命中后立即结束分组，并取消未执行成员。
                    group.completed = true;
                    group.success = true;
                    group.winner = nodeId;
                    for (String memberId : group.members) {
                        if (!memberId.equals(nodeId) && !resolved.contains(memberId)) {
                            resolveCancelled(memberId, "ANY 并行组命中赢家: " + nodeId);
                        }
                    }
                    return;
                }
                if (!group.completed && group.allMembersResolved(resolved) && !group.hasSuccess()) {
                    // ANY 全部结束且无成功节点，按失败处理。
                    group.completed = true;
                    group.success = false;
                    if (isFailFast()) {
                        ExecutionState state = group.anyTimeout() ? ExecutionState.TIMEOUT : ExecutionState.FAILED;
                        terminateWorkflow(state, "ANY 并行组无成功节点: " + group.groupId, outcome.error);
                    }
                }
                return;
            }

            if (outcome.state != ExecutionState.SUCCESS) {
                // ALL 任意失败即可判定组失败。
                group.completed = true;
                group.success = false;
                if (isFailFast()) {
                    ExecutionState state = outcome.state == ExecutionState.TIMEOUT ? ExecutionState.TIMEOUT : ExecutionState.FAILED;
                    terminateWorkflow(state, outcome.message, outcome.error);
                }
                return;
            }
            if (group.allMembersSuccess()) {
                group.completed = true;
                group.success = true;
            }
        }

        /**
         * 执行resolveCancelled。
         */
        private void resolveCancelled(String nodeId, String message) {
            if (resolved.contains(nodeId)) {
                return;
            }
            resolved.add(nodeId);
            runtime.updateState(nodeId, ExecutionState.CANCELLED);
            GroupRuntime group = getGroup(nodeId);
            if (group != null) {
                group.memberStates.put(nodeId, ExecutionState.CANCELLED);
            }
            propagate(nodeId, Set.of());
            if (message != null && workflowMessage == null) {
                workflowMessage = message;
            }
        }

        /**
         * 执行propagate。
         */
        private void propagate(String fromNodeId, Set<String> selectedSuccessors) {
            // 对每条出边都消费一次“是否选中”的决策，保证 join 节点能正确归并。
            List<Edge> edges = outgoingByNode.getOrDefault(fromNodeId, List.of());
            for (Edge edge : edges) {
                boolean selected = selectedSuccessors.contains(edge.getTo());
                consumeDecision(edge.getFrom(), edge.getTo(), selected);
            }
        }

        /**
         * 消费一条父边决策。
         *
         * <p>每条边只消费一次：从 pendingParents 移除 parentId。
         * selected=true 时累加 selectedParentCount，用于最终可达性判断。
         */
        private void consumeDecision(String parentId, String nodeId, boolean selected) {
            if (resolved.contains(nodeId)) {
                return;
            }
            Set<String> pending = pendingParents.get(nodeId);
            if (pending.remove(parentId)) {
                if (selected) {
                    selectedParentCount.put(nodeId, selectedParentCount.get(nodeId) + 1);
                }
                checkReadyOrSkip(nodeId);
            }
        }

        /**
         * 执行checkReadyOrSkip。
         */
        private void checkReadyOrSkip(String nodeId) {
            if (resolved.contains(nodeId)) {
                return;
            }
            if (!pendingParents.get(nodeId).isEmpty()) {
                return;
            }
            if (roots.contains(nodeId) || selectedParentCount.get(nodeId) > 0) {
                // 所有父边都已处理，且至少有一条命中路径，进入可执行队列。
                readyQueue.add(nodeId);
            } else {
                // 没有任何父边命中，说明该节点不在本次执行路径上。
                resolveCancelled(nodeId, "未命中执行路径");
            }
        }

        /**
         * 计算当前节点应该激活的后继节点集合。
         *
         * <p>顺序边：返回全部后继。
         * <p>条件边：按声明顺序 First-Match-Wins；default 兜底且最多一个。
         */
        private Set<String> resolveSuccessors(String nodeId) {
            List<Edge> edges = outgoingByNode.getOrDefault(nodeId, List.of());
            if (edges.isEmpty()) {
                return Set.of();
            }
            boolean conditional = false;
            for (Edge edge : edges) {
                if (edge.getLabel() != null && !edge.getLabel().isBlank()) {
                    conditional = true;
                    break;
                }
            }
            if (!conditional) {
                // 顺序边：全部后继都需要被调度。
                Set<String> next = new LinkedHashSet<>();
                for (Edge edge : edges) {
                    next.add(edge.getTo());
                }
                return next;
            }

            // 条件边：按声明顺序 First-Match-Wins，default 仅在全部不命中时生效。
            Edge defaultEdge = null;
            for (Edge edge : edges) {
                String raw = edge.getLabel() == null ? "" : edge.getLabel().trim();
                if (raw.isEmpty()) {
                    continue;
                }
                if ("default".equalsIgnoreCase(raw)) {
                    if (defaultEdge != null) {
                        throw new WorkflowExecutionException("default 分支只能存在一个: " + nodeId);
                    }
                    defaultEdge = edge;
                    continue;
                }
                Object value = expressionEngine.evaluate(raw, runtime.getContext().getVariables());
                boolean matched = toBoolean(value, raw);
                if (matched) {
                    return Set.of(edge.getTo());
                }
            }
            if (defaultEdge != null) {
                return Set.of(defaultEdge.getTo());
            }
            throw new NoRouteMatchedException("节点无可匹配分支: " + nodeId);
        }

        /**
         * 将表达式结果转换为布尔值。
         *
         * <p>允许：
         * <p>- Boolean
         * <p>- String("true"/"false")
         * <p>- Number(0=false, 非0=true)
         *
         * <p>其余类型一律视为表达式非法。
         */
        private boolean toBoolean(Object value, String expression) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            if (value instanceof String str) {
                String normalized = str.trim().toLowerCase(Locale.ROOT);
                if ("true".equals(normalized)) {
                    return true;
                }
                if ("false".equals(normalized)) {
                    return false;
                }
                throw new ExpressionEvaluationException("表达式返回值不可转 Boolean: " + expression);
            }
            if (value instanceof Number number) {
                return number.doubleValue() != 0d;
            }
            throw new ExpressionEvaluationException("表达式返回值不可转 Boolean: " + expression);
        }

        /**
         * 读取节点 retry 次数，缺省为 0。
         */
        private int resolveRetry(String nodeId) {
            NodeMeta nodeMeta = meta.getNodeMeta().get(nodeId);
            if (nodeMeta == null || nodeMeta.getRetry() == null) {
                return 0;
            }
            return Math.max(0, nodeMeta.getRetry());
        }

        /**
         * 执行resolveUnitTimeout并返回结果。
         * @return 执行结果
         */
        private long resolveUnitTimeout(String nodeId, GroupRuntime group) {
            // 超时优先级：Node > Group > Global。
            NodeMeta nodeMeta = meta.getNodeMeta().get(nodeId);
            if (nodeMeta != null && nodeMeta.getTimeout() != null && nodeMeta.getTimeout() > 0) {
                return nodeMeta.getTimeout();
            }
            if (group != null) {
                GroupMeta groupMeta = meta.getGroupMeta().get(group.groupId);
                long parsedGroupTimeout = parseTimeout(groupMeta.getAttributes().get("ms"), groupMeta.getAttributes().get("timeout"));
                if (parsedGroupTimeout > 0) {
                    return parsedGroupTimeout;
                }
            }
            Long globalNodeTimeoutMs = options.getGlobalNodeTimeoutMs();
            return globalNodeTimeoutMs == null ? -1L : globalNodeTimeoutMs;
        }

        /**
         * 解析 timeout 值。
         *
         * <p>支持：
         * <p>- ms=5000
         * <p>- timeout=5s/3m/1h
         *
         * <p>非法输入返回 -1，表示“当前层未提供有效超时”。
         */
        private long parseTimeout(String ms, String timeout) {
            if (ms != null && !ms.isBlank()) {
                try {
                    return Long.parseLong(ms.trim());
                } catch (NumberFormatException ignore) {
                    return -1L;
                }
            }
            if (timeout == null || timeout.isBlank()) {
                return -1L;
            }
            String normalized = timeout.trim().toLowerCase(Locale.ROOT);
            int split = 0;
            while (split < normalized.length() && Character.isDigit(normalized.charAt(split))) {
                split++;
            }
            if (split == 0 || split == normalized.length()) {
                return -1L;
            }
            long amount;
            try {
                amount = Long.parseLong(normalized.substring(0, split));
            } catch (NumberFormatException ignore) {
                return -1L;
            }
            String unit = normalized.substring(split);
            return switch (unit) {
                case "ms" -> amount;
                case "s" -> amount * 1000L;
                case "m" -> amount * 60_000L;
                case "h" -> amount * 3_600_000L;
                default -> -1L;
            };
        }

        /**
         * 执行mergeOutputs。
         */
        private void mergeOutputs(Map<String, Object> outputs) {
            if (outputs == null || outputs.isEmpty()) {
                return;
            }
            runtime.getContext().getVariables().putAll(outputs);
        }

        /**
         * 判断globalTimeoutExceeded。
         * @return true 表示globalTimeoutExceeded
         */
        private boolean isGlobalTimeoutExceeded() {
            Long globalTimeoutMs = options.getGlobalTimeoutMs();
            if (globalTimeoutMs == null || globalTimeoutMs <= 0) {
                return false;
            }
            return System.currentTimeMillis() - startedAtMs > globalTimeoutMs;
        }

        /**
         * 判断failFast。
         * @return true 表示failFast
         */
        private boolean isFailFast() {
            return options.getFailureStrategy() == FailureStrategy.FAIL_FAST;
        }

        /**
         * 终止 workflow（幂等）。
         *
         * <p>一旦 workflowState 已设置，不允许再次覆盖，保证首个终止原因优先。
         */
        private void terminateWorkflow(ExecutionState state, String message, Throwable error) {
            if (workflowState != null) {
                return;
            }
            workflowState = state;
            workflowMessage = message;
            workflowError = error;
        }

        /**
         * 执行cancelUnresolvedNodes。
         */
        private void cancelUnresolvedNodes() {
            for (String nodeId : graph.getNodes().keySet()) {
                if (!resolved.contains(nodeId)) {
                    resolveCancelled(nodeId, null);
                }
            }
        }

        /**
         * 执行resolveLeftNodesAsCancelled。
         */
        private void resolveLeftNodesAsCancelled() {
            // 反复扫描直到不再有可判定节点，避免遗漏“链式不可达”节点。
            boolean changed = true;
            while (changed) {
                changed = false;
                for (String nodeId : graph.getNodes().keySet()) {
                    if (!resolved.contains(nodeId) && pendingParents.get(nodeId).isEmpty()) {
                        resolveCancelled(nodeId, null);
                        changed = true;
                    }
                }
            }
            for (String nodeId : graph.getNodes().keySet()) {
                if (!resolved.contains(nodeId)) {
                    resolveCancelled(nodeId, null);
                }
            }
        }

        /**
         * 执行deriveFinalWorkflowState并返回结果。
         * @return 执行结果
         */
        private ExecutionState deriveFinalWorkflowState() {
            // 当前实现下：只要存在 TIMEOUT 则整体 TIMEOUT；否则存在 FAILED 则 FAILED；其余 SUCCESS。
            boolean anyFailed = false;
            boolean anyTimeout = false;
            for (String nodeId : graph.getNodes().keySet()) {
                ExecutionState state = runtime.getState(nodeId);
                if (state == ExecutionState.TIMEOUT) {
                    anyTimeout = true;
                }
                if (state == ExecutionState.FAILED) {
                    anyFailed = true;
                }
            }
            if (anyTimeout) {
                return ExecutionState.TIMEOUT;
            }
            if (anyFailed) {
                return ExecutionState.FAILED;
            }
            return ExecutionState.SUCCESS;
        }

        /**
         * 执行extractNodeStates并返回结果。
         * @return 执行结果
         */
        private Map<String, ExecutionState> extractNodeStates() {
            Map<String, ExecutionState> states = new LinkedHashMap<>();
            for (String nodeId : graph.getNodes().keySet()) {
                states.put(nodeId, runtime.getState(nodeId));
            }
            return states;
        }

        /**
         * 获取group。
         * @return group
         */
        private GroupRuntime getGroup(String nodeId) {
            String groupId = nodeToGroup.get(nodeId);
            if (groupId == null) {
                return null;
            }
            return groupById.get(groupId);
        }

        /**
         * 执行copyVariables并返回结果。
         * @return 执行结果
         */
        private Map<String, Object> copyVariables(Map<String, Object> source) {
            Map<String, Object> copied = new LinkedHashMap<>();
            if (source != null) {
                copied.putAll(source);
            }
            return copied;
        }

        /**
         * 流程结束后统一触发流程级后置拦截。
         *
         * <p>成功触发 onSuccess，其他终态触发 onFailure。
         */
        private void emitWorkflowInterceptors(WorkflowExecutionResult result, Map<String, Object> initialVariables) {
            if (result.getState() == ExecutionState.SUCCESS) {
                invokeWorkflowSuccess(result);
                return;
            }
            invokeWorkflowFailure(result.getWorkflowId(), initialVariables, result.getState(), result.getError());
        }
    }

    /**
     * 触发流程前置拦截（按注册顺序）。
     */
    private void invokeWorkflowBefore(String workflowId, Map<String, Object> variables) {
        for (WorkflowInterceptor interceptor : workflowInterceptors) {
            if (interceptor.supportsWorkflow(workflowId)) {
                // 仅命中范围的拦截器才会触发。
                interceptor.before(workflowId, variables);
            }
        }
    }

    /**
     * 触发流程成功拦截（按注册顺序）。
     */
    private void invokeWorkflowSuccess(WorkflowExecutionResult result) {
        for (WorkflowInterceptor interceptor : workflowInterceptors) {
            if (interceptor.supportsWorkflow(result.getWorkflowId())) {
                interceptor.onSuccess(result);
            }
        }
    }

    /**
     * 触发流程失败拦截（按注册顺序）。
     */
    private void invokeWorkflowFailure(String workflowId,
                                       Map<String, Object> variables,
                                       ExecutionState state,
                                       Throwable error) {
        for (WorkflowInterceptor interceptor : workflowInterceptors) {
            if (interceptor.supportsWorkflow(workflowId)) {
                interceptor.onFailure(workflowId, variables, state, error);
            }
        }
    }

    /**
     * 触发节点前置拦截（按注册顺序）。
     */
    private void invokeNodeBefore(NodeExecutionContext context) {
        for (NodeInterceptor interceptor : nodeInterceptors) {
            if (interceptor.supportsNode(context.getWorkflowId(), context.getNodeId())) {
                interceptor.before(context);
            }
        }
    }

    /**
     * 触发节点成功拦截（按注册顺序）。
     */
    private void invokeNodeSuccess(NodeExecutionContext context, NodeResult result) {
        for (NodeInterceptor interceptor : nodeInterceptors) {
            if (interceptor.supportsNode(context.getWorkflowId(), context.getNodeId())) {
                interceptor.onSuccess(context, result);
            }
        }
    }

    /**
     * 触发节点失败拦截（按注册顺序）。
     */
    private void invokeNodeFailure(NodeExecutionContext context, NodeResult result) {
        for (NodeInterceptor interceptor : nodeInterceptors) {
            if (interceptor.supportsNode(context.getWorkflowId(), context.getNodeId())) {
                interceptor.onFailure(context, result);
            }
        }
    }

    /**
     * 并行组执行期状态容器。
     *
     * <p>保存组内成员的执行结果，用于 ANY/ALL 聚合判定。
     */
    private static final class GroupRuntime {
        private final String groupId;
        private final ParallelMode mode;
        private final List<String> members;
        private final Map<String, ExecutionState> memberStates = new LinkedHashMap<>();
        private boolean completed;
        private boolean success;
        private String winner;

        /**
         * 执行GroupRuntime并返回结果。
         * @return 执行结果
         */
        private GroupRuntime(String groupId, ParallelMode mode, List<String> members) {
            this.groupId = groupId;
            this.mode = mode;
            this.members = new ArrayList<>(members);
        }

        /**
         * 执行hasSuccess并返回结果。
         * @return 执行结果
         */
        private boolean hasSuccess() {
            for (ExecutionState state : memberStates.values()) {
                if (state == ExecutionState.SUCCESS) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 执行anyTimeout并返回结果。
         * @return 执行结果
         */
        private boolean anyTimeout() {
            for (ExecutionState state : memberStates.values()) {
                if (state == ExecutionState.TIMEOUT) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 执行allMembersResolved并返回结果。
         * @return 执行结果
         */
        private boolean allMembersResolved(Set<String> resolvedNodes) {
            for (String member : members) {
                if (!resolvedNodes.contains(member)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 执行allMembersSuccess并返回结果。
         * @return 执行结果
         */
        private boolean allMembersSuccess() {
            for (String member : members) {
                if (memberStates.get(member) != ExecutionState.SUCCESS) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 单次 attempt 的归一化结果。
     *
     * <p>调度层只依赖该结构，不直接依赖 Worker/子流程原始返回，
     * 便于统一处理成功传播、失败传播、重试与并行聚合。
     */
    private static final class AttemptOutcome {
        private final ExecutionState state;
        private final Map<String, Object> outputs;
        private final Set<String> selectedSuccessors;
        private final String message;
        private final Throwable error;

        /**
         * 执行AttemptOutcome并返回结果。
         * @return 执行结果
         */
        private AttemptOutcome(ExecutionState state,
                               Map<String, Object> outputs,
                               Set<String> selectedSuccessors,
                               String message,
                               Throwable error) {
            this.state = state;
            this.outputs = outputs;
            this.selectedSuccessors = selectedSuccessors;
            this.message = message;
            this.error = error;
        }

        /**
         * 执行success并返回结果。
         * @return 执行结果
         */
        private static AttemptOutcome success(Map<String, Object> outputs, Set<String> selectedSuccessors) {
            return new AttemptOutcome(
                    ExecutionState.SUCCESS,
                    outputs == null ? Map.of() : new LinkedHashMap<>(outputs),
                    new LinkedHashSet<>(selectedSuccessors),
                    null,
                    null
            );
        }

        /**
         * 执行failed并返回结果。
         * @return 执行结果
         */
        private static AttemptOutcome failed(String message, Throwable error) {
            return new AttemptOutcome(ExecutionState.FAILED, Map.of(), Set.of(), message, error);
        }

        /**
         * 执行timeout并返回结果。
         * @return 执行结果
         */
        private static AttemptOutcome timeout(String message) {
            return new AttemptOutcome(ExecutionState.TIMEOUT, Map.of(), Set.of(), message, null);
        }

        /**
         * 执行cancelled并返回结果。
         * @return 执行结果
         */
        private static AttemptOutcome cancelled(String message) {
            return new AttemptOutcome(ExecutionState.CANCELLED, Map.of(), Set.of(), message, null);
        }
    }
}
