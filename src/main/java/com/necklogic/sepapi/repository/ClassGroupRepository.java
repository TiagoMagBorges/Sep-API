package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.ClassGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {
    Page<ClassGroup> findAllByProfessorId(UUID professorId, Pageable pageable);
    Optional<ClassGroup> findByIdAndProfessorId(UUID id, UUID professorId);
}