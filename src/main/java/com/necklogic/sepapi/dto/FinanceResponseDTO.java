package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinanceResponseDTO(
        UUID id,
        UUID studentId,
        String studentName,
        BigDecimal amount,
        LocalDate dueDate,
        PaymentStatus status,
        String description
) {}