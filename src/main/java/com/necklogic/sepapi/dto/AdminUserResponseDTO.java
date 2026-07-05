package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.UserRole;

import java.util.UUID;

public record AdminUserResponseDTO(

        UUID id,
        String name,
        String email,
        String phone,
        UserRole role

) {}
