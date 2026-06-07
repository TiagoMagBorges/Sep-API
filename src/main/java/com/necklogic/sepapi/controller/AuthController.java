package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.AuthRequestDTO;
import com.necklogic.sepapi.dto.AuthResponseDTO;
import com.necklogic.sepapi.dto.RegisterRequestDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.ProfessorRepository;
import com.necklogic.sepapi.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final ProfessorRepository professorRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken(((Professor) auth.getPrincipal()).getId());

        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO data) {
        if (this.professorRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.badRequest().build();
        }

        String encryptedPassword = passwordEncoder.encode(data.senha());

        Professor newProfessor = Professor.builder()
                .nome(data.nome())
                .email(data.email())
                .senha(encryptedPassword)
                .build();

        this.professorRepository.save(newProfessor);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentAccount(@AuthenticationPrincipal Professor professor) {
        Professor managedProfessor = professorRepository.findById(professor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        managedProfessor.setAtivo(false);
        managedProfessor.setArquivadoEm(now);
        managedProfessor.setExclusaoFisicaEm(now.plusYears(2));
        professorRepository.save(managedProfessor);

        return ResponseEntity.noContent().build();
    }
}
