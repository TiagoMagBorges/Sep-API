package com.necklogic.sepapi.config;

import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.model.enums.BillingType;
import com.necklogic.sepapi.repository.StudentRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final FinanceRepository financeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (professorRepository.count() > 0) {
            return;
        }

        Professor professor = Professor.builder()
                .name("Tiago Borges")
                .email("tiagomagborges@gmail.com")
                .password(passwordEncoder.encode("senha_segura_123"))
                .build();

        professorRepository.save(professor);

        Student student1 = Student.builder().name("Carlos Silva").subject("Matemática").active(true).billingType(BillingType.MONTHLY).creditBalance(0).professor(professor).build();
        Student student2 = Student.builder().name("Ana Beatriz").subject("Inglês").active(true).billingType(BillingType.CREDIT_PACKAGE).creditBalance(4).professor(professor).build();
        Student student3 = Student.builder().name("Marcos Paulo").subject("Física").active(true).billingType(BillingType.MONTHLY).creditBalance(0).professor(professor).build();
        Student student4 = Student.builder().name("Julia Santos").subject("Química").active(false).billingType(BillingType.CREDIT_PACKAGE).creditBalance(2).professor(professor).build();

        studentRepository.saveAll(List.of(student1, student2, student3, student4));

        LocalDateTime today = LocalDateTime.now();

        Lesson lesson1 = Lesson.builder().dateTime(today.plusDays(1)).status(LessonStatus.SCHEDULED).student(student1).professor(professor).build();
        Lesson lesson2 = Lesson.builder().dateTime(today.plusDays(2)).status(LessonStatus.SCHEDULED).student(student2).professor(professor).build();
        Lesson lesson3 = Lesson.builder().dateTime(today.minusDays(1)).status(LessonStatus.COMPLETED).student(student3).professor(professor).build();

        lessonRepository.saveAll(List.of(lesson1, lesson2, lesson3));

        LocalDate dueToday = LocalDate.now();

        Finance finance1 = Finance.builder().amount(new BigDecimal("150.00")).dueDate(dueToday.plusDays(5)).status(PaymentStatus.PENDING).student(student1).professor(professor).build();
        Finance finance2 = Finance.builder().amount(new BigDecimal("200.00")).dueDate(dueToday.minusDays(2)).status(PaymentStatus.OVERDUE).student(student2).professor(professor).build();
        Finance finance3 = Finance.builder().amount(new BigDecimal("180.00")).dueDate(dueToday).status(PaymentStatus.PAID).student(student3).professor(professor).build();

        financeRepository.saveAll(List.of(finance1, finance2, finance3));
    }
}