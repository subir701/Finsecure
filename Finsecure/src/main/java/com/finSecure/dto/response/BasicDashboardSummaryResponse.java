package com.finSecure.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BasicDashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<RecordResponse> recentActivity
) {
}
