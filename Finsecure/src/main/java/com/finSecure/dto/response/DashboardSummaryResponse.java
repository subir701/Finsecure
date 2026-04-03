package com.finSecure.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        Map<String, BigDecimal> expensesByCategory,
        Map<String, BigDecimal> incomeByCategory,
        List<MonthlyTrend> monthlyTrends,
        List<RecordResponse> recentActivity
) {

    public record MonthlyTrend(
            String month,
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal net
    ){}
}
