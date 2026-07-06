package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.AdminUserDTO;
import com.necklogic.sepapi.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final ProfessorService professorService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(professorService.getAllProfessors(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        professorService.deleteProfessorAsAdmin(id);
        return ResponseEntity.noContent().build();
    }
}