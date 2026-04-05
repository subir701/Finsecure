package com.finSecure.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record CategorySummaryResponse(
        Map<String, BigDecimal> expensesByCategory,
        Map<String, BigDecimal> incomeByCategory
) {}