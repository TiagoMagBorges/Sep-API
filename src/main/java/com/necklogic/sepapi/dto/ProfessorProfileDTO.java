package com.necklogic.sepapi.dto;

public record ProfessorProfileDTO(
        String name,
        String email,
        String phone,
        boolean emailNotifications,
        boolean lowCreditAlerts,
        boolean missedClassAlerts,
        boolean paymentAlerts
) {}