package com.necklogic.sepapi.dto;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
        long totalActiveStudents,
        long lessonsInWeek,
        BigDecimal pendingPayments
) {}