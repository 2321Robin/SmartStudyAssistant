package com.smartstudy.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "smartstudy.storage.plan-file=target/test-data/plan-controller-test.json",
        "deepseek.api-key="
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PlanControllerTest {

    private static final Path STORAGE_FILE = Path.of("target/test-data/plan-controller-test.json");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanUpStorage() throws Exception {
        Files.deleteIfExists(STORAGE_FILE);
    }

    @Test
    void generateReturnsDeterministicStudyPlan() throws Exception {
        mockMvc.perform(post("/api/plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"30天学完Spring Cloud","durationDays":30,"dailyHours":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("30天学完Spring Cloud"))
                .andExpect(jsonPath("$.data.tasks.length()").value(4));
    }

    @Test
    void generateReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"","durationDays":0,"dailyHours":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void listReturnsGeneratedPlans() throws Exception {
        createPlan();

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("30天学完Spring Cloud"));
    }

    @Test
    void listReturnsNewestPlansFirst() throws Exception {
        createPlan("先创建的计划");
        createPlan("后创建的计划");

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("后创建的计划"))
                .andExpect(jsonPath("$.data[1].title").value("先创建的计划"));
    }

    @Test
    void updateStatusChangesTaskStatus() throws Exception {
        JsonNode plan = createPlan();
        long planId = plan.get("id").asLong();
        long taskId = plan.get("tasks").get(0).get("id").asLong();

        mockMvc.perform(patch("/api/plans/{planId}/tasks/{taskId}/status", planId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tasks[0].status").value("COMPLETED"));
    }

    @Test
    void deleteRemovesPlanFromList() throws Exception {
        JsonNode plan = createPlan();

        mockMvc.perform(delete("/api/plans/{id}", plan.get("id").asLong()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void updateStatusReturnsNotFoundWhenPlanDoesNotExist() throws Exception {
        mockMvc.perform(patch("/api/plans/{planId}/tasks/{taskId}/status", 999, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateStatusReturnsBadRequestForInvalidStatus() throws Exception {
        JsonNode plan = createPlan();

        mockMvc.perform(patch("/api/plans/{planId}/tasks/{taskId}/status", plan.get("id").asLong(), plan.get("tasks").get(0).get("id").asLong())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PROGRESS"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    private JsonNode createPlan() throws Exception {
        return createPlan("30天学完Spring Cloud");
    }

    private JsonNode createPlan(String goal) throws Exception {
        String response = mockMvc.perform(post("/api/plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"%s","durationDays":30,"dailyHours":2}
                                """.formatted(goal)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("data");
    }
}
