package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.BillingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentRequestDTO(
        @NotBlank String name,
        @NotBlank String subject,
        boolean active,
        @NotNull BillingType billingType,
        Integer creditBalance
) {}