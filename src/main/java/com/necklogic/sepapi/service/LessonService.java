package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.LessonRequestDTO;
import com.necklogic.sepapi.dto.LessonResponseDTO;
import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.BillingType;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ClassGroupRepository classGroupRepository;

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

    @Transactional
    public LessonResponseDTO create(LessonRequestDTO dto, Professor professor) {
        if (!dto.endTime().isAfter(dto.dateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        if (lessonRepository.existsOverlappingLesson(professor.getId(), dto.dateTime(), dto.endTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Schedule conflict.");
        }

        Student student = null;
        ClassGroup classGroup = null;

        if (dto.studentId() != null) {
            student = studentRepository.findByIdAndProfessorId(dto.studentId(), professor.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
            processCreditAdjustment(student, null, dto.status());
        } else if (dto.classGroupId() != null) {
            classGroup = classGroupRepository.findByIdAndProfessorId(dto.classGroupId(), professor.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class group not found"));
            processGroupCreditAdjustment(classGroup, null, dto.status());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson must have a student or class group");
        }

        Lesson lesson = Lesson.builder()
                .dateTime(dto.dateTime())
                .endTime(dto.endTime())
                .status(dto.status())
                .publicLog(dto.publicLog())
                .privateNotes(dto.privateNotes())
                .student(student)
                .classGroup(classGroup)
                .professor(professor)
                .build();

        return mapToDTO(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponseDTO update(UUID id, LessonRequestDTO dto, UUID professorId) {
        if (!dto.endTime().isAfter(dto.dateTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        Lesson lesson = lessonRepository.findById(id)
                .filter(l -> l.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (lessonRepository.existsOverlappingLessonExcludingId(professorId, id, dto.dateTime(), dto.endTime())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Schedule conflict.");
        }

        if (lesson.getStudent() != null) {
            processCreditAdjustment(lesson.getStudent(), lesson.getStatus(), dto.status());
        } else if (lesson.getClassGroup() != null) {
            processGroupCreditAdjustment(lesson.getClassGroup(), lesson.getStatus(), dto.status());
        }

        lesson.setDateTime(dto.dateTime());
        lesson.setEndTime(dto.endTime());
        lesson.setStatus(dto.status());
        lesson.setPublicLog(dto.publicLog());
        lesson.setPrivateNotes(dto.privateNotes());

        return mapToDTO(lessonRepository.save(lesson));
    }

    @Transactional
    public void delete(UUID id, UUID professorId) {
        Lesson lesson = lessonRepository.findById(id)
                .filter(l -> l.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (lesson.getStudent() != null) {
            processCreditAdjustment(lesson.getStudent(), lesson.getStatus(), null);
        } else if (lesson.getClassGroup() != null) {
            processGroupCreditAdjustment(lesson.getClassGroup(), lesson.getStatus(), null);
        }

        lessonRepository.delete(lesson);
    }

    private void processGroupCreditAdjustment(ClassGroup classGroup, LessonStatus oldStatus, LessonStatus newStatus) {
        for (Student student : classGroup.getStudents()) {
            processCreditAdjustment(student, oldStatus, newStatus);
        }
    }

    private void processCreditAdjustment(Student student, LessonStatus oldStatus, LessonStatus newStatus) {
        if (student.getBillingType() != BillingType.CREDIT_PACKAGE) {
            return;
        }

        boolean oldConsumed = oldStatus != null && oldStatus != LessonStatus.CANCELED;
        boolean newConsumed = newStatus != null && newStatus != LessonStatus.CANCELED;

        if (!oldConsumed && newConsumed) {
            student.setCreditBalance(student.getCreditBalance() - 1);
            studentRepository.save(student);
        } else if (oldConsumed && !newConsumed) {
            student.setCreditBalance(student.getCreditBalance() + 1);
            studentRepository.save(student);
        }
    }

    private LessonResponseDTO mapToDTO(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getStudent() != null ? lesson.getStudent().getId() : null,
                lesson.getStudent() != null ? lesson.getStudent().getName() : null,
                lesson.getClassGroup() != null ? lesson.getClassGroup().getId() : null,
                lesson.getClassGroup() != null ? lesson.getClassGroup().getName() : null,
                lesson.getStudent() != null ? lesson.getStudent().getSubject() : "Turma",
                lesson.getDateTime(),
                lesson.getEndTime(),
                lesson.getStatus(),
                lesson.getPublicLog(),
                lesson.getPrivateNotes()
        );
    }
}