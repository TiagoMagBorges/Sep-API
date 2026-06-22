package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.AdminUserResponseDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfessorRepository professorRepository;
    private final ProfessorService professorService;

    public Page<AdminUserResponseDTO> listUsers(Pageable pageable){

        return professorRepository.findAll(pageable)
                .map(p -> new AdminUserResponseDTO(
                        p.getId(),
                        p.getName(),
                        p.getEmail(),
                        p.getPhone(),
                        p.getRole()
                ));
    }

    public void deleteUser(UUID id){
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        professorService.deleteAccount(professor);
    }

}
