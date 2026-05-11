package com.necklogic.sepapi.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record ClassGroupRequestDTO(
        @NotBlank String name,
        List<UUID> studentIds
) {}