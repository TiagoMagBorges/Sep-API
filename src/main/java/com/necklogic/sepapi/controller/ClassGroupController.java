package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.ClassGroupRequestDTO;
import com.necklogic.sepapi.dto.ClassGroupResponseDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.ClassGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/class-groups")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @GetMapping
    public ResponseEntity<Page<ClassGroupResponseDTO>> list(@AuthenticationPrincipal Professor professor,
                                                            Pageable pageable) {
        return ResponseEntity.ok(classGroupService.list(professor.getId(), pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<ClassGroupResponseDTO> getById(@PathVariable UUID id,
                                                         @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(classGroupService.getById(id, professor.getId()));
    }

    @PostMapping
    public ResponseEntity<ClassGroupResponseDTO> create(@RequestBody @Valid ClassGroupRequestDTO dto,
                                                        @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classGroupService.create(dto, professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody @Valid ClassGroupRequestDTO dto,
                                                        @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(classGroupService.update(id, dto, professor.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal Professor professor) {
        classGroupService.delete(id, professor.getId());
        return ResponseEntity.noContent().build();
    }
}