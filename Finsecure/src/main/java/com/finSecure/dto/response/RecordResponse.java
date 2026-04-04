package com.finSecure.dto.response;

import com.finSecure.entity.Category;
import com.finSecure.entity.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecordResponse(
        UUID recordId,
        BigDecimal amount,
        Category category,
        RecordType recordType,
        String note,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {
}
