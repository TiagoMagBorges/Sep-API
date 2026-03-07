package com.necklogic.sepapi.dto;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
        long totalAlunosAtivos,
        long aulasNaSemana,
        BigDecimal pagamentosPendentes
) {}