package com.necklogic.sepapi.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.BillingType;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.ProfessorRepository;
import com.necklogic.sepapi.repository.StudentRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final FinanceRepository financeRepository;
    private final ClassGroupRepository classGroupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (professorRepository.count() > 0) {
            return;
        }

        Professor professor = Professor.builder()
                .name("Tiago Borges")
                .email("admin@admin.com")
                .password(passwordEncoder.encode("123456"))
                .build();

        professorRepository.save(professor);

        ClassGroup group1 = ClassGroup.builder()
                .name("Turma de Teoria Musical - Turma A")
                .professor(professor)
                .build();

        ClassGroup group2 = ClassGroup.builder()
                .name("Prática de Conjunto")
                .professor(professor)
                .build();

        ClassGroup group3 = ClassGroup.builder()
                .name("Masterclass de Improvisação")
                .professor(professor)
                .build();

        classGroupRepository.saveAll(List.of(group1, group2, group3));

        Student student1 = Student.builder().name("Carlos Silva").subject("Teoria Musical").active(true).billingType(BillingType.MONTHLY).creditBalance(0).professor(professor).classGroup(group1).build();
        Student student2 = Student.builder().name("Ana Beatriz").subject("Guitarra").active(true).billingType(BillingType.CREDIT_PACKAGE).creditBalance(4).professor(professor).classGroup(group2).build();
        Student student3 = Student.builder().name("Marcos Paulo").subject("Produção Musical").active(true).billingType(BillingType.CREDIT_PACKAGE).creditBalance(0).professor(professor).build();
        Student student4 = Student.builder().name("Julia Santos").subject("Harmonia").active(true).billingType(BillingType.CREDIT_PACKAGE).creditBalance(2).professor(professor).classGroup(group1).build();
        Student student5 = Student.builder().name("Pedro Henrique").subject("Baixo").active(true).billingType(BillingType.MONTHLY).creditBalance(0).professor(professor).classGroup(group2).build();
        Student student6 = Student.builder().name("Nick Nery").subject("Guitarra").active(true).billingType(BillingType.CREDIT_PACKAGE).creditBalance(10).professor(professor).classGroup(group3).build();

        studentRepository.saveAll(List.of(student1, student2, student3, student4, student5, student6));

        LocalDateTime today = LocalDateTime.now();

        Lesson lesson1 = Lesson.builder()
                .dateTime(today.plusDays(1).withHour(14).withMinute(0).withSecond(0))
                .endTime(today.plusDays(1).withHour(15).withMinute(0).withSecond(0))
                .status(LessonStatus.SCHEDULED)
                .student(student1)
                .professor(professor)
                .build();

        Lesson lesson2 = Lesson.builder()
                .dateTime(today.withHour(16).withMinute(0).withSecond(0))
                .endTime(today.withHour(17).withMinute(0).withSecond(0))
                .status(LessonStatus.SCHEDULED)
                .classGroup(group2)
                .professor(professor)
                .build();

        Lesson lesson3 = Lesson.builder()
                .dateTime(today.minusDays(2).withHour(10).withMinute(0).withSecond(0))
                .endTime(today.minusDays(2).withHour(11).withMinute(0).withSecond(0))
                .status(LessonStatus.COMPLETED)
                .publicLog("Revisão de escalas maiores e menores. Ótimo progresso na digitação.")
                .privateNotes("Dificuldade pontual na transição de acordes com pestana, focar nisso na próxima aula.")
                .student(student2)
                .professor(professor)
                .build();

        Lesson lesson4 = Lesson.builder()
                .dateTime(today.minusDays(5).withHour(14).withMinute(0).withSecond(0))
                .endTime(today.minusDays(5).withHour(15).withMinute(0).withSecond(0))
                .status(LessonStatus.CANCELED)
                .privateNotes("Faltou sem avisar. Risco de evasão. Necessário entrar em contato com os pais para alinhar engajamento.")
                .student(student4)
                .professor(professor)
                .build();

        Lesson lesson5 = Lesson.builder()
                .dateTime(today.minusDays(7).withHour(18).withMinute(0).withSecond(0))
                .endTime(today.minusDays(7).withHour(19).withMinute(0).withSecond(0))
                .status(LessonStatus.COMPLETED)
                .publicLog("Introdução aos modos gregos e improvisação inicial.")
                .classGroup(group1)
                .professor(professor)
                .build();

        Lesson lesson6 = Lesson.builder()
                .dateTime(today.minusDays(15).withHour(19).withMinute(0).withSecond(0))
                .endTime(today.minusDays(15).withHour(20).withMinute(0).withSecond(0))
                .status(LessonStatus.COMPLETED)
                .publicLog("Dinâmica de grupo e percepção rítmica.")
                .classGroup(group2)
                .professor(professor)
                .build();

        lessonRepository.saveAll(List.of(lesson1, lesson2, lesson3, lesson4, lesson5, lesson6));

        LocalDate dueToday = LocalDate.now();

        Finance finance1 = Finance.builder()
                .amount(new BigDecimal("150.00"))
                .dueDate(dueToday.plusDays(5))
                .status(PaymentStatus.PENDING)
                .description("Mensalidade Fixa")
                .student(student1)
                .professor(professor)
                .build();

        Finance finance2 = Finance.builder()
                .amount(new BigDecimal("200.00"))
                .dueDate(dueToday.minusDays(2))
                .status(PaymentStatus.OVERDUE)
                .description("Pacote de 4 Aulas (Guitarra)")
                .student(student2)
                .professor(professor)
                .build();

        Finance finance3 = Finance.builder()
                .amount(new BigDecimal("180.00"))
                .dueDate(dueToday)
                .status(PaymentStatus.PAID)
                .description("Pacote de 4 Aulas")
                .student(student4)
                .professor(professor)
                .build();

        Finance finance4 = Finance.builder()
                .amount(new BigDecimal("300.00"))
                .dueDate(dueToday.minusMonths(1))
                .status(PaymentStatus.PAID)
                .description("Pacote de 8 Aulas")
                .student(student3)
                .professor(professor)
                .build();

        Finance financeGroup1 = Finance.builder()
                .amount(new BigDecimal("600.00"))
                .dueDate(dueToday.minusMonths(1).plusDays(10))
                .status(PaymentStatus.PAID)
                .description("Fechamento Turma A - Mês Anterior")
                .classGroup(group1)
                .professor(professor)
                .build();

        Finance financeGroup2 = Finance.builder()
                .amount(new BigDecimal("600.00"))
                .dueDate(dueToday.plusDays(10))
                .status(PaymentStatus.PENDING)
                .description("Fechamento Turma A - Mês Atual")
                .classGroup(group1)
                .professor(professor)
                .build();

        Finance financeGroup3 = Finance.builder()
                .amount(new BigDecimal("450.00"))
                .dueDate(dueToday.minusDays(5))
                .status(PaymentStatus.OVERDUE)
                .description("Taxa de Estúdio - Prática de Conjunto")
                .classGroup(group2)
                .professor(professor)
                .build();

        Finance financeGroup4 = Finance.builder()
                .amount(new BigDecimal("800.00"))
                .dueDate(dueToday.minusMonths(2))
                .status(PaymentStatus.PAID)
                .description("Inscrição Masterclass")
                .classGroup(group3)
                .professor(professor)
                .build();

        financeRepository.saveAll(List.of(finance1, finance2, finance3, finance4, financeGroup1, financeGroup2, financeGroup3, financeGroup4));
    }
}