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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordRepository extends JpaRepository<Record, UUID> {

    @Query("""
            SELECT r FROM Record r WHERE r.user.userId = :userId
            AND (:category IS NULL OR r.category = :category)
            AND (:recordType IS NULL OR r.recordType = :recordType)
            AND (:from IS NULL OR r.createdAt >= :from)
            AND (:to IS NULL OR r.createdAt <= :to)
            ORDER BY r.createdAt DESC
            """)
    Page<Record> findByFilters(
            @Param("userId") UUID userId,
            @Param("category") Category category,
            @Param("recordType") RecordType recordType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
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
            SELECT FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM') AS month,
                   r.recordType,
                   COALESCE(SUM(r.amount), 0)
            FROM Record r
            WHERE r.user.userId = :userId
            AND r.createdAt >= :from
            GROUP BY FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM'), r.recordType
            ORDER BY month ASC
            """)
    List<Object[]> monthlyTrends(@Param("userId") UUID userId, @Param("from") LocalDateTime from);

    // ── Recent activity ───────────────────────────────────────────────────────

    List<Record> findTop10ByUserUserIdOrderByCreatedAtDesc(UUID userId);

    // ── Admin: all records with filters ──────────────────────────────────────

    @Query("""
            SELECT r FROM Record r
                WHERE (:category IS NULL OR r.category = :category)
                AND (:recordType IS NULL OR r.recordType = :recordType)
                AND (cast(:from as localdatetime) IS NULL OR r.createdAt >= :from)
                AND (cast(:to as localdatetime) IS NULL OR r.createdAt <= :to)
                ORDER BY r.createdAt DESC
            """)
    Page<Record> findAllByFilters(
            @Param("category") Category category,
            @Param("recordType") RecordType recordType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
