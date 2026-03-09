package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    long countByProfessorIdAndDateTimeBetweenAndStatus(UUID professorId, LocalDateTime start, LocalDateTime end, LessonStatus status);
}