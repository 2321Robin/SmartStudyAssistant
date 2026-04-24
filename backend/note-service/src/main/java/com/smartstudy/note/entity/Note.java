package com.smartstudy.note.entity;

import java.time.Instant;

public record Note(
        Long id,
        String title,
        String content,
        boolean publicVisible,
        Instant createdAt
) {
}
