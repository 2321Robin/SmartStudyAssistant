package com.smartstudy.plan.controller;

import com.smartstudy.common.api.ApiResponse;
import com.smartstudy.plan.entity.StudyPlan;
import com.smartstudy.plan.service.PlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<StudyPlan>> generate(@Valid @RequestBody GeneratePlanRequest request) {
        StudyPlan plan = planService.generate(request.goal(), request.durationDays(), request.dailyHours());
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyPlan>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(planService.list()));
    }

    @PatchMapping("/{planId}/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<StudyPlan>> updateTaskStatus(
            @PathVariable Long planId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        StudyPlan updatedPlan = planService.updateTaskStatus(planId, taskId, request.status());
        if (updatedPlan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Plan or task not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(updatedPlan));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long planId) {
        if (!planService.delete(planId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Plan not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    public record GeneratePlanRequest(
            @NotBlank String goal,
            @NotNull @Min(1) @Max(365) Integer durationDays,
            @NotNull @Min(1) @Max(24) Integer dailyHours
    ) {
    }

    public record UpdateTaskStatusRequest(
            @NotBlank @Pattern(regexp = "PENDING|COMPLETED") String status
    ) {
    }
}
