package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.FinanceRequestDTO;
import com.necklogic.sepapi.dto.FinanceResponseDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finances")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<FinanceResponseDTO>> listByInterval(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(financeService.listByInterval(professor.getId(), start, end));
    }

    @PostMapping
    public ResponseEntity<FinanceResponseDTO> create(
            @RequestBody @Valid FinanceRequestDTO dto,
            @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.create(dto, professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinanceResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid FinanceRequestDTO dto,
            @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(financeService.updateStatus(id, professor.getId(), dto));
    }
}