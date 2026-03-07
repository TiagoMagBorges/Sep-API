package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Aula;
import com.necklogic.sepapi.model.enums.StatusAula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AulaRepository extends JpaRepository<Aula, UUID> {
    long countByProfessorIdAndDataHoraBetweenAndStatus(UUID professorId, LocalDateTime start, LocalDateTime end, StatusAula status);
}