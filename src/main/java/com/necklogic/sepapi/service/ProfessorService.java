package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.AdminUserDTO;
import com.necklogic.sepapi.dto.ChangePasswordRequestDTO;
import com.necklogic.sepapi.dto.ProfessorProfileDTO;
import com.necklogic.sepapi.dto.UpdateProfileRequestDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.ProfessorRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorProfileDTO getProfile(Professor professor) {
        return new ProfessorProfileDTO(
                professor.getName(),
                professor.getEmail(),
                professor.getPhone(),
                professor.isEmailNotifications(),
                professor.isLowCreditAlerts(),
                professor.isMissedClassAlerts(),
                professor.isPaymentAlerts()
        );
    }

    @Transactional
    public ProfessorProfileDTO updateProfile(Professor professor, UpdateProfileRequestDTO dto) {
        professor.setName(dto.name());
        professor.setPhone(dto.phone());
        professor.setEmailNotifications(dto.emailNotifications());
        professor.setLowCreditAlerts(dto.lowCreditAlerts());
        professor.setMissedClassAlerts(dto.missedClassAlerts());
        professor.setPaymentAlerts(dto.paymentAlerts());

        Professor updatedProfessor = professorRepository.save(professor);
        return getProfile(updatedProfessor);
    }

    @Transactional
    public void changePassword(Professor professor, ChangePasswordRequestDTO dto) {
        if (!passwordEncoder.matches(dto.currentPassword(), professor.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
        professor.setPassword(passwordEncoder.encode(dto.newPassword()));
        professorRepository.save(professor);
    }

    @Transactional
    public void deleteAccount(Professor professor) {
        String emailToRelease = professor.getEmail();
        professor.setEmail("deleted_" + System.currentTimeMillis() + "_" + emailToRelease);
        professorRepository.save(professor);

        studentRepository.deleteAllByProfessorId(professor.getId());
        professorRepository.delete(professor);
    }

    public Page<AdminUserDTO> getAllProfessors(Pageable pageable) {
        return professorRepository.findAll(pageable)
                .map(professor -> new AdminUserDTO(
                        professor.getId(),
                        professor.getName(),
                        professor.getEmail(),
                        professor.getCreatedAt()
                ));
    }

    @Transactional
    public void deleteProfessorAsAdmin(UUID id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        deleteAccount(professor);
    }
}