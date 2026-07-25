package com.necklogic.sepapi.service;

import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.ProfessorRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock private ProfessorRepository professorRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private PasswordEncoder passwordEncoder; // Necessário para injetar o serviço com sucesso[cite: 1]

    @InjectMocks private ProfessorService professorService;

    @Test
    @DisplayName("Deve renomear o e-mail com timestamp e apagar alunos em cascata ao deletar conta")
    void shouldAnonymizeEmailAndCascadeDeleteStudentsWhenDeletingAccount() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        String emailOriginal = "tiago@sep.com";

        Professor professor = Professor.builder()
                .id(professorId)
                .name("Tiago Borges")
                .email(emailOriginal)
                .password("senha_secreta")
                .build();

        // ==========================================
        // 2. ACT
        // ==========================================
        professorService.deleteAccount(professor);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        String emailModificado = professor.getEmail();

        // valida se o e-mail foi anonimizado para liberar recadastro
        assertTrue(emailModificado.startsWith("deleted_"), "O e-mail deve começar com o prefixo 'deleted_'");
        assertTrue(emailModificado.endsWith("_" + emailOriginal), "O e-mail deve terminar com o endereço original");

        // Verifica se o repositório salvou a alteração do e-mail no banco
        verify(professorRepository, times(1)).save(professor);

        // verifica se todos os alunos vinculados a esse professor foram removidos em cascata
        verify(studentRepository, times(1)).deleteAllByProfessorId(professorId);

        // verifica se o comando de deleção que aciona o Soft Delete do Hibernate foi disparado
        verify(professorRepository, times(1)).delete(professor);
    }
}