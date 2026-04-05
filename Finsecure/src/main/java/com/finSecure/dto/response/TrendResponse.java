package com.finSecure.dto.response;

import java.util.List;

public record TrendResponse(
        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends
) {}