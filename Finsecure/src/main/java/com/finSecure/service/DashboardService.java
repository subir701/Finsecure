package com.finSecure.service;

import com.finSecure.dto.response.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface DashboardService {
    // ── User-scoped (VIEWER, ANALYST, ADMIN) ──────────────────────────────────
    TotalsResponse getTotalsByUserId(Authentication auth);
    CategorySummaryResponse getCategorySummaryByUserId(Authentication auth);
    TrendResponse getMonthlyTrendsByUserId(Authentication auth);
    List<RecordResponse> getRecentActivityByUserId(Authentication auth);

    // ── System-wide (ANALYST, ADMIN) ──────────────────────────────────────────
    TotalsResponse getGlobalTotals();
    CategorySummaryResponse getGlobalCategorySummary();
    TrendResponse getGlobalMonthlyTrends(Integer months);
    List<RecordResponse> getRecentActivity();

    // ── Full summary shortcuts ────────────────────────────────────────────────
    DashboardSummaryResponse getGlobalSummary(Authentication auth);
    BasicDashboardSummaryResponse getBasicSummary(Authentication auth);
}
