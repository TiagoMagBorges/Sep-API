package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.ClassGroupRequestDTO;
import com.necklogic.sepapi.dto.ClassGroupResponseDTO;
import com.necklogic.sepapi.dto.StudentResponseDTO;
import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public Page<ClassGroupResponseDTO> list(UUID professorId, Pageable pageable) {
        return classGroupRepository.findAllByProfessorId(professorId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public ClassGroupResponseDTO getById(UUID id, UUID professorId) {
        ClassGroup classGroup = classGroupRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new EntityNotFoundException("ClassGroup not found"));
        return mapToResponseDTO(classGroup);
    }

    @Transactional
    public ClassGroupResponseDTO create(ClassGroupRequestDTO dto, Professor professor) {
        ClassGroup classGroup = ClassGroup.builder()
                .name(dto.name())
                .professor(professor)
                .build();

        ClassGroup savedClassGroup = classGroupRepository.save(classGroup);

        if (dto.studentIds() != null && !dto.studentIds().isEmpty()) {
            List<Student> students = studentRepository.findAllById(dto.studentIds());
            students.stream()
                    .filter(student -> student.getProfessor().getId().equals(professor.getId()))
                    .forEach(student -> student.setClassGroup(savedClassGroup));

            savedClassGroup.setStudents(students);
        }

        return mapToResponseDTO(savedClassGroup);
    }

    @Transactional
    public ClassGroupResponseDTO update(UUID id, ClassGroupRequestDTO dto, UUID professorId) {
        ClassGroup classGroup = classGroupRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new EntityNotFoundException("ClassGroup not found"));

        classGroup.setName(dto.name());

        if (dto.studentIds() != null) {
            List<Student> currentStudents = studentRepository.findByClassGroup(classGroup);
            currentStudents.forEach(s -> s.setClassGroup(null));

            List<Student> newStudents = studentRepository.findAllById(dto.studentIds());
            newStudents.stream()
                    .filter(s -> s.getProfessor().getId().equals(professorId))
                    .forEach(s -> s.setClassGroup(classGroup));
            classGroup.setStudents(newStudents);
        }

        return mapToResponseDTO(classGroupRepository.save(classGroup));
    }

    @Transactional
    public void delete(UUID id, UUID professorId) {
        ClassGroup classGroup = classGroupRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new EntityNotFoundException("ClassGroup not found"));

        List<Student> students = studentRepository.findByClassGroup(classGroup);
        students.forEach(s -> s.setClassGroup(null));

        classGroupRepository.delete(classGroup);
    }

    private ClassGroupResponseDTO mapToResponseDTO(ClassGroup classGroup) {
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

        return new ClassGroupResponseDTO(classGroup.getId(), classGroup.getName(), studentDTOs);
    }
}