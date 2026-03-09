package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.ProfessorAnalyticsDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ProfessorAnalyticsDTO> getStudentAnalytics(
            @PathVariable UUID studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(analyticsService.getStudentAnalytics(
                studentId,
                professor.getId(),
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        ));
    }
}