package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    UserDetails findByEmail(String email);

    @Modifying
    @Query(value = "DELETE FROM professors WHERE deleted_at <= :thresholdDate", nativeQuery = true)
    void hardDeleteOldProfessors(@Param("thresholdDate") LocalDateTime thresholdDate);

}