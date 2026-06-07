package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Aluno;
import com.necklogic.sepapi.model.enums.TipoCobranca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

    long countByProfessorIdAndAtivoTrueAndArquivadoEmIsNull(UUID professorId);

    long countByProfessorIdAndTipoCobrancaAndSaldoCreditosLessThanEqualAndArquivadoEmIsNull(UUID id, TipoCobranca tipo, Integer saldo);

    long countByProfessorIdAndTipoCobrancaAndSaldoCreditosGreaterThanAndArquivadoEmIsNull(UUID id, TipoCobranca tipo, Integer saldo);

    Page<Aluno> findAllByProfessorIdAndArquivadoEmIsNull(UUID professorId, Pageable pageable);

    Optional<Aluno> findByIdAndProfessorIdAndArquivadoEmIsNull(UUID id, UUID professorId);

    List<Aluno> findAllByClassGroupIdAndProfessorIdAndArquivadoEmIsNullOrderByNomeAsc(UUID classGroupId, UUID professorId);


}
