package com.necklogic.sepapi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserDTO(
        UUID id,
        String name,
        String email,
        LocalDateTime createdAt
) {}