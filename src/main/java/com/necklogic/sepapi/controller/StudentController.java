package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.StudentRequestDTO;
import com.necklogic.sepapi.dto.StudentResponseDTO;
import com.necklogic.sepapi.dto.StudentMetricsDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.StudentService;
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
@RequestMapping("api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<StudentResponseDTO>> list(@AuthenticationPrincipal Professor professor,
                                                         Pageable pageable){

        return ResponseEntity.ok(studentService.list(professor.getId(), pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<StudentResponseDTO> getById(@PathVariable UUID id,
                                                      @AuthenticationPrincipal Professor professor){

        return ResponseEntity.ok(studentService.getById(id, professor.getId()));
    }

    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(@RequestBody @Valid StudentRequestDTO dto,
                                                     @AuthenticationPrincipal Professor professor){

        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(dto, professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> update(@PathVariable UUID id,
                                                     @RequestBody @Valid StudentRequestDTO dto,
                                                     @AuthenticationPrincipal Professor professor){

        return ResponseEntity.ok(studentService.update(id, dto, professor.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Professor professor){
        studentService.delete(id, professor.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<StudentMetricsDTO> getMetrics(@AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(studentService.getMetrics(professor.getId()));
    }

}