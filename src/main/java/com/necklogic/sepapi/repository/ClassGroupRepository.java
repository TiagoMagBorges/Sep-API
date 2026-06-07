package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {

    Optional<ClassGroup> findByIdAndProfessorId(UUID id, UUID professorId);
}
