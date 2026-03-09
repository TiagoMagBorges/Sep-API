package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.BillingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    long countByProfessorIdAndActiveTrue(UUID professorId);

    long countByProfessorIdAndBillingTypeAndCreditBalanceLessThanEqual(UUID id, BillingType type, Integer balance);

    long countByProfessorIdAndBillingTypeAndCreditBalanceGreaterThan(UUID id, BillingType type, Integer balance);

    Page<Student> findAllByProfessorId(UUID professorId, Pageable pageable);

    Optional<Student> findByIdAndProfessorId(UUID id, UUID professorId);

}