package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.LessonRequestDTO;
import com.necklogic.sepapi.dto.LessonResponseDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<List<LessonResponseDTO>> listByInterval(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal Professor professor) {

        return ResponseEntity.ok(lessonService.listByInterval(professor.getId(), start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Professor professor) {

        return ResponseEntity.ok(lessonService.getById(id, professor.getId()));
    }

    @PostMapping
    public ResponseEntity<LessonResponseDTO> create(
            @RequestBody @Valid LessonRequestDTO dto,
            @AuthenticationPrincipal Professor professor) {

        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(dto, professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid LessonRequestDTO dto,
            @AuthenticationPrincipal Professor professor) {

        return ResponseEntity.ok(lessonService.update(id, dto, professor.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Professor professor) {

        lessonService.delete(id, professor.getId());
        return ResponseEntity.noContent().build();
    }
}