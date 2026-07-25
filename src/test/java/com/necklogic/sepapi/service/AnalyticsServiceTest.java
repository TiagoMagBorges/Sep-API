package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.FinanceAnalyticsDTO;
import com.necklogic.sepapi.dto.ProfessorAnalyticsDTO;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private FinanceRepository financeRepository;
    @Mock
    private ClassGroupRepository classGroupRepository;

    @InjectMocks private AnalyticsService analyticsService;

    @Test
    @DisplayName("Deve calcular corretamente a taxa de presença do aluno (75% para 3 concluídas em 4 aulas)")
    void shouldCalculateAttendanceRateCorrectly() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        Student student = Student.builder()
                .id(studentId)
                .name("Nick Nery")
                .professor(professor)
                .build();

        // cria 4 aulas: 3 concluídas e 1 cancelada por falta
        Lesson aula1 = Lesson.builder().status(LessonStatus.COMPLETED).dateTime(LocalDateTime.now()).build();
        Lesson aula2 = Lesson.builder().status(LessonStatus.COMPLETED).dateTime(LocalDateTime.now()).build();
        Lesson aula3 = Lesson.builder().status(LessonStatus.COMPLETED).dateTime(LocalDateTime.now()).build();
        Lesson aula4 = Lesson.builder().status(LessonStatus.CANCELED).dateTime(LocalDateTime.now()).build();

        List<Lesson> listaDeAulas = List.of(aula1, aula2, aula3, aula4);

        // ensina os mocks a responderem às consultas do serviço
        when(studentRepository.findByIdAndProfessorId(studentId, professorId))
                .thenReturn(Optional.of(student));

        when(lessonRepository.findAllByStudentIdAndDateTimeBetweenOrderByDateTimeAsc(any(), any(), any()))
                .thenReturn(listaDeAulas);

        // ==========================================
        // 2. ACT
        // ==========================================
        ProfessorAnalyticsDTO resultado = analyticsService.getStudentAnalytics(
                studentId,
                professorId,
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now()
        );

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // valida se o total de aulas e as contagens individuais foram mapeadas corretamente
        assertEquals(4, resultado.totalLessons(), "O total de aulas deve ser 4");
        assertEquals(3, resultado.attendedLessons(), "As aulas concluídas devem somar 3");
        assertEquals(1, resultado.missedLessons(), "As faltas devem somar 1");

        // valida a precisão matemática da taxa de presença (3 de 4 = 75.0%)
        assertEquals(75.0, resultado.attendanceRate(), 0.01, "A taxa de presença deve ser exatamente 75.0%");
    }

    @Test
    @DisplayName("Deve somar corretamente os totais financeiros por status (PAID, PENDING e OVERDUE)")
    void shouldSumFinanceTotalsByStatusCorrectly() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        // criamos 4 registros financeiros com valores e status diferentes
        Finance pagto1 = Finance.builder().amount(new BigDecimal("150.00")).status(PaymentStatus.PAID).professor(professor).build();
        Finance pagto2 = Finance.builder().amount(new BigDecimal("50.00")).status(PaymentStatus.PAID).professor(professor).build();
        Finance pagto3 = Finance.builder().amount(new BigDecimal("300.00")).status(PaymentStatus.PENDING).professor(professor).build();
        Finance pagto4 = Finance.builder().amount(new BigDecimal("120.00")).status(PaymentStatus.OVERDUE).professor(professor).build();

        List<Finance> listaDeFinancas = List.of(pagto1, pagto2, pagto3, pagto4);

        // ensinamos o financeRepository a devolver a nossa lista quando o serviço pedir
        when(financeRepository.findAllByProfessorIdAndDueDateBetweenOrderByDueDateDesc(any(), any(), any()))
                .thenReturn(listaDeFinancas);

        // ==========================================
        // 2. ACT
        // ==========================================
        FinanceAnalyticsDTO resultado = analyticsService.getFinanceAnalytics(
                professorId,
                LocalDate.now().minusMonths(1),
                LocalDate.now()
        );

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // valida se os pagamentos concluídos somam R$ 200,00 (150 + 50)
        assertEquals(new BigDecimal("200.00"), resultado.totalPaid(), "O total pago deve ser R$ 200.00");

        // Valida se os pendentes isolaram exatamente R$ 300,00
        assertEquals(new BigDecimal("300.00"), resultado.totalPending(), "O total pendente deve ser R$ 300.00");

        // Valida se os atrasados isolaram exatamente R$ 120,00
        assertEquals(new BigDecimal("120.00"), resultado.totalOverdue(), "O total em atraso deve ser R$ 120.00");
    }
}