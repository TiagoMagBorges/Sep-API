package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    UserDetails findByEmail(String email);
}