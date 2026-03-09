package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.FinanceRequestDTO;
import com.necklogic.sepapi.dto.FinanceResponseDTO;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceRepository financeRepository;
    private final StudentRepository studentRepository;

    public List<FinanceResponseDTO> listByInterval(UUID professorId, LocalDate start, LocalDate end) {
        return financeRepository.findAllByProfessorIdAndDueDateBetweenOrderByDueDateDesc(professorId, start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FinanceResponseDTO create(FinanceRequestDTO dto, Professor professor) {
        Student student = studentRepository.findByIdAndProfessorId(dto.studentId(), professor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        Finance finance = Finance.builder()
                .amount(dto.amount())
                .dueDate(dto.dueDate())
                .status(dto.status())
                .description(dto.description())
                .student(student)
                .professor(professor)
                .build();

        return mapToDTO(financeRepository.save(finance));
    }

    public FinanceResponseDTO updateStatus(UUID id, UUID professorId, FinanceRequestDTO dto) {
        Finance finance = financeRepository.findById(id)
                .filter(f -> f.getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        finance.setStatus(dto.status());
        return mapToDTO(financeRepository.save(finance));
    }

    private FinanceResponseDTO mapToDTO(Finance finance) {
        return new FinanceResponseDTO(
                finance.getId(),
                finance.getStudent().getId(),
                finance.getStudent().getName(),
                finance.getAmount(),
                finance.getDueDate(),
                finance.getStatus(),
                finance.getDescription()
        );
    }
}