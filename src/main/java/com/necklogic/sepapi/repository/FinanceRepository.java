package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface FinanceRepository extends JpaRepository<Finance, UUID> {
    @Query("SELECT SUM(f.amount) FROM Finance f WHERE f.professor.id = :professorId AND f.status = :status")
    Optional<BigDecimal> sumAmountByProfessorIdAndStatus(@Param("professorId") UUID professorId, @Param("status") PaymentStatus status);
}