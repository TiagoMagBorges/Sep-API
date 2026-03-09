package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FinanceRequestDTO(
        @NotNull UUID studentId,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate dueDate,
        @NotNull PaymentStatus status,
        String description
) {}