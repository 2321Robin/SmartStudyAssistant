package com.smartstudy.resource.entity;

import java.util.List;

public record LearningResource(
        Long id,
        String title,
        String description,
        String source,
        String level,
        String type,
        List<String> tags,
        String url
) {
}
