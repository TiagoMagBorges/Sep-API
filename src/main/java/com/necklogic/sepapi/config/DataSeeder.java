package com.necklogic.sepapi.config;

import com.necklogic.sepapi.model.Aluno;
import com.necklogic.sepapi.model.Aula;
import com.necklogic.sepapi.model.Financa;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.model.enums.StatusAula;
import com.necklogic.sepapi.model.enums.StatusPagamento;
import com.necklogic.sepapi.model.enums.TipoCobranca;
import com.necklogic.sepapi.repository.AlunoRepository;
import com.necklogic.sepapi.repository.AulaRepository;
import com.necklogic.sepapi.repository.FinancaRepository;
import com.necklogic.sepapi.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;
    private final FinancaRepository financaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (professorRepository.count() > 0) {
            return;
        }

        Professor professor = Professor.builder()
                .nome("Tiago Borges")
                .email("tiagomagborges@gmail.com")
                .senha(passwordEncoder.encode("senha_segura_123"))
                .build();

        professorRepository.save(professor);

        Aluno aluno1 = Aluno.builder().nome("Carlos Silva").ativo(true).tipoCobranca(TipoCobranca.MENSALIDADE).professor(professor).build();
        Aluno aluno2 = Aluno.builder().nome("Ana Beatriz").ativo(true).tipoCobranca(TipoCobranca.PACOTE_CREDITOS).professor(professor).build();
        Aluno aluno3 = Aluno.builder().nome("Marcos Paulo").ativo(true).tipoCobranca(TipoCobranca.MENSALIDADE).professor(professor).build();
        Aluno aluno4 = Aluno.builder().nome("Julia Santos").ativo(false).tipoCobranca(TipoCobranca.PACOTE_CREDITOS).professor(professor).build();

        alunoRepository.saveAll(List.of(aluno1, aluno2, aluno3, aluno4));

        LocalDateTime hoje = LocalDateTime.now();

        Aula aula1 = Aula.builder().dataHora(hoje.plusDays(1)).status(StatusAula.AGENDADA).aluno(aluno1).professor(professor).build();
        Aula aula2 = Aula.builder().dataHora(hoje.plusDays(2)).status(StatusAula.AGENDADA).aluno(aluno2).professor(professor).build();
        Aula aula3 = Aula.builder().dataHora(hoje.minusDays(1)).status(StatusAula.REALIZADA).aluno(aluno3).professor(professor).build();

        aulaRepository.saveAll(List.of(aula1, aula2, aula3));

        LocalDate vencimentoHoje = LocalDate.now();

        Financa financa1 = Financa.builder().valor(new BigDecimal("150.00")).dataVencimento(vencimentoHoje.plusDays(5)).status(StatusPagamento.PENDENTE).aluno(aluno1).professor(professor).build();
        Financa financa2 = Financa.builder().valor(new BigDecimal("200.00")).dataVencimento(vencimentoHoje.minusDays(2)).status(StatusPagamento.ATRASADO).aluno(aluno2).professor(professor).build();
        Financa financa3 = Financa.builder().valor(new BigDecimal("180.00")).dataVencimento(vencimentoHoje).status(StatusPagamento.PAGO).aluno(aluno3).professor(professor).build();

        financaRepository.saveAll(List.of(financa1, financa2, financa3));
    }
}