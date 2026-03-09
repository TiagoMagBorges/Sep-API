package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.ChangePasswordRequestDTO;
import com.necklogic.sepapi.dto.ProfessorProfileDTO;
import com.necklogic.sepapi.dto.UpdateProfileRequestDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.ProfessorRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final FinanceRepository financeRepository;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        professor.setPassword(passwordEncoder.encode(dto.newPassword()));
        professorRepository.save(professor);
    }

    @Transactional
    public void deleteAccount(Professor professor) {
        financeRepository.deleteAllByProfessorId(professor.getId());
        lessonRepository.deleteAllByProfessorId(professor.getId());
        studentRepository.deleteAllByProfessorId(professor.getId());
        professorRepository.delete(professor);
    }
}