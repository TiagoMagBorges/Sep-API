package com.necklogic.sepapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequestDTO(
        @NotBlank String name,
        String phone,
        @NotNull Boolean emailNotifications,
        @NotNull Boolean lowCreditAlerts,
        @NotNull Boolean missedClassAlerts,
        @NotNull Boolean paymentAlerts
) {}