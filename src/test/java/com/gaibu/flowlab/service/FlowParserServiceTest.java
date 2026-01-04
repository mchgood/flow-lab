package com.gaibu.flowlab.service;

import com.gaibu.flowlab.exception.ParseException;
import com.gaibu.flowlab.transformer.model.Edge;
import com.gaibu.flowlab.transformer.model.FlowGraph;
import com.gaibu.flowlab.transformer.model.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 流程解析服务测试
 */
class FlowParserServiceTest {

    private FlowParserService service;

    @BeforeEach
    void setUp() {
        service = new FlowParserService();
    }

    @Test
    void testParseSimpleFlowchart() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B[结束]
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getNodes()).hasSize(2);
        assertThat(result.getEdges()).hasSize(1);

        // 验证节点
        Node nodeA = findNodeById(result, "A");
        assertThat(nodeA).isNotNull();
        assertThat(nodeA.getLabel()).isEqualTo("开始");
        assertThat(nodeA.getShape()).isEqualTo("rectangle");

        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getLabel()).isEqualTo("结束");

        // 验证边
        Edge edge = result.getEdges().get(0);
        assertThat(edge.getFrom()).isEqualTo("A");
        assertThat(edge.getTo()).isEqualTo("B");
        assertThat(edge.getLabel()).isEqualTo("");
    }

    @Test
    void testParseDiamondNode() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B{判断}
                    B --> C[结束]
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getNodes()).hasSize(3);
        assertThat(result.getEdges()).hasSize(2);

        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getLabel()).isEqualTo("判断");
        assertThat(nodeB.getShape()).isEqualTo("diamond");
    }

    @Test
    void testParseCircleNode() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B((结束))
                """;

        FlowGraph result = service.parse(mermaid);

        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getLabel()).isEqualTo("结束");
        assertThat(nodeB.getShape()).isEqualTo("circle");
    }

    @Test
    void testParseRoundRectangleNode() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B([处理])
                """;

        FlowGraph result = service.parse(mermaid);

        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getLabel()).isEqualTo("处理");
        assertThat(nodeB.getShape()).isEqualTo("round_rectangle");
    }

    @Test
    void testParseEdgeWithLabel() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B{判断}
                    B -->|是| C[处理A]
                    B -->|否| D[处理B]
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getEdges()).hasSize(3);

        // 查找带标签的边
        Edge edgeToC = findEdge(result, "B", "C");
        assertThat(edgeToC).isNotNull();
        assertThat(edgeToC.getLabel()).isEqualTo("是");

        Edge edgeToD = findEdge(result, "B", "D");
        assertThat(edgeToD).isNotNull();
        assertThat(edgeToD.getLabel()).isEqualTo("否");
    }

    @Test
    void testParseComplexFlowchart() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B{判断条件}
                    B -->|是| C[处理A]
                    B -->|否| D[处理B]
                    C --> E((结束))
                    D --> E
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getNodes()).hasSize(5);
        assertThat(result.getEdges()).hasSize(5);
    }

    @Test
    void testParseLRDirection() {
        String mermaid = """
                flowchart LR
                    A[开始] --> B[结束]
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getNodes()).hasSize(2);
        assertThat(result.getEdges()).hasSize(1);
    }

    @Test
    void testParseToJson() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B[结束]
                """;

        String json = service.parseToJson(mermaid);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"nodes\"");
        assertThat(json).contains("\"edges\"");
        assertThat(json).contains("\"id\" : \"A\"");
        assertThat(json).contains("\"label\" : \"开始\"");
    }

    @Test
    void testParseToCompactJson() {
        String mermaid = """
                flowchart TD
                    A[开始] --> B[结束]
                """;

        String json = service.parseToCompactJson(mermaid);

        assertThat(json).isNotNull();
        assertThat(json).contains("\"nodes\"");
        assertThat(json).doesNotContain("\n"); // 紧凑格式不应该有换行
    }

    @Test
    void testValidate() {
        String validMermaid = """
                flowchart TD
                    A[开始] --> B[结束]
                """;

        assertThat(service.validate(validMermaid)).isTrue();
    }

    @Test
    void testValidateInvalid() {
        String invalidMermaid = "invalid mermaid syntax";

        assertThat(service.validate(invalidMermaid)).isFalse();
    }

    @Test
    void testParseEmptyInput() {
        assertThatThrownBy(() -> service.parse(""))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("不能为空");

        assertThatThrownBy(() -> service.parse(null))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("不能为空");
    }

    @Test
    void testParseMultipleEdgesFromSameNode() {
        String mermaid = """
                flowchart TD
                    A --> B
                    A --> C
                    A --> D
                """;

        FlowGraph result = service.parse(mermaid);

        assertThat(result.getNodes()).hasSize(4);
        assertThat(result.getEdges()).hasSize(3);

        // 验证所有边都从 A 出发
        assertThat(result.getEdges())
                .allMatch(edge -> edge.getFrom().equals("A"));
    }

    // ==================== 子图测试用例 ====================

    @Test
    void testParseSimpleSubgraph() {
        String mermaid = """
                flowchart TD
                    A[开始]
                    subgraph 子流程1
                        B[步骤1] --> C[步骤2]
                    end
                    A --> B
                    C --> D[结束]
                """;

        FlowGraph result = service.parse(mermaid);

        // 验证节点数量（A, B, C, D）
        assertThat(result.getNodes()).hasSize(4);

        // 验证所有节点都被正确解析
        assertThat(findNodeById(result, "A")).isNotNull();
        assertThat(findNodeById(result, "B")).isNotNull();
        assertThat(findNodeById(result, "C")).isNotNull();
        assertThat(findNodeById(result, "D")).isNotNull();

        // 验证边数量（B->C, A->B, C->D）
        assertThat(result.getEdges()).hasSize(3);

        // 验证子图内的边
        Edge edgeInSubgraph = findEdge(result, "B", "C");
        assertThat(edgeInSubgraph).isNotNull();

        // 验证主图到子图的边
        Edge edgeToSubgraph = findEdge(result, "A", "B");
        assertThat(edgeToSubgraph).isNotNull();

        // 验证子图到主图的边
        Edge edgeFromSubgraph = findEdge(result, "C", "D");
        assertThat(edgeFromSubgraph).isNotNull();
    }

    @Test
    void testParseSubgraphWithDifferentNodeShapes() {
        String mermaid = """
                flowchart TD
                    A[开始]
                    subgraph 决策子流程
                        B{条件判断} -->|是| C[处理]
                        B -->|否| D((跳过))
                    end
                    A --> B
                """;

        FlowGraph result = service.parse(mermaid);

        // 验证节点
        assertThat(result.getNodes()).hasSize(4);

        // 验证菱形节点
        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getLabel()).isEqualTo("条件判断");
        assertThat(nodeB.getShape()).isEqualTo("diamond");

        // 验证矩形节点
        Node nodeC = findNodeById(result, "C");
        assertThat(nodeC).isNotNull();
        assertThat(nodeC.getShape()).isEqualTo("rectangle");

        // 验证圆形节点
        Node nodeD = findNodeById(result, "D");
        assertThat(nodeD).isNotNull();
        assertThat(nodeD.getLabel()).isEqualTo("跳过");
        assertThat(nodeD.getShape()).isEqualTo("circle");

        // 验证边标签
        Edge edgeToC = findEdge(result, "B", "C");
        assertThat(edgeToC).isNotNull();
        assertThat(edgeToC.getLabel()).isEqualTo("是");

        Edge edgeToD = findEdge(result, "B", "D");
        assertThat(edgeToD).isNotNull();
        assertThat(edgeToD.getLabel()).isEqualTo("否");
    }

    @Test
    void testParseMultipleSubgraphs() {
        String mermaid = """
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

        FlowGraph result = service.parse(mermaid);

        // 验证节点数量（A, B, C, D, E, F）
        assertThat(result.getNodes()).hasSize(6);

        // 验证边数量（B->C, D->E, A->B, C->D, E->F）
        assertThat(result.getEdges()).hasSize(5);

        // 验证第一个子图的边
        assertThat(findEdge(result, "B", "C")).isNotNull();

        // 验证第二个子图的边
        assertThat(findEdge(result, "D", "E")).isNotNull();

        // 验证子图之间的连接
        assertThat(findEdge(result, "C", "D")).isNotNull();
    }

    @Test
    void testParseSubgraphWithComplexFlow() {
        String mermaid = """
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

        FlowGraph result = service.parse(mermaid);

        // 验证节点数量
        assertThat(result.getNodes()).hasSize(6);
        // Start, Submit, Review, Approve, Reject, End

        // 验证边数量
        assertThat(result.getEdges()).hasSize(6);
        // Submit->Review, Review->Approve, Review->Reject, Reject->Submit, Start->Submit, Approve->End

        // 验证子图内的循环边（Reject -> Submit）
        Edge cycleEdge = findEdge(result, "Reject", "Submit");
        assertThat(cycleEdge).isNotNull();

        // 验证决策节点
        Node reviewNode = findNodeById(result, "Review");
        assertThat(reviewNode).isNotNull();
        assertThat(reviewNode.getShape()).isEqualTo("diamond");

        // 验证所有相关的边
        assertThat(findEdge(result, "Submit", "Review")).isNotNull();
        assertThat(findEdge(result, "Review", "Approve")).isNotNull();
        assertThat(findEdge(result, "Review", "Reject")).isNotNull();
        assertThat(findEdge(result, "Start", "Submit")).isNotNull();
        assertThat(findEdge(result, "Approve", "End")).isNotNull();
    }

    @Test
    void testParseSubgraphWithOnlyNodes() {
        String mermaid = """
                flowchart TD
                    A[开始]

                    subgraph 子流程
                        B[节点1]
                        C[节点2]
                    end

                    A --> B
                    A --> C
                """;

        FlowGraph result = service.parse(mermaid);

        // 验证节点
        assertThat(result.getNodes()).hasSize(3);

        // 验证边（只有主图到子图的边，子图内没有边）
        assertThat(result.getEdges()).hasSize(2);
        assertThat(findEdge(result, "A", "B")).isNotNull();
        assertThat(findEdge(result, "A", "C")).isNotNull();
    }

    @Test
    void testParseSubgraphWithParallelBranches() {
        String mermaid = """
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

        FlowGraph result = service.parse(mermaid);

        // 验证节点数量（Start, A, B, C, D, End）
        assertThat(result.getNodes()).hasSize(6);

        // 验证边数量
        // 子图内: A->D, B->D, C->D (3条)
        // 主图到子图: Start->A, Start->B, Start->C (3条)
        // 子图到主图: D->End (1条)
        // 总计: 7条
        assertThat(result.getEdges()).hasSize(7);

        // 验证汇总节点的入边
        assertThat(findEdge(result, "A", "D")).isNotNull();
        assertThat(findEdge(result, "B", "D")).isNotNull();
        assertThat(findEdge(result, "C", "D")).isNotNull();

        // 验证起始节点的出边
        assertThat(findEdge(result, "Start", "A")).isNotNull();
        assertThat(findEdge(result, "Start", "B")).isNotNull();
        assertThat(findEdge(result, "Start", "C")).isNotNull();

        // 验证结束边
        assertThat(findEdge(result, "D", "End")).isNotNull();
    }

    @Test
    void testParseSubgraphWithRoundRectangleNodes() {
        String mermaid = """
                flowchart TD
                    A[开始]

                    subgraph 数据处理流程
                        B([接收数据]) --> C([验证数据])
                        C --> D([存储数据])
                    end

                    A --> B
                    D --> E((完成))
                """;

        FlowGraph result = service.parse(mermaid);

        // 验证圆角矩形节点
        Node nodeB = findNodeById(result, "B");
        assertThat(nodeB).isNotNull();
        assertThat(nodeB.getShape()).isEqualTo("round_rectangle");

        Node nodeC = findNodeById(result, "C");
        assertThat(nodeC).isNotNull();
        assertThat(nodeC.getShape()).isEqualTo("round_rectangle");

        Node nodeD = findNodeById(result, "D");
        assertThat(nodeD).isNotNull();
        assertThat(nodeD.getShape()).isEqualTo("round_rectangle");

        // 验证圆形节点
        Node nodeE = findNodeById(result, "E");
        assertThat(nodeE).isNotNull();
        assertThat(nodeE.getShape()).isEqualTo("circle");

        // 验证边
        assertThat(result.getEdges()).hasSize(4);
    }

    @Test
    void testParseEmptySubgraph() {
        String mermaid = """
                flowchart TD
                    A[开始]

                    subgraph 空子流程
                    end

                    A --> B[结束]
                """;

        FlowGraph result = service.parse(mermaid);

        // 空子图不应影响解析
        assertThat(result.getNodes()).hasSize(2);
        assertThat(result.getEdges()).hasSize(1);
        assertThat(findEdge(result, "A", "B")).isNotNull();
    }

    // 辅助方法：根据 ID 查找节点
    private Node findNodeById(FlowGraph graph, String id) {
        return graph.getNodes().stream()
                .filter(node -> node.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 辅助方法：查找边
    private Edge findEdge(FlowGraph graph, String from, String to) {
        return graph.getEdges().stream()
                .filter(edge -> edge.getFrom().equals(from) && edge.getTo().equals(to))
                .findFirst()
                .orElse(null);
    }
}
