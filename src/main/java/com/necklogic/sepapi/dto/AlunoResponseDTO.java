package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.TipoCobranca;

import java.util.UUID;

public record AlunoResponseDTO(

        UUID id,
        String nome,
        boolean ativo,
        TipoCobranca tipoCobranca

) {
}
