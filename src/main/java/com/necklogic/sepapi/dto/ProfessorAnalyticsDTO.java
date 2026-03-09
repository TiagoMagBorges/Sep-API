package com.necklogic.sepapi.dto;

import java.util.List;
import java.util.UUID;

public record ProfessorAnalyticsDTO(
        UUID studentId,
        String studentName,
        long totalLessons,
        long attendedLessons,
        long missedLessons,
        double attendanceRate,
        List<LessonNoteDTO> privateNotes
) {}