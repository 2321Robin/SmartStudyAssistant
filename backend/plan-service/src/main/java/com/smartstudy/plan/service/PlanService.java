package com.smartstudy.plan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.plan.entity.StudyPlan;
import com.smartstudy.plan.entity.StudyTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PlanService {

    private static final TypeReference<List<StudyPlan>> PLAN_LIST_TYPE = new TypeReference<>() {
    };

    private final AiPlanService aiPlanService;
    private final ObjectMapper objectMapper;
    private final Path storageFile;
    private final AtomicLong planIdGenerator = new AtomicLong(1);
    private final AtomicLong taskIdGenerator = new AtomicLong(1);
    private final Map<Long, StudyPlan> plans = new LinkedHashMap<>();

    @Autowired
    public PlanService(
            AiPlanService aiPlanService,
            ObjectMapper objectMapper,
            @Value("${smartstudy.storage.plan-file:.smartstudyassistant/plans.json}") String storageFile) {
        this(aiPlanService, objectMapper, Path.of(storageFile));
    }

    PlanService(AiPlanService aiPlanService, Path storageFile) {
        this(aiPlanService, new ObjectMapper(), storageFile);
    }

    PlanService(AiPlanService aiPlanService, ObjectMapper objectMapper, Path storageFile) {
        this.aiPlanService = aiPlanService;
        this.objectMapper = objectMapper;
        this.storageFile = storageFile;
        loadFromDisk();
    }

    public synchronized StudyPlan generate(String goal, Integer durationDays, Integer dailyHours) {
        List<StudyTask> tasks = new ArrayList<>();
        for (StudyTask task : aiPlanService.generateTasks(goal, durationDays, dailyHours)) {
            tasks.add(new StudyTask(taskIdGenerator.getAndIncrement(), task.getTitle(), task.getStage(), task.getStatus()));
        }

        StudyPlan plan = new StudyPlan(
                planIdGenerator.getAndIncrement(),
                goal,
                goal,
                durationDays,
                dailyHours,
                "PENDING",
                tasks
        );
        plans.put(plan.getId(), plan);
        saveToDisk();
        return plan;
    }

    public synchronized List<StudyPlan> list() {
        List<StudyPlan> orderedPlans = new ArrayList<>(plans.values());
        orderedPlans.sort((left, right) -> Long.compare(right.getId(), left.getId()));
        return orderedPlans;
    }

    public synchronized StudyPlan updateTaskStatus(Long planId, Long taskId, String status) {
        StudyPlan plan = plans.get(planId);
        if (plan == null) {
            return null;
        }

        for (StudyTask task : plan.getTasks()) {
            if (taskId.equals(task.getId())) {
                task.setStatus(status);
                updatePlanStatus(plan);
                saveToDisk();
                return plan;
            }
        }

        return null;
    }

    public synchronized boolean delete(Long planId) {
        boolean removed = plans.remove(planId) != null;
        if (removed) {
            saveToDisk();
        }
        return removed;
    }

    private void updatePlanStatus(StudyPlan plan) {
        boolean allCompleted = plan.getTasks().stream().allMatch(task -> "COMPLETED".equals(task.getStatus()));
        plan.setStatus(allCompleted ? "COMPLETED" : "PENDING");
    }

    private void loadFromDisk() {
        if (!Files.exists(storageFile)) {
            return;
        }

        try {
            List<StudyPlan> storedPlans = objectMapper.readValue(storageFile.toFile(), PLAN_LIST_TYPE);
            for (StudyPlan plan : storedPlans) {
                plans.put(plan.getId(), plan);
                planIdGenerator.set(Math.max(planIdGenerator.get(), plan.getId() + 1));
                for (StudyTask task : plan.getTasks()) {
                    if (task.getId() != null) {
                        taskIdGenerator.set(Math.max(taskIdGenerator.get(), task.getId() + 1));
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load plans from " + storageFile, exception);
        }
    }

    private void saveToDisk() {
        try {
            Path parent = storageFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storageFile.toFile(), plans.values());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save plans to " + storageFile, exception);
        }
    }
}
