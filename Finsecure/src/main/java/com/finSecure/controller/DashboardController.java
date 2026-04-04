package com.finSecure.controller;

import com.finSecure.dto.response.DashboardSummaryResponse;
import com.finSecure.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finsecure/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * VIEWER, ANALYST and ADMIN only.
     * Returns: total income, total expenses, net balance,
     *          category breakdown, monthly trends (last 6 months), recent activity.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    public ResponseEntity<DashboardSummaryResponse> getSummary(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getSummary(auth));
    }
}