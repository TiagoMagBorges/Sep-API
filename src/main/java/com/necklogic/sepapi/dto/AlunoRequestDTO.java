package com.necklogic.sepapi.dto;

import com.necklogic.sepapi.model.enums.TipoCobranca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlunoRequestDTO(

        @NotBlank String nome,
        boolean ativo,
        @NotNull TipoCobranca tipoCobranca

        ) {
}
