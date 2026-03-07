package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.DashboardSummaryDTO;
import com.necklogic.sepapi.model.enums.StatusAula;
import com.necklogic.sepapi.model.enums.StatusPagamento;
import com.necklogic.sepapi.repository.AlunoRepository;
import com.necklogic.sepapi.repository.AulaRepository;
import com.necklogic.sepapi.repository.FinancaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;
    private final FinancaRepository financaRepository;

    public DashboardSummaryDTO getSummary(UUID professorId) {
        long totalAlunos = alunoRepository.countByProfessorIdAndAtivoTrue(professorId);

        LocalDateTime startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
        long aulasNaSemana = aulaRepository.countByProfessorIdAndDataHoraBetweenAndStatus(professorId, startOfWeek, endOfWeek, StatusAula.AGENDADA);

        BigDecimal pagamentosPendentes = financaRepository.sumValorByProfessorIdAndStatus(professorId, StatusPagamento.PENDENTE)
                .orElse(BigDecimal.ZERO);

        return new DashboardSummaryDTO(totalAlunos, aulasNaSemana, pagamentosPendentes);
    }
}