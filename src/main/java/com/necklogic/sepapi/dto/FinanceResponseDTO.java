package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinanceResponseDTO(
        UUID id,
        UUID studentId,
        String studentName,
        UUID classGroupId,
        String classGroupName,
        BigDecimal amount,
        LocalDate dueDate,
        PaymentStatus status,
        String description
) {
}