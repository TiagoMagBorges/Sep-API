package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.*;
import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final FinanceRepository financeRepository;
    private final ClassGroupRepository classGroupRepository;

    public ProfessorAnalyticsDTO getStudentAnalytics(UUID studentId, UUID professorId, LocalDateTime start, LocalDateTime end) {
        Student student = studentRepository.findByIdAndProfessorId(studentId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Lesson> lessons = lessonRepository.findAllByStudentIdAndDateTimeBetweenOrderByDateTimeAsc(studentId, start, end);

        long total = lessons.size();
        long attended = lessons.stream().filter(l -> l.getStatus() == LessonStatus.COMPLETED).count();
        long missed = lessons.stream().filter(l -> l.getStatus() == LessonStatus.CANCELED).count();
        double rate = total > 0 ? (double) attended / total * 100 : 0.0;

        List<LessonNoteDTO> notes = lessons.stream()
                .filter(l -> l.getPrivateNotes() != null && !l.getPrivateNotes().isBlank())
                .map(l -> new LessonNoteDTO(l.getDateTime().toString(), l.getPrivateNotes()))
                .collect(Collectors.toList());

        return new ProfessorAnalyticsDTO(student.getId(), student.getName(), total, attended, missed, rate, notes);
    }

    public FinanceAnalyticsDTO getFinanceAnalytics(UUID professorId, LocalDate start, LocalDate end) {
        List<Finance> finances = financeRepository.findAllByProfessorIdAndDueDateBetweenOrderByDueDateDesc(professorId, start, end);

        BigDecimal totalPaid = finances.stream()
                .filter(f -> f.getStatus() == PaymentStatus.PAID)
                .map(Finance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = finances.stream()
                .filter(f -> f.getStatus() == PaymentStatus.PENDING)
                .map(Finance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOverdue = finances.stream()
                .filter(f -> f.getStatus() == PaymentStatus.OVERDUE)
                .map(Finance::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinanceAnalyticsDTO(totalPaid, totalPending, totalOverdue);
    }

    public ClassGroupAnalyticsDTO getClassGroupAnalytics(UUID classGroupId, UUID professorId, LocalDateTime start, LocalDateTime end) {
        ClassGroup classGroup = classGroupRepository.findByIdAndProfessorId(classGroupId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Lesson> lessons = lessonRepository.findAllByClassGroupIdAndDateTimeBetweenOrderByDateTimeAsc(classGroupId, start, end);

        long total = lessons.size();
        long attended = lessons.stream().filter(l -> l.getStatus() == LessonStatus.COMPLETED).count();
        double rate = total > 0 ? (double) attended / total * 100 : 0.0;

        List<StudentResponseDTO> studentDTOs = classGroup.getStudents() != null ?
                classGroup.getStudents().stream()
                        .map(s -> new StudentResponseDTO(
                                s.getId(),
                                s.getName(),
                                s.getSubject(),
                                s.isActive(),
                                s.getBillingType(),
                                s.getCreditBalance()
                        ))
                        .collect(Collectors.toList()) : List.of();

        List<LessonNoteDTO> notes = lessons.stream()
                .filter(l -> l.getPrivateNotes() != null && !l.getPrivateNotes().isBlank())
                .map(l -> new LessonNoteDTO(l.getDateTime().toString(), l.getPrivateNotes()))
                .collect(Collectors.toList());

        return new ClassGroupAnalyticsDTO(
                classGroup.getId(),
                classGroup.getName(),
                rate,
                studentDTOs,
                notes
        );
    }
}