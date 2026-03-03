package com.gaibu.flowlab.engine.task.impl;

import com.gaibu.flowlab.engine.task.FlowTask;
import com.gaibu.flowlab.engine.task.TaskRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * 基于 Spring 容器的任务注册表。
 */
public class SpringBeanTaskRegistry implements TaskRegistry {

    /**
     * Spring 上下文。
     */
    private final ApplicationContext applicationContext;

    public SpringBeanTaskRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public FlowTask getTask(String nodeId) {
        try {
            return applicationContext.getBean(nodeId, FlowTask.class);
        } catch (BeansException ex) {
            return null;
        }
    }
}
