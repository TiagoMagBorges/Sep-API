package com.necklogic.sepapi.controller;

import com.necklogic.sepapi.dto.DashboardSummaryDTO;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@AuthenticationPrincipal Professor professor) {
        return ResponseEntity.ok(dashboardService.getSummary(professor.getId()));
    }
}