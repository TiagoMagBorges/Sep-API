package com.necklogic.sepapi.dto;

public record StudentMetricsDTO(
        long active,
        long lowCredits,
        long upToDate
) {}