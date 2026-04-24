package com.smartstudy.plan.service;

import com.smartstudy.plan.entity.StudyPlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanServicePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void generatedPlansRemainAvailableAfterServiceRestart() {
        Path storageFile = tempDir.resolve("plans.json");
        AiPlanService aiPlanService = new AiPlanService(new RestTemplateBuilder(), "", "https://api.deepseek.com", "deepseek-chat");

        PlanService firstService = new PlanService(aiPlanService, storageFile);
        StudyPlan createdPlan = firstService.generate("30天学完Spring Cloud", 30, 2);

        PlanService restartedService = new PlanService(aiPlanService, storageFile);
        List<StudyPlan> plans = restartedService.list();

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getId()).isEqualTo(createdPlan.getId());
        assertThat(plans.get(0).getTitle()).isEqualTo("30天学完Spring Cloud");
        assertThat(plans.get(0).getTasks()).hasSize(4);
    }
}
