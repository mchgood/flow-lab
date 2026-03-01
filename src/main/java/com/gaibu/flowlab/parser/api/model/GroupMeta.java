package com.gaibu.flowlab.parser.api.model;

import com.gaibu.flowlab.parser.api.enums.GroupType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分组语义元数据。
 */
@Getter
@Setter
public class GroupMeta {

    /**
     * 分组 id。
     */
    private String groupId;
    /**
     * 分组类型（当前主要是并行组）。
     */
    private GroupType type;
    /**
     * 分组属性（mode、any_complete_to 等）。
     */
    private Map<String, String> attributes = new LinkedHashMap<>();
    /**
     * 分组成员节点 id 列表。
     */
    private List<String> nodeIds = new ArrayList<>();

    /**
     * 构造GroupMeta实例。
     */
    public GroupMeta() {
    }

    /**
     * 构造GroupMeta实例。
     */
    public GroupMeta(String groupId, GroupType type, Map<String, String> attributes, List<String> nodeIds) {
        this.groupId = groupId;
        this.type = type;
        this.attributes = new LinkedHashMap<>(attributes);
        this.nodeIds = new ArrayList<>(nodeIds);
    }

}
