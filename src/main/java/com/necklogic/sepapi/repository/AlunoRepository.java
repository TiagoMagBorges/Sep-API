package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    long countByProfessorIdAndAtivoTrue(UUID professorId);

    Page<Aluno> findAllByProfessorId(UUID professorId, Pageable pageable);

    Optional<Aluno> findByIdAndProfessorId(UUID id, UUID professorId);

}