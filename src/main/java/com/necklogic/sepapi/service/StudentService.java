package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.StudentMetricsDTO;
import com.necklogic.sepapi.dto.StudentRequestDTO;
import com.necklogic.sepapi.dto.StudentResponseDTO;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.enums.BillingType;
import com.necklogic.sepapi.repository.StudentRepository;
import com.necklogic.sepapi.strategy.BillingStrategy;
import com.necklogic.sepapi.strategy.MonthlyStrategy;
import com.necklogic.sepapi.strategy.CreditPackageStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final MonthlyStrategy monthlyStrategy;
    private final CreditPackageStrategy creditPackageStrategy;

    public Page<StudentResponseDTO> list(UUID professorId, Pageable pageable){
        return studentRepository.findAllByProfessorId(professorId, pageable)
                .map(this::mapToDTO);
    }

    public StudentResponseDTO getById(UUID id, UUID professorId){
        Student student = studentRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapToDTO(student);
    }

    public StudentResponseDTO create(StudentRequestDTO dto, Professor professor){
        Student student = Student.builder()
                .name(dto.name())
                .subject(dto.subject())
                .active(dto.active())
                .billingType(dto.billingType())
                .creditBalance(dto.creditBalance() != null ? dto.creditBalance() : 0)
                .professor(professor)
                .build();

        applyStrategy(student);

        return mapToDTO(studentRepository.save(student));
    }

    public StudentResponseDTO update(UUID id, StudentRequestDTO dto, UUID professorId){
        Student student = studentRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        student.setName(dto.name());
        student.setSubject(dto.subject());
        student.setActive(dto.active());
        student.setBillingType(dto.billingType());
        student.setCreditBalance(dto.creditBalance() != null ? dto.creditBalance() : 0);

        applyStrategy(student);

        return mapToDTO(studentRepository.save(student));
    }

    public void delete(UUID id, UUID professorId){
        Student student = studentRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        studentRepository.delete(student);
    }

    public StudentMetricsDTO getMetrics(UUID professorId) {
        long active = studentRepository.countByProfessorIdAndActiveTrue(professorId);
        long lowCredits = studentRepository.countByProfessorIdAndBillingTypeAndCreditBalanceLessThanEqual(professorId, BillingType.CREDIT_PACKAGE, 3);
        long upToDate = studentRepository.countByProfessorIdAndBillingTypeAndCreditBalanceGreaterThan(professorId, BillingType.CREDIT_PACKAGE, 3);

        return new StudentMetricsDTO(active, lowCredits, upToDate);
    }

    private void applyStrategy(Student student){
        BillingStrategy strategy = switch(student.getBillingType()){
            case MONTHLY -> monthlyStrategy;
            case CREDIT_PACKAGE -> creditPackageStrategy;
        };

        strategy.process(student);
    }

    private StudentResponseDTO mapToDTO(Student student){
        return new StudentResponseDTO(student.getId(), student.getName(),
                                    student.getSubject(), student.isActive(),
                                    student.getBillingType(), student.getCreditBalance());
    }
}