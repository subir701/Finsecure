package com.finSecure.dto.response;

import java.math.BigDecimal;

public record TotalsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance
) {

}
