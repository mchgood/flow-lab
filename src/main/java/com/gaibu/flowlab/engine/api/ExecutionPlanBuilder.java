package com.gaibu.flowlab.engine.api;

import com.gaibu.flowlab.engine.model.ExecutionPlan;
import com.gaibu.flowlab.parser.api.model.WorkflowDefinition;

/**
 * 执行计划构建器。
 */
public interface ExecutionPlanBuilder {

    ExecutionPlan build(WorkflowDefinition definition);
}

