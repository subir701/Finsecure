package com.finSecure.dto.request;

import com.finSecure.entity.Category;
import com.finSecure.entity.RecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Record type is required")
        RecordType recordType,

        @NotBlank(message = "Note is required")
        String note,

        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate
) {}
