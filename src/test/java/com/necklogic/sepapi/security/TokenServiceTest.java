package com.necklogic.sepapi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // injeta manualmente uma chave secreta na variável privada @Value do serviço
        ReflectionTestUtils.setField(tokenService, "secret", "minha-chave-secreta-de-teste");
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido contendo o UUID no Subject e a Role correta")
    void shouldGenerateTokenSuccessfully() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();

        Professor professor = Professor.builder()
                .id(professorId)
                .email("tiago@sep.com")
                .role(UserRole.ADMIN) // Testando com perfil de Administrador [source: 1]
                .build();

        // ==========================================
        // 2. ACT
        // ==========================================
        String token = tokenService.generateToken(professor);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // garante que uma string de token real foi criada
        assertNotNull(token, "O token gerado não pode ser nulo");
        assertFalse(token.isEmpty(), "O token gerado não pode estar vazio");

        // Decodifica o token gerado para inspecionar os dados criptografados lá dentro
        DecodedJWT decodedJWT = JWT.decode(token);

        // Valida se o emissor (Issuer) gravado é "sep-api"
        assertEquals("sep-api", decodedJWT.getIssuer(), "O Issuer deve ser 'sep-api'");

        // valida se o UUID do professor foi gravado corretamente no Subject
        assertEquals(professorId.toString(), decodedJWT.getSubject(), "O Subject deve conter o UUID do professor");

        // valida se a permissão foi gravada na claim 'role'
        assertEquals("ADMIN", decodedJWT.getClaim("role").asString(), "A claim 'role' deve conter o perfil ADMIN");

        // garante que o token possui uma data de expiração definida
        assertNotNull(decodedJWT.getExpiresAt(), "O token precisa ter uma data de expiração");
    }

    @Test
    @DisplayName("Deve validar um token autenticado e retornar o UUID do professor (Subject)")
    void shouldValidateTokenAndReturnSubjectSuccessfully() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        UUID professorId = UUID.randomUUID();
        Professor professor = Professor.builder()
                .id(professorId)
                .email("tiago@sep.com")
                .role(UserRole.PROFESSOR)
                .build();

        // gera um token real utilizando o próprio serviço
        String tokenValido = tokenService.generateToken(professor);

        // ==========================================
        // 2. ACT
        // ==========================================
        String subjectRetornado = tokenService.validateToken(tokenValido);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // o método deve descriptografar o token e nos devolver exatamente a String do UUID original
        assertNotNull(subjectRetornado, "O Subject retornado não deveria ser nulo para um token válido");
        assertEquals(professorId.toString(), subjectRetornado, "O Subject retornado deve ser idêntico ao UUID do professor");
    }

    @Test
    @DisplayName("Deve retornar NULL graciosamente ao tentar validar um token inválido, adulterado ou vazio")
    void shouldReturnNullWhenValidatingInvalidToken() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        String tokenLixo = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload.signature";
        String tokenVazio = "";

        // ==========================================
        // 2. ACT
        // ==========================================
        String resultadoLixo = tokenService.validateToken(tokenLixo);
        String resultadoVazio = tokenService.validateToken(tokenVazio);

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // o serviço é obrigado a capturar a exceção criptográfica e devolver null tranquilamente
        assertNull(resultadoLixo, "O resultado deve ser null ao validar uma string de token corrompida");
        assertNull(resultadoVazio, "O resultado deve ser null ao validar um token vazio");
    }
}