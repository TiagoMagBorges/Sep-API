package com.necklogic.sepapi.dto;

import java.math.BigDecimal;

public record FinanceAnalyticsDTO(

   BigDecimal totalPaid,
   BigDecimal totalPending,
   BigDecimal totalOverdue

) {}
