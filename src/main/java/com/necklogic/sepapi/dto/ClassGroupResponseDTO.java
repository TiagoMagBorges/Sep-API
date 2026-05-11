package com.necklogic.sepapi.dto;

import java.util.List;
import java.util.UUID;

public record ClassGroupResponseDTO(
        UUID id,
        String name,
        List<StudentResponseDTO> students
) {}