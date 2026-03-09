package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.BillingType;

import java.util.UUID;

public record StudentResponseDTO(
        UUID id,
        String name,
        String subject,
        boolean active,
        BillingType billingType,
        Integer creditBalance
) {}