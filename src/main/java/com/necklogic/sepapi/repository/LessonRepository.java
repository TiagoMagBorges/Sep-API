package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    long countByProfessorIdAndDateTimeBetweenAndStatus(UUID professorId, LocalDateTime start, LocalDateTime end, LessonStatus status);

    List<Lesson> findAllByProfessorIdAndDateTimeBetweenOrderByDateTimeAsc(UUID professorId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.professor.id = :professorId AND l.status != 'CANCELED' AND l.dateTime < :endTime AND l.endTime > :startTime")
    boolean existsOverlappingLesson(@Param("professorId") UUID professorId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(l) > 0 FROM Lesson l WHERE l.professor.id = :professorId AND l.id != :lessonId AND l.status != 'CANCELED' AND l.dateTime < :endTime AND l.endTime > :startTime")
    boolean existsOverlappingLessonExcludingId(@Param("professorId") UUID professorId, @Param("lessonId") UUID lessonId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Lesson> findAllByStudentIdAndDateTimeBetweenOrderByDateTimeAsc(UUID studentId, LocalDateTime start, LocalDateTime end);
}