package com.smartstudy.resource.controller;

import com.smartstudy.common.api.ApiResponse;
import com.smartstudy.resource.entity.LearningResource;
import com.smartstudy.resource.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearningResource>>> search(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(resourceService.search(keyword)));
    }
}
