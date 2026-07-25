package com.necklogic.sepapi.repository;

import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest // sobe um banco H2 em memória exclusivo para testar queries e repositórios
class ProfessorRepositoryTest {

    @Autowired private ProfessorRepository professorRepository;

    // o TestEntityManager é um utilitário do Spring para manipularmos o banco diretamente em testes
    @Autowired private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve apagar FISICAMENTE (Hard Delete) apenas professores com deleted_at anterior a 2 anos")
    void shouldHardDeleteOldSoftDeletedProfessors() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        // 1 - professor ativo (deletedAt é null) - não pode ser tocado
        Professor profAtivo = Professor.builder()
                .name("Professor Ativo").email("ativo@sep.com").password("123").role(UserRole.PROFESSOR)
                .build();

        // 2 - professor Deletado recentemente (há 6 meses) - deve continuar retido por segurança
        Professor profDeletadoRecente = Professor.builder()
                .name("Deletado Recente").email("recente@sep.com").password("123").role(UserRole.PROFESSOR)
                .deletedAt(LocalDateTime.now().minusMonths(6))
                .build();

        // 3 - professor Deletado antigo (há 3 anos) - ALVO DO HARD DELETE
        Professor profDeletadoAntigo = Professor.builder()
                .name("Deletado Antigo").email("antigo@sep.com").password("123").role(UserRole.PROFESSOR)
                .deletedAt(LocalDateTime.now().minusYears(3))
                .build();

        // persiste direto via EntityManager para forçar a gravação no banco em memória
        UUID idAtivo = (UUID) entityManager.persistAndGetId(profAtivo);
        UUID idRecente = (UUID) entityManager.persistAndGetId(profDeletadoRecente);
        UUID idAntigo = (UUID) entityManager.persistAndGetId(profDeletadoAntigo);

        entityManager.flush(); // força a escrita no disco/memória do H2 agora

        // ==========================================
        // 2. ACT
        // ==========================================
        // simula a data de corte da rotina, exatamente 2 anos atrás
        LocalDateTime dataCorte = LocalDateTime.now().minusYears(2);
        professorRepository.hardDeleteOldProfessors(dataCorte);

        entityManager.flush();
        entityManager.clear(); // limpa o cache do Hibernate para obrigar a leitura real do banco

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // valida a contagem física de linhas direto no banco usando nosso método auxiliar nativo
        assertEquals(1L, countPhysicalRowsById(idAtivo), "O professor ativo deve continuar intacto no banco");
        assertEquals(1L, countPhysicalRowsById(idRecente), "O professor deletado há apenas 6 meses NÃO pode ser apagado ainda");
        assertEquals(0L, countPhysicalRowsById(idAntigo), "O professor deletado há 3 anos DEVE ser apagado fisicamente (Hard Delete)");
    }

    // método auxiliar que executa SQL puro no H2 para ignorar a anotação @SQLRestriction do Hibernate
    private long countPhysicalRowsById(UUID id) {
        Number count = (Number) entityManager.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM professors WHERE id = :id")
                .setParameter("id", id)
                .getSingleResult();
        return count.longValue();
    }

    @Test
    @DisplayName("Deve ignorar professores com Soft Delete (deleted_at preenchido) nas buscas padrão e no login")
    void shouldIgnoreSoftDeletedProfessorsInStandardQueries() {
        // ==========================================
        // 1. ARRANGE
        // ==========================================
        // 1 - professor ativo, deletedAt continua null
        Professor profAtivo = Professor.builder()
                .name("Professor Ativo").email("ativo@sep.com").password("123").role(UserRole.PROFESSOR)
                .build();

        // 2 - professor deletado, simulando que passou pelo Soft Delete e tem data preenchida
        Professor profDeletado = Professor.builder()
                .name("Professor Deletado").email("inativo@sep.com").password("123").role(UserRole.PROFESSOR)
                .deletedAt(LocalDateTime.now())
                .build();

        // persiste ambos no banco de dados H2
        entityManager.persist(profAtivo);
        entityManager.persist(profDeletado);
        entityManager.flush();

        // limpa o cache de 1º nível da memória do Hibernate
        // sem isso, ao chamar o findByEmail, o Hibernate pegaria o objeto direto da memória RAM
        // sem ir no banco H2 executar o SELECT com a restrição do @SQLRestriction
        entityManager.clear();

        // ==========================================
        // 2. ACT
        // ==========================================
        var listaGeral = professorRepository.findAll();
        var buscaLoginInativo = professorRepository.findByEmail("inativo@sep.com");
        var buscaLoginAtivo = professorRepository.findByEmail("ativo@sep.com");

        // ==========================================
        // 3. ASSERT
        // ==========================================
        // a listagem geral (findAll) deve retornar tamanho 1, escondendo completamente o usuário deletado
        assertEquals(1, listaGeral.size(), "O findAll deve trazer apenas 1 professor (o ativo)");
        assertEquals("ativo@sep.com", listaGeral.get(0).getEmail(), "O único professor retornado deve ser o ativo");

        // a busca por e-mail (usada no login) deve retornar null para o e-mail do inativo
        assertNull(buscaLoginInativo, "O findByEmail NÃO deve encontrar usuários com deleted_at preenchido");

        // a busca por e-mail para o usuário ativo deve funcionar normalmente
        assertNotNull(buscaLoginAtivo, "O findByEmail deve encontrar o usuário ativo normalmente");
    }
}