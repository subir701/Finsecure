package com.finSecure.repository;

import com.finSecure.entity.Category;
import com.finSecure.entity.Record;
import com.finSecure.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordRepository extends JpaRepository<Record, UUID> {

    @Query("""
            SELECT r FROM Record r WHERE r.user.userId = :userId
            AND (:category IS NULL OR r.category = :category)
            AND (:recordType IS NULL OR r.recordType = :recordType)
            AND (cast(:from as localdate) IS NULL OR r.transactionDate >= :from)
            AND (cast(:to as localdate) IS NULL OR r.transactionDate <= :to)
            ORDER BY r.transactionDate DESC
            """)
    Page<Record> findByFilters(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("recordType") RecordType recordType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
            );

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM Record r WHERE r.user.userId = :userId AND r.recordType = 'INCOME'")
    BigDecimal sumIncomeByUser(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM Record r WHERE r.user.userId = :userId AND r.recordType = 'EXPENSE'")
    BigDecimal sumExpenseByUser(@Param("userId") UUID userId);

    @Query("""
            SELECT r.category, COALESCE(SUM(r.amount), 0)
            FROM Record r
            WHERE r.user.userId = :userId AND r.recordType = :recordType
            GROUP BY r.category
            """)
    List<Object[]> sumAmountByCategoryAndType(@Param("userId") UUID userId, @Param("recordType") RecordType recordType);

    @Query("""
            SELECT FUNCTION('TO_CHAR', r.transactionDate, 'YYYY-MM') AS month,
                   r.recordType,
                   COALESCE(SUM(r.amount), 0)
            FROM Record r
            WHERE r.user.userId = :userId
            AND r.transactionDate >= :from
            GROUP BY FUNCTION('TO_CHAR', r.transactionDate, 'YYYY-MM'), r.recordType
            ORDER BY month ASC
            """)
    List<Object[]> monthlyTrends(@Param("userId") UUID userId, @Param("from") LocalDate from);

    // ── Recent activity ───────────────────────────────────────────────────────

    List<Record> findTop10ByUserUserIdOrderByTransactionDateDesc(UUID userId);

    // ── Admin: all records with filters ──────────────────────────────────────

    @Query("""
            SELECT r FROM Record r
                WHERE (:category IS NULL OR r.category = :category)
                AND (:recordType IS NULL OR r.recordType = :recordType)
                AND (cast(:from as localdate) IS NULL OR r.transactionDate >= :from)
                AND (cast(:to as localdate) IS NULL OR r.transactionDate <= :to)
                ORDER BY r.transactionDate DESC
            """)
    Page<Record> findAllByFilters(
            @Param("category") Category category,
            @Param("recordType") RecordType recordType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM Record r WHERE r.recordType = 'INCOME'")
    BigDecimal sumTotalIncome();

    @Query("SELECT COALESCE(SUM(r.amount),0) FROM Record r WHERE r.recordType = 'EXPENSE'")
    BigDecimal sumTotalExpense();

    @Query("""
            SELECT r.category, COALESCE(SUM(r.amount), 0)
            FROM Record r WHERE r.recordType = :recordType
            GROUP BY r.category
            """)
    List<Object[]> sumTotalByCategoryAndType(@Param("recordType") RecordType recordType);

    @Query("""
            SELECT FUNCTION('TO_CHAR', r.transactionDate, 'YYYY-MM') AS month,
                   r.recordType,
                   COALESCE(SUM(r.amount), 0)
            FROM Record r
            WHERE r.transactionDate >= :from
            GROUP BY FUNCTION('TO_CHAR', r.transactionDate, 'YYYY-MM'), r.recordType
            ORDER BY month ASC
            """)
    List<Object[]> globalMonthlyTrends(@Param("from") LocalDate from);

    List<Record> findTop10ByOrderByTransactionDateDesc();
}
