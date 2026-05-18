package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.LessonStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonRequestDTO(
        UUID studentId,
        UUID classGroupId,
        LocalDateTime dateTime,
        LocalDateTime endTime,
        LessonStatus status,
        String publicLog,
        String privateNotes
) {
}