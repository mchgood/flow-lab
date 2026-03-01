package com.gaibu.flowlab.parser.api;

import com.gaibu.flowlab.parser.api.model.Directive;
import com.gaibu.flowlab.parser.api.model.Graph;
import com.gaibu.flowlab.parser.api.model.GraphMeta;

import java.util.List;

/**
 * 语义绑定接口。
 */
public interface MetaBinder {

    /**
     * 将结构图与指令列表绑定为语义信息。
     *
     * @param graph 图结构
     * @param directives 指令列表
     * @return 绑定后的元数据
     */
    GraphMeta bind(Graph graph, List<Directive> directives);
}
