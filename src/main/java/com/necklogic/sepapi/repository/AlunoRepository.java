package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    long countByProfessorIdAndAtivoTrue(UUID professorId);
}