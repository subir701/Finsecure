package com.finSecure.service;

import com.finSecure.dto.response.*;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;


    // ── 2. Totals only ────────────────────────────────────────────────────────

    @Override
    public TotalsResponse getTotalsByUserId(Authentication auth) {
        log.debug("Fetching totals for user: {}", auth.getName());
        UUID userId = getUser(auth).getUserId();

        BigDecimal totalIncome   = recordRepository.sumIncomeByUser(userId);
        BigDecimal totalExpenses = recordRepository.sumExpenseByUser(userId);

        return new TotalsResponse(totalIncome, totalExpenses, totalIncome.subtract(totalExpenses));
    }

    @Override
    public TotalsResponse getGlobalTotals() {
        log.debug("Fetching global totals.");

        BigDecimal totalIncome   = recordRepository.sumTotalIncome();
        BigDecimal totalExpenses = recordRepository.sumTotalExpense();

        return new TotalsResponse(totalIncome, totalExpenses, totalIncome.subtract(totalExpenses));
    }

    // ── 3. Category breakdown ─────────────────────────────────────────────────

    @Override
    public CategorySummaryResponse getCategorySummaryByUserId(Authentication auth) {
        log.debug("Fetching category summary for user: {}", auth.getName());
        UUID userId = getUser(auth).getUserId();

        return new CategorySummaryResponse(
                buildCategoryMap(userId, RecordType.EXPENSE),
                buildCategoryMap(userId, RecordType.INCOME)
        );
    }

    @Override
    public CategorySummaryResponse getGlobalCategorySummary() {
        log.debug("Fetching Global category summary");


        return new CategorySummaryResponse(
                buildGlobalCategoryMap(RecordType.EXPENSE),
                buildGlobalCategoryMap(RecordType.INCOME)
        );
    }

    // ── 4. Monthly trends ─────────────────────────────────────────────────────

    @Override
    public TrendResponse getMonthlyTrendsByUserId(Authentication auth) {
        log.debug("Fetching monthly trends for user: {}", auth.getName());
        UUID userId = getUser(auth).getUserId();
        return new TrendResponse(buildMonthlyTrends(userId));
    }

    @Override
    public TrendResponse getGlobalMonthlyTrends(Integer months) {
        log.debug("Fetching Global monthly trends");
        LocalDate monthsAgo = LocalDate.now().minusMonths(months);
        return new TrendResponse(mapTrends(recordRepository.globalMonthlyTrends(monthsAgo)));
    }

    // ── 5. Recent activity ────────────────────────────────────────────────────

    @Override
    public List<RecordResponse> getRecentActivityByUserId(Authentication auth) {
        log.debug("Fetching recent activity for user: {}", auth.getName());
        UUID userId = getUser(auth).getUserId();
        return getRecentActivityListByUserId(userId);
    }

    @Override
    public List<RecordResponse> getRecentActivity() {
        log.debug("Fetching recent activity");
        return getRecentActivityList();
    }

    // ── 6. Admin: global summary across all users ─────────────────────────────

    @Override
    public DashboardSummaryResponse getGlobalSummary(Authentication auth) {
        log.info("Building global dashboard summary (admin): {}", auth.getName());

        BigDecimal totalIncome   = recordRepository.sumTotalIncome();
        BigDecimal totalExpenses = recordRepository.sumTotalExpense();
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> expensesByCategory = buildGlobalCategoryMap(RecordType.EXPENSE);
        Map<String, BigDecimal> incomeByCategory   = buildGlobalCategoryMap(RecordType.INCOME);

        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        List<Object[]> raw = recordRepository.globalMonthlyTrends(sixMonthsAgo);
        List<DashboardSummaryResponse.MonthlyTrend> trends = mapTrends(raw);

        List<RecordResponse> recentActivity = recordRepository
                .findTop10ByOrderByTransactionDateDesc()
                .stream().map(this::toResponse).toList();

        return new DashboardSummaryResponse(
                totalIncome, totalExpenses, netBalance,
                expensesByCategory, incomeByCategory, trends, recentActivity
        );
    }

    @Override
    public BasicDashboardSummaryResponse getBasicSummary(Authentication auth) {
        log.info("Building basic dashboard summary: {}", auth.getName());

        BigDecimal totalIncome   = recordRepository.sumTotalIncome();
        BigDecimal totalExpenses = recordRepository.sumTotalExpense();
        BigDecimal netBalance    = totalIncome.subtract(totalExpenses);


        List<RecordResponse> recentActivity = recordRepository
                .findTop10ByOrderByTransactionDateDesc()
                .stream().map(this::toResponse).toList();

        return new BasicDashboardSummaryResponse(
                totalIncome, totalExpenses, netBalance, recentActivity
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Map<String, BigDecimal> buildCategoryMap(UUID userId, RecordType type) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        recordRepository.sumAmountByCategoryAndType(userId, type)
                .forEach(row -> map.put(((Category) row[0]).name(), (BigDecimal) row[1]));
        return map;
    }

    private Map<String, BigDecimal> buildGlobalCategoryMap(RecordType type) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        recordRepository.sumTotalByCategoryAndType(type)
                .forEach(row -> map.put(((Category) row[0]).name(), (BigDecimal) row[1]));
        return map;
    }

    private List<DashboardSummaryResponse.MonthlyTrend> buildMonthlyTrends(UUID userId) {
        // Fixed: was LocalDateTime, must be LocalDate to match transactionDate field type
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return mapTrends(recordRepository.monthlyTrends(userId, sixMonthsAgo));
    }

    private List<DashboardSummaryResponse.MonthlyTrend> mapTrends(List<Object[]> raw) {
        Map<String, BigDecimal[]> monthMap = new LinkedHashMap<>();
        for (Object[] row : raw) {
            String month     = (String) row[0];
            RecordType type  = (RecordType) row[1];
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
                )).toList();
    }

    public List<RecordResponse> getRecentActivityListByUserId(UUID userId) {
        return recordRepository.findTop10ByUserUserIdOrderByTransactionDateDesc(userId)
                .stream().map(this::toResponse).toList();
    }
    private List<RecordResponse> getRecentActivityList() {
        return recordRepository.findTop10ByOrderByTransactionDateDesc()
                .stream().map(this::toResponse).toList();
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