package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.AlunoRequestDTO;
import com.necklogic.sepapi.dto.AlunoResponseDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.AlunoService;
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
@RequestMapping("api/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @GetMapping
    public ResponseEntity<Page<AlunoResponseDTO>> listar(@AuthenticationPrincipal Professor professor,
                                                         Pageable pageable){

        return ResponseEntity.ok(alunoService.listar(professor.getId(), pageable));
    }

    @GetMapping("{id}")
    public ResponseEntity<AlunoResponseDTO> buscarPorId(@PathVariable UUID id,
                                                        @AuthenticationPrincipal Professor professor){

        return ResponseEntity.ok(alunoService.buscarPorId(id, professor.getId()));
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDTO> criar(@RequestBody @Valid AlunoRequestDTO dto,
                                                  @AuthenticationPrincipal Professor professor){

        return ResponseEntity.status(HttpStatus.CREATED).body(alunoService.criar(dto, professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlunoResponseDTO> atualizar(@PathVariable UUID id,
                                                      @RequestBody @Valid AlunoRequestDTO dto,
                                                      @AuthenticationPrincipal Professor professor){

        return ResponseEntity.ok(alunoService.atualizar(id, dto, professor.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id, @AuthenticationPrincipal Professor professor){
        alunoService.deletar(id, professor.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metricas")
    public ResponseEntity<com.necklogic.sepapi.dto.AlunoMetricasDTO> getMetrics(@AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(alunoService.obterMetricas(professor.getId()));
    }

}
