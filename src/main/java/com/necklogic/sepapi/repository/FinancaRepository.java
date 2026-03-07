package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Financa;
import com.necklogic.sepapi.model.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface FinancaRepository extends JpaRepository<Financa, UUID> {
    @Query("SELECT SUM(f.valor) FROM Financa f WHERE f.professor.id = :professorId AND f.status = :status")
    Optional<BigDecimal> sumValorByProfessorIdAndStatus(@Param("professorId") UUID professorId, @Param("status") StatusPagamento status);
}