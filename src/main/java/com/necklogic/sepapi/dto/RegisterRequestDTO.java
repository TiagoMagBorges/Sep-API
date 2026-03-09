package com.necklogic.sepapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDTO(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {}