package com.gaibu.flowlab.parser.impl;

import com.gaibu.flowlab.exception.ValidationException;
import com.gaibu.flowlab.parser.api.DirectiveScanner;
import com.gaibu.flowlab.parser.api.FlowchartParser;
import com.gaibu.flowlab.parser.api.MarkdownParser;
import com.gaibu.flowlab.parser.api.MetaBinder;
import com.gaibu.flowlab.parser.api.WorkflowDefinitionParser;
import com.gaibu.flowlab.parser.api.model.Directive;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.GraphMeta;
import com.gaibu.flowlab.parser.api.model.MermaidDocument;
import com.gaibu.flowlab.parser.api.model.SubflowMeta;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WorkflowDefinition 解析入口实现。
 */
public class DefaultWorkflowDefinitionParser implements WorkflowDefinitionParser {

    private final MarkdownParser markdownParser;
    private final FlowchartParser flowchartParser;
    private final DirectiveScanner directiveScanner;
    private final MetaBinder metaBinder;

    /**
     * 构造DefaultWorkflowDefinitionParser实例。
     */
    public DefaultWorkflowDefinitionParser() {
        this(new DefaultMarkdownParser(), new DefaultFlowchartParser(), new DefaultDirectiveScanner(), new DefaultMetaBinder());
    }

    /**
     * 构造DefaultWorkflowDefinitionParser实例。
     */
    public DefaultWorkflowDefinitionParser(MarkdownParser markdownParser,
                                           FlowchartParser flowchartParser,
                                           DirectiveScanner directiveScanner,
                                           MetaBinder metaBinder) {
        this.markdownParser = markdownParser;
        this.flowchartParser = flowchartParser;
        this.directiveScanner = directiveScanner;
        this.metaBinder = metaBinder;
    }

    @Override
    /**
     * 执行parse并返回结果。
     * @return 执行结果
     */
    public List<WorkflowDefinition> parse(String markdownContent) {
        List<MermaidDocument> documents = markdownParser.parse(markdownContent);
        List<WorkflowDefinition> definitions = new ArrayList<>();
        Map<String, WorkflowDefinition> definitionsById = new LinkedHashMap<>();

        for (MermaidDocument document : documents) {
            Graph graph = flowchartParser.parse(document.getSource());
            graph.setId(document.getId());
            List<Directive> directives = directiveScanner.scan(document.getSource());
            GraphMeta meta = metaBinder.bind(graph, directives);
            WorkflowDefinition definition = new WorkflowDefinition(document.getId(), document.getDescription(), graph, meta);
            definitions.add(definition);
            definitionsById.put(definition.getId(), definition);
        }

        validateSubflowReferences(definitionsById);
        return definitions;
    }

    /**
     * 执行validateSubflowReferences。
     */
    private void validateSubflowReferences(Map<String, WorkflowDefinition> definitionsById) {
        Map<String, Set<String>> refs = new HashMap<>();
        for (WorkflowDefinition definition : definitionsById.values()) {
            Set<String> targets = new HashSet<>();
            for (SubflowMeta subflow : definition.getMeta().getSubflows().values()) {
                if (!definitionsById.containsKey(subflow.getReferenceId())) {
                    throw new ValidationException("子流程引用不存在: " + subflow.getReferenceId(), subflow.getNodeId());
                }
                if (definition.getId().equals(subflow.getReferenceId())) {
                    throw new ValidationException("子流程禁止直接自引用", subflow.getNodeId());
                }
                targets.add(subflow.getReferenceId());
            }
            refs.put(definition.getId(), targets);
        }
        detectCycle(refs);
    }

    /**
     * 执行detectCycle。
     */
    private void detectCycle(Map<String, Set<String>> refs) {
        Map<String, Integer> state = new HashMap<>();
        for (String node : refs.keySet()) {
            if (state.getOrDefault(node, 0) == 0) {
                dfs(node, refs, state);
            }
        }
    }

    /**
     * 执行dfs。
     */
    private void dfs(String node, Map<String, Set<String>> refs, Map<String, Integer> state) {
        state.put(node, 1);
        for (String next : refs.getOrDefault(node, Set.of())) {
            int st = state.getOrDefault(next, 0);
            if (st == 1) {
                throw new ValidationException("子流程引用存在循环: " + node + " -> " + next);
            }
            if (st == 0) {
                dfs(next, refs, state);
            }
        }
        state.put(node, 2);
    }
}
