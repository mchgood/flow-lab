package com.gaibu.flowlab.engine.repository;

import com.gaibu.flowlab.engine.model.ProcessDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessDefinitionRepository 测试
 */
class ProcessDefinitionRepositoryTest {

    @Test
    void testSaveAndFindById() {
        ProcessDefinitionRepository repository = new ProcessDefinitionRepository();
        ProcessDefinition definition = new ProcessDefinition();
        definition.setId("def-1");
        definition.setName("流程A");

        repository.save(definition);

        ProcessDefinition found = repository.findById("def-1");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("流程A");
    }

    @Test
    void testFindByNameAndFindAll() {
        ProcessDefinitionRepository repository = new ProcessDefinitionRepository();

        ProcessDefinition def1 = new ProcessDefinition();
        def1.setId("def-1");
        def1.setName("流程A");
        repository.save(def1);

        ProcessDefinition def2 = new ProcessDefinition();
        def2.setId("def-2");
        def2.setName("流程A");
        repository.save(def2);

        ProcessDefinition def3 = new ProcessDefinition();
        def3.setId("def-3");
        def3.setName("流程B");
        repository.save(def3);

        List<ProcessDefinition> byName = repository.findByName("流程A");
        assertThat(byName).hasSize(2);

        assertThat(repository.findAll()).hasSize(3);
    }

    @Test
    void testDeleteById() {
        ProcessDefinitionRepository repository = new ProcessDefinitionRepository();
        ProcessDefinition definition = new ProcessDefinition();
        definition.setId("def-1");
        definition.setName("流程A");
        repository.save(definition);

        repository.deleteById("def-1");

        assertThat(repository.findById("def-1")).isNull();
        assertThat(repository.findAll()).isEmpty();
    }
}
