package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.LessonStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponseDTO(
        UUID id,
        UUID studentId,
        String studentName,
        String subject,
        LocalDateTime dateTime,
        LocalDateTime endTime,
        LessonStatus status,
        String publicLog,
        String privateNotes
) {}