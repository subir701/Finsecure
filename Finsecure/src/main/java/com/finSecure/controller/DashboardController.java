package com.finSecure.controller;

import com.finSecure.dto.response.*;
import com.finSecure.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/finsecure/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and summary endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/my/totals")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "My totals — income, expenses, net balance",
            description = "Returns financial totals scoped to the authenticated user's own records."
    )
    public ResponseEntity<TotalsResponse> getMyTotals(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getTotalsByUserId(auth));
    }

    @GetMapping("/my/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "My category breakdown — income and expenses by category",
            description = "Returns category-wise totals scoped to the authenticated user."
    )
    public ResponseEntity<CategorySummaryResponse> getMyCategorySummary(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getCategorySummaryByUserId(auth));
    }

    @GetMapping("/my/trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "My monthly trends — last 6 months",
            description = "Returns month-by-month income vs expense trends for the authenticated user."
    )
    public ResponseEntity<TrendResponse> getMyTrends(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrendsByUserId(auth));
    }

    @GetMapping("/my/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "My recent activity — last 10 transactions ",
            description = "Returns the 10 most recent records for the authenticated user."
    )
    public ResponseEntity<List<RecordResponse>> getMyRecentActivity(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getRecentActivityByUserId(auth));
    }

    @GetMapping("/global/totals")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "Global totals — platform-wide income, expenses, net (ANALYST, ADMIN)",
            description = "Aggregates all records across all users. Allows Analyst to view the system's financial health."
    )
    public ResponseEntity<TotalsResponse> getGlobalTotals() {
        return ResponseEntity.ok(dashboardService.getGlobalTotals());
    }

    @GetMapping("/global/categories")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "Global category breakdown — across all users (ANALYST, ADMIN)",
            description = "Category-wise income and expense totals across the entire platform."
    )
    public ResponseEntity<CategorySummaryResponse> getGlobalCategorySummary() {
        return ResponseEntity.ok(dashboardService.getGlobalCategorySummary());
    }

    @GetMapping("/global/trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "Global monthly trends — configurable window (ANALYST, ADMIN)",
            description = "Platform-wide month-by-month trends. Use ?months=6 (default) to control the lookback window."
    )
    public ResponseEntity<TrendResponse> getGlobalTrends(
            @RequestParam(defaultValue = "6") Integer months) {
        return ResponseEntity.ok(dashboardService.getGlobalMonthlyTrends(months));
    }

    @GetMapping("/global/recent")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
            summary = "Global recent activity — last 10 transactions across all users (ANALYST, ADMIN)",
            description = "Returns the 10 most recent records platform-wide, regardless of which user created them."
    )
    public ResponseEntity<List<RecordResponse>> getGlobalRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }

    @GetMapping("/global/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Global full summary — complete platform-wide overview (ADMIN only)",
            description = "One-shot endpoint combining totals, categories, trends and recent activity across all users."
    )
    public ResponseEntity<DashboardSummaryResponse> getGlobalSummary(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getGlobalSummary(auth));
    }

    @GetMapping("/global/basic/summary")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST','VIEWER')")
    @Operation(
            summary = "Global full summary — Basic summary for the viewer and analyst",
            description = "One-shot endpoint combining totals, categories, trends and recent activity across all users."
    )
    public ResponseEntity<BasicDashboardSummaryResponse> getBasicSummary(Authentication auth) {
        return ResponseEntity.ok(dashboardService.getBasicSummary(auth));
    }
}