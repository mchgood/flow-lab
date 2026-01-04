package com.gaibu.flowlab;

import com.gaibu.flowlab.service.FlowParserService;
import com.gaibu.flowlab.transformer.model.FlowGraph;

/**
 * 使用示例
 */
public class Example {

    public static void main(String[] args) {
        FlowParserService service = new FlowParserService();

        // 示例 1：简单流程图
        String mermaid1 = """
                flowchart TD
                    A[开始] --> B[结束]
                """;

        System.out.println("=== 示例 1：简单流程图 ===");
        String json1 = service.parseToJson(mermaid1);
        System.out.println(json1);

        // 示例 2：决策流程图
        String mermaid2 = """
                flowchart TD
                    A[开始] --> B{判断条件}
                    B -->|是| C[处理A]
                    B -->|否| D[处理B]
                    C --> E((结束))
                    D --> E
                """;

        System.out.println("\n=== 示例 2：决策流程图 ===");
        String json2 = service.parseToJson(mermaid2);
        System.out.println(json2);

        // 示例 3：使用 FlowGraph 对象
        System.out.println("\n=== 示例 3：使用 FlowGraph 对象 ===");
        FlowGraph graph = service.parse(mermaid1);
        System.out.println("节点数量: " + graph.getNodes().size());
        System.out.println("边数量: " + graph.getEdges().size());
        graph.getNodes().forEach(node ->
                System.out.println("节点: " + node.getId() + " - " + node.getLabel() + " (" + node.getShape() + ")")
        );
        graph.getEdges().forEach(edge ->
                System.out.println("边: " + edge.getFrom() + " -> " + edge.getTo() +
                        (edge.getLabel().isEmpty() ? "" : " [" + edge.getLabel() + "]"))
        );
    }
}
