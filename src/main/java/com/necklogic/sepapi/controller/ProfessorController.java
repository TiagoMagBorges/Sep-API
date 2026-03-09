package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.ChangePasswordRequestDTO;
import com.necklogic.sepapi.dto.ProfessorProfileDTO;
import com.necklogic.sepapi.dto.UpdateProfileRequestDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping("/me")
    public ResponseEntity<ProfessorProfileDTO> getProfile(@AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(professorService.getProfile(professor));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfessorProfileDTO> updateProfile(
            @AuthenticationPrincipal Professor professor,
            @RequestBody @Valid UpdateProfileRequestDTO dto) {
        return ResponseEntity.ok(professorService.updateProfile(professor, dto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Professor professor,
            @RequestBody @Valid ChangePasswordRequestDTO dto) {
        professorService.changePassword(professor, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Professor professor) {
        professorService.deleteAccount(professor);
        return ResponseEntity.noContent().build();
    }
}