package com.necklogic.sepapi.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupAnalyticsDTO(

        UUID classGroupId,
        String classGroupName,
        double attendanceRate,
        List<StudentResponseDTO> students,
        List<LessonNoteDTO> privateNotes

) {}
