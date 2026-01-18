package com.gaibu.flowlab.engine.repository;

import com.gaibu.flowlab.engine.enums.ProcessInstanceStatus;
import com.gaibu.flowlab.engine.model.ProcessInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProcessInstanceRepository 测试
 */
class ProcessInstanceRepositoryTest {

    @Test
    void testSaveAndFindById() {
        ProcessInstanceRepository repository = new ProcessInstanceRepository();
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-1");
        instance.setProcessDefinitionId("def-1");
        instance.setBusinessKey("BK-1");
        instance.setStatus(ProcessInstanceStatus.RUNNING);

        repository.save(instance);

        ProcessInstance found = repository.findById("inst-1");
        assertThat(found).isNotNull();
        assertThat(found.getBusinessKey()).isEqualTo("BK-1");
    }

    @Test
    void testFindByDefinitionIdAndStatus() {
        ProcessInstanceRepository repository = new ProcessInstanceRepository();

        ProcessInstance inst1 = new ProcessInstance();
        inst1.setId("inst-1");
        inst1.setProcessDefinitionId("def-1");
        inst1.setStatus(ProcessInstanceStatus.RUNNING);
        repository.save(inst1);

        ProcessInstance inst2 = new ProcessInstance();
        inst2.setId("inst-2");
        inst2.setProcessDefinitionId("def-1");
        inst2.setStatus(ProcessInstanceStatus.COMPLETED);
        repository.save(inst2);

        ProcessInstance inst3 = new ProcessInstance();
        inst3.setId("inst-3");
        inst3.setProcessDefinitionId("def-2");
        inst3.setStatus(ProcessInstanceStatus.RUNNING);
        repository.save(inst3);

        List<ProcessInstance> byDef = repository.findByDefinitionId("def-1");
        assertThat(byDef).hasSize(2);

        List<ProcessInstance> running = repository.findByStatus(ProcessInstanceStatus.RUNNING);
        assertThat(running).hasSize(2);
    }

    @Test
    void testFindByBusinessKeyAndDelete() {
        ProcessInstanceRepository repository = new ProcessInstanceRepository();
        ProcessInstance instance = new ProcessInstance();
        instance.setId("inst-1");
        instance.setProcessDefinitionId("def-1");
        instance.setBusinessKey("BK-1");
        instance.setStatus(ProcessInstanceStatus.RUNNING);
        repository.save(instance);

        assertThat(repository.findByBusinessKey("BK-1")).isNotNull();

        repository.deleteById("inst-1");

        assertThat(repository.findById("inst-1")).isNull();
        assertThat(repository.findAll()).isEmpty();
    }
}
