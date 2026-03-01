package com.gaibu.flowlab.parser.api.model;

import com.gaibu.flowlab.parser.api.enums.Direction;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程图结构模型。
 *
 * <p>该对象只表达拓扑信息（节点/边/方向），不承载执行期状态。
 */
@Getter
@Setter
public class Graph {

    /**
     * 图 id（通常与 workflow id 一致）。
     */
    private String id;
    /**
     * 图方向（TD/LR 等）。
     */
    private Direction direction;
    /**
     * 节点表（nodeId -> node）。
     */
    private Map<String, Node> nodes = new LinkedHashMap<>();
    /**
     * 边列表（按源码声明顺序）。
     */
    private List<Edge> edges = new ArrayList<>();

    /**
     * 创建空图模型。
     */
    public Graph() {
    }

    /**
     * 创建图模型。
     *
     * @param id 图标识，通常对应 workflow id
     * @param direction 图布局方向
     */
    public Graph(String id, Direction direction) {
        this.id = id;
        this.direction = direction;
    }

    /**
     * 添加节点。
     *
     * <p>节点以 id 为键保存；同 id 节点会被新值覆盖，因此上层应在写入前校验唯一性。
     */
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    /**
     * 获取node。
     * @return node
     */
    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * 判断指定节点是否存在。
     *
     * @param nodeId 节点 id
     * @return true 表示存在
     */
    public boolean containsNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    /**
     * 添加边。
     *
     * <p>该方法仅负责存储，不做语义校验；边合法性由解析器/校验器负责。
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
