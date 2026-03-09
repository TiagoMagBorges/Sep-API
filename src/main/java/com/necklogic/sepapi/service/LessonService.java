package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.LessonRequestDTO;
import com.necklogic.sepapi.dto.LessonResponseDTO;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;

    public List<LessonResponseDTO> listByInterval(UUID professorId, LocalDateTime start, LocalDateTime end) {
        return lessonRepository.findAllByProfessorIdAndDateTimeBetweenOrderByDateTimeAsc(professorId, start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public LessonResponseDTO getById(UUID id, UUID professorId) {
        Lesson lesson = lessonRepository.findById(id)
                .filter(l -> l.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapToDTO(lesson);
    }

    public LessonResponseDTO create(LessonRequestDTO dto, Professor professor) {
        if (dto.endTime().isBefore(dto.dateTime()) || dto.endTime().isEqual(dto.dateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        if (lessonRepository.existsOverlappingLesson(professor.getId(), dto.dateTime(), dto.endTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Schedule conflict: There is already a lesson in this time slot.");
        }

        Student student = studentRepository.findByIdAndProfessorId(dto.studentId(), professor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        Lesson lesson = Lesson.builder()
                .dateTime(dto.dateTime())
                .endTime(dto.endTime())
                .status(dto.status())
                .student(student)
                .professor(professor)
                .build();

        return mapToDTO(lessonRepository.save(lesson));
    }

    public LessonResponseDTO update(UUID id, LessonRequestDTO dto, UUID professorId) {
        if (dto.endTime().isBefore(dto.dateTime()) || dto.endTime().isEqual(dto.dateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        Lesson lesson = lessonRepository.findById(id)
                .filter(l -> l.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (lessonRepository.existsOverlappingLessonExcludingId(professorId, id, dto.dateTime(), dto.endTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Schedule conflict: There is already a lesson in this time slot.");
        }

        Student student = studentRepository.findByIdAndProfessorId(dto.studentId(), professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        lesson.setDateTime(dto.dateTime());
        lesson.setEndTime(dto.endTime());
        lesson.setStatus(dto.status());
        lesson.setStudent(student);

        return mapToDTO(lessonRepository.save(lesson));
    }

    public void delete(UUID id, UUID professorId) {
        Lesson lesson = lessonRepository.findById(id)
                .filter(l -> l.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        lessonRepository.delete(lesson);
    }

    private LessonResponseDTO mapToDTO(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getStudent().getId(),
                lesson.getStudent().getName(),
                lesson.getStudent().getSubject(),
                lesson.getDateTime(),
                lesson.getEndTime(),
                lesson.getStatus()
        );
    }
}