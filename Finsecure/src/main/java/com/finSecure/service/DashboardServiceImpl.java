package com.finSecure.service;

import com.finSecure.dto.response.DashboardSummaryResponse;
import com.finSecure.dto.response.RecordResponse;
import com.finSecure.entity.Category;
import com.finSecure.entity.Record;
import com.finSecure.entity.RecordType;
import com.finSecure.entity.User;
import com.finSecure.exception.ApiException;
import com.finSecure.repository.RecordRepository;
import com.finSecure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService{

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardSummaryResponse getSummary(Authentication auth) {
        log.info("Building dashboard summary for user: {}", auth.getName());

        UUID userId = getUser(auth).getUserId();

        BigDecimal totalIncome = recordRepository.sumIncomeByUser(userId);
        BigDecimal totalExpenses = recordRepository.sumExpenseByUser(userId);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        log.debug("Summary totals: user={}, income={}, expenses={}, net={}",
                auth.getName(), totalIncome, totalExpenses, netBalance);

        Map<String, BigDecimal> expensesByCategory = buildCategoryMap(userId,RecordType.EXPENSE);
        Map<String, BigDecimal> incomeByCategory = buildCategoryMap(userId, RecordType.INCOME);

        List<DashboardSummaryResponse.MonthlyTrend> monthlyTrends = buildMonthlyTrends(userId);
        log.debug("Monthly trends built: {} months of data for user={}", monthlyTrends.size(), auth.getName());

        List<RecordResponse> recentActivity = recordRepository
                .findTop10ByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        log.info("Dashboard summary built successfully for user={}", auth.getName());

        return new DashboardSummaryResponse(totalIncome,totalExpenses,netBalance,expensesByCategory,incomeByCategory,monthlyTrends,recentActivity);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, BigDecimal> buildCategoryMap(UUID userId, RecordType type) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        recordRepository.sumAmountByCategoryAndType(userId, type)
                .forEach(row -> map.put(((Category) row[0]).name(), (BigDecimal) row[1]));
        return map;
    }

    private List<DashboardSummaryResponse.MonthlyTrend> buildMonthlyTrends(UUID userId) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> raw = recordRepository.monthlyTrends(userId, sixMonthsAgo);

        // raw rows: [month, recordType, sum]  — two rows per month (INCOME + EXPENSE)
        Map<String, BigDecimal[]> monthMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String month = (String) row[0];
            RecordType type = (RecordType) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            monthMap.computeIfAbsent(month, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (type == RecordType.INCOME) {
                monthMap.get(month)[0] = amount;
            } else {
                monthMap.get(month)[1] = amount;
            }
        }

        return monthMap.entrySet().stream()
                .map(e -> new DashboardSummaryResponse.MonthlyTrend(
                        e.getKey(),
                        e.getValue()[0],
                        e.getValue()[1],
                        e.getValue()[0].subtract(e.getValue()[1])
                ))
                .toList();
    }

    private User getUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> ApiException.notFound("Authenticated user not found"));
    }

    private RecordResponse toResponse(Record record) {
        return new RecordResponse(
                record.getRecordId(),
                record.getAmount(),
                record.getCategory(),
                record.getRecordType(),
                record.getNote(),
                record.getTransactionDate(),
                record.getCreatedAt()
        );
    }
}
