package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.LessonRequestDTO;
import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.BillingType;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 1. Ativa o motor do Mockito no JUnit 5
class LessonServiceTest {

    // cria simuladores para as dependências de LessonService no construtor
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ClassGroupRepository classGroupRepository;

    // injeta os simuladores acima dentro da instância real do LessonService
    @InjectMocks
    private LessonService lessonService;

    @Test
    @DisplayName("Deve lançar ResponseStatusException (CONFLICT) ao tentar agendar aula em horário já ocupado.")
    void shouldThrowExceptionWhenScheduleConflicts() {

        // ==========================================
        // ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        LocalDateTime inicio = LocalDateTime.of(2026, 8, 10, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 10, 15, 0);

        LessonRequestDTO dto = new LessonRequestDTO(
                UUID.randomUUID(), null, inicio, fim, null, null, null
        );

        // repositório falso responde true se já existe aula no horário
        when(lessonRepository.existsOverlappingLesson(
                any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(true);

        // ==========================================
        // ACT & ASSERT
        // ==========================================
        // captura a exceção do método create() quando ele encontra conflito
        ResponseStatusException excecaoCapturada = assertThrows(
                ResponseStatusException.class,
                () -> lessonService.create(dto, professor)
        );

        // valida se o status HTTP e a mensagem de exceção escolhida são iguais as do service real
        assertEquals(HttpStatus.CONFLICT, excecaoCapturada.getStatusCode());
        assertEquals("Schedule conflict.", excecaoCapturada.getReason());
    }

    @Test
    @DisplayName("Deve descontar 1 crédito do aluno de pacote quando a aula for concluída")
    void shouldDeductCreditWhenCompletingLessonForPackageStudent() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        // cria um aluno com cobrança por pacote e com 5 crétditos
        Student student = Student.builder()
                .id(studentId)
                .name("Nick Nery")
                .billingType(BillingType.CREDIT_PACKAGE)
                .creditBalance(5)
                .professor(professor)
                .build();

        // DTO simula a requisição de uma aula concluída
        LessonRequestDTO dto = new LessonRequestDTO(
                studentId,
                null,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                LessonStatus.COMPLETED,
                "Aula de Matemática 2",
                null
        );

        // ensina aos mocks como responder ao fluxo sem apontar conflito e encontrar aluno
        when(lessonRepository.existsOverlappingLesson(any(), any(), any())).thenReturn(false);
        when(studentRepository.findByIdAndProfessorId(studentId, professorId)).thenReturn(Optional.of(student));

        // devolve apenas o próprio objeto quando o repository tentar  salvar a aula
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArguments()[0]);

        // ==========================================
        // 2. ACT
        // ==========================================
        lessonService.create(dto, professor);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // valida se o cálculo dos créditos funcionou (-1 de saldo)
        assertEquals(4, student.getCreditBalance(), "O saldo de créditos deve ser reduzido em exatamente 1");

        // verifica se o serviço chamou studentRepositoru.save() no banco apenas
        // uma vez para persistir o novo saldo de créditos
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    @DisplayName("Deve devolver 1 crédito ao aluno de pacote quando uma aula concluída for revertida para cancelada")
    void shouldRefundCreditWhenRevertingCompletedLesson() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        // aluno de pacote com saldo 4
        Student student = Student.builder()
                .id(studentId)
                .name("Nick Nery")
                .billingType(BillingType.CREDIT_PACKAGE)
                .creditBalance(4)
                .professor(professor)
                .build();

        // aula já existente no banco com o status antigo: COMPLETED
        Lesson aulaExistenteNoBanco = Lesson.builder()
                .id(lessonId)
                .student(student)
                .professor(professor)
                .dateTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .status(LessonStatus.COMPLETED)
                .build();

        // DTO com nova requisição: professor muda o status para CANCELED
        LessonRequestDTO dtoEdicao = new LessonRequestDTO(
                studentId,
                null,
                aulaExistenteNoBanco.getDateTime(),
                aulaExistenteNoBanco.getEndTime(),
                LessonStatus.CANCELED,
                "Aula cancelada pelo aluno com antecedência",
                null
        );

        // o serviço aciona findById(id) e filtra o professor por memória
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(aulaExistenteNoBanco));

        // o método update aciona a checagem que exclui o ID da própria aula
        when(lessonRepository.existsOverlappingLessonExcludingId(any(), any(), any(), any()))
                .thenReturn(false);

        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArguments()[0]);

        // ==========================================
        // 2. ACT
        // ==========================================
        lessonService.update(lessonId, dtoEdicao, professorId);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // o saldo antigo de 4 deve subir de volta para 5 (+1 crédito estornado)
        assertEquals(5, student.getCreditBalance(), "O saldo do aluno deve ser estornado em +1 crédito");

        // garante que o serviço mandou o repository salvar a devolução do crédito
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    @DisplayName("Deve lançar ResponseStatusException (BAD_REQUEST) ao tentar criar aula com hora final anterior à hora inicial")
    void shouldThrowExceptionWhenEndTimeIsBeforeOrEqualToStartTime() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        Professor professor = Professor.builder().id(UUID.randomUUID()).build();

        // data inicial: 15h00 - Data final: 14h00 (inválido)
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 10, 15, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 8, 10, 14, 0);

        LessonRequestDTO dto = new LessonRequestDTO(
                UUID.randomUUID(),
                null,
                inicio,
                fim,
                LessonStatus.SCHEDULED,
                "Aula com horário inválido",
                null
        );

        // ==========================================
        // 2 & 3. ACT & ASSERT
        // ==========================================
        // capturam a exceção disparada na primeira linha de validação do método create()
        ResponseStatusException excecaoCapturada = assertThrows(
                ResponseStatusException.class,
                () -> lessonService.create(dto, professor)
        );

        // valida se o status HTTP 400 Bad Request e a razão exata coincidem com a regra do service
        assertEquals(HttpStatus.BAD_REQUEST, excecaoCapturada.getStatusCode());
        assertEquals("End time must be after start time", excecaoCapturada.getReason());

        // verifica que o método save() do lessonRepository foi acionado nenhuma vez
        verify(lessonRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Deve descontar 1 crédito de TODOS os alunos de pacote ao concluir uma aula em grupo")
    void shouldDeductCreditFromAllStudentsWhenCompletingGroupLesson() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        UUID classGroupId = UUID.randomUUID();
        Professor professor = Professor.builder().id(professorId).build();

        // cria 2 alunos com pacote de créditos e saldos diferentes na carteira
        Student aluno1 = Student.builder()
                .id(UUID.randomUUID())
                .name("Carlos (Guitarra)")
                .billingType(BillingType.CREDIT_PACKAGE)
                .creditBalance(5) // Começa com 5[cite: 1]
                .professor(professor)
                .build();

        Student aluno2 = Student.builder()
                .id(UUID.randomUUID())
                .name("Ana (Guitarra)")
                .billingType(BillingType.CREDIT_PACKAGE)
                .creditBalance(3) // Começa com 3[cite: 1]
                .professor(professor)
                .build();

        // Cria a turma englobando os dois alunos criados
        ClassGroup turma = ClassGroup.builder()
                .id(classGroupId)
                .name("Prática de Conjunto")
                .professor(professor)
                .students(List.of(aluno1, aluno2)) // lista de matriculados
                .build();

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fim = inicio.plusHours(1);

        // DTO agenda a aula pela ID da TURMA e manda o status como COMPLETED
        LessonRequestDTO dto = new LessonRequestDTO(
                null, // studentId é null porque é aula em grupo
                classGroupId,
                inicio,
                fim,
                LessonStatus.COMPLETED,
                "Aula de ensaio geral",
                null
        );

        // ensina o repositório de turmas a devolver o grupo falso ao ser consultado
        when(lessonRepository.existsOverlappingLesson(any(), any(), any())).thenReturn(false);
        when(classGroupRepository.findByIdAndProfessorId(classGroupId, professorId)).thenReturn(Optional.of(turma));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArguments()[0]);

        // ==========================================
        // 2. ACT
        // ==========================================
        lessonService.create(dto, professor);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // valida o cálculo de cobrança para cada estudante da turma
        assertEquals(4, aluno1.getCreditBalance(), "O saldo do aluno 1 deve cair de 5 para 4");
        assertEquals(2, aluno2.getCreditBalance(), "O saldo do aluno 2 deve cair de 3 para 2");

        // garante que o serviço ordenou a atualização no banco de dados para os DOIS alunos
        verify(studentRepository, times(1)).save(aluno1);
        verify(studentRepository, times(1)).save(aluno2);
    }
}