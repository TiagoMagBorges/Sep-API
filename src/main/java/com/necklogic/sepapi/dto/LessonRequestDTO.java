package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.LessonStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonRequestDTO(
        @NotNull UUID studentId,
        @NotNull LocalDateTime dateTime,
        @NotNull LocalDateTime endTime,
        @NotNull LessonStatus status,
        String publicLog,
        String privateNotes
) {}