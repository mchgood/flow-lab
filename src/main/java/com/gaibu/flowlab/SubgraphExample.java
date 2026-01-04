package com.gaibu.flowlab;

import com.gaibu.flowlab.service.FlowParserService;
import com.gaibu.flowlab.transformer.model.FlowGraph;

/**
 * 子图解析示例
 */
public class SubgraphExample {

    public static void main(String[] args) {
        FlowParserService service = new FlowParserService();

        // 示例 1：简单子图
        System.out.println("=".repeat(60));
        System.out.println("示例 1：简单子图");
        System.out.println("=".repeat(60));

        String example1 = """
                flowchart TD
                    A[开始]
                    subgraph 子流程1
                        B[步骤1] --> C[步骤2]
                    end
                    A --> B
                    C --> D[结束]
                """;

        printMermaidAndResult(service, example1);

        // 示例 2：包含决策的子图
        System.out.println("\n" + "=".repeat(60));
        System.out.println("示例 2：包含决策的子图");
        System.out.println("=".repeat(60));

        String example2 = """
                flowchart TD
                    A[开始]
                    subgraph 决策子流程
                        B{条件判断} -->|是| C[处理]
                        B -->|否| D((跳过))
                    end
                    A --> B
                """;

        printMermaidAndResult(service, example2);

        // 示例 3：多个子图
        System.out.println("\n" + "=".repeat(60));
        System.out.println("示例 3：多个并列子图");
        System.out.println("=".repeat(60));

        String example3 = """
                flowchart TD
                    A[开始]

                    subgraph 子流程1
                        B[步骤1] --> C[步骤2]
                    end

                    subgraph 子流程2
                        D[步骤3] --> E[步骤4]
                    end

                    A --> B
                    C --> D
                    E --> F[结束]
                """;

        printMermaidAndResult(service, example3);

        // 示例 4：复杂的审批流程子图
        System.out.println("\n" + "=".repeat(60));
        System.out.println("示例 4：复杂的审批流程子图（包含循环）");
        System.out.println("=".repeat(60));

        String example4 = """
                flowchart TD
                    Start[开始]

                    subgraph 审批流程
                        Submit[提交申请] --> Review{审核}
                        Review -->|通过| Approve[批准]
                        Review -->|拒绝| Reject[驳回]
                        Reject --> Submit
                    end

                    Start --> Submit
                    Approve --> End((结束))
                """;

        printMermaidAndResult(service, example4);

        // 示例 5：并行处理子图
        System.out.println("\n" + "=".repeat(60));
        System.out.println("示例 5：并行处理子图");
        System.out.println("=".repeat(60));

        String example5 = """
                flowchart LR
                    Start[开始]

                    subgraph 并行处理
                        A[任务A]
                        B[任务B]
                        C[任务C]
                        A --> D[汇总]
                        B --> D
                        C --> D
                    end

                    Start --> A
                    Start --> B
                    Start --> C
                    D --> End[结束]
                """;

        printMermaidAndResult(service, example5);
    }

    private static void printMermaidAndResult(FlowParserService service, String mermaid) {
        System.out.println("\n【Mermaid 流程图】");
        System.out.println(mermaid);

        FlowGraph graph = service.parse(mermaid);

        System.out.println("【解析结果】");
        System.out.println("节点数量: " + graph.getNodes().size());
        System.out.println("边数量: " + graph.getEdges().size());

        System.out.println("\n【节点列表】");
        graph.getNodes().forEach(node ->
                System.out.printf("  - %-10s | %-15s | %s%n",
                        node.getId(),
                        node.getLabel(),
                        node.getShape())
        );

        System.out.println("\n【边列表】");
        graph.getEdges().forEach(edge ->
                System.out.printf("  - %s -> %s%s%n",
                        edge.getFrom(),
                        edge.getTo(),
                        edge.getLabel().isEmpty() ? "" : " [" + edge.getLabel() + "]")
        );

        System.out.println("\n【JSON 输出】");
        String json = service.parseToJson(mermaid);
        System.out.println(json);
    }
}
