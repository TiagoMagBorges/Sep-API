package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.DashboardSummaryDTO;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.repository.StudentRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.FinanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final FinanceRepository financeRepository;

    public DashboardSummaryDTO getSummary(UUID professorId) {
        long totalStudents = studentRepository.countByProfessorIdAndActiveTrue(professorId);

        LocalDateTime startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
        long lessonsInWeek = lessonRepository.countByProfessorIdAndDateTimeBetweenAndStatus(professorId, startOfWeek, endOfWeek, LessonStatus.SCHEDULED);

        BigDecimal pendingPayments = financeRepository.sumAmountByProfessorIdAndStatus(professorId, PaymentStatus.PENDING)
                .orElse(BigDecimal.ZERO);

        return new DashboardSummaryDTO(totalStudents, lessonsInWeek, pendingPayments);
    }
}