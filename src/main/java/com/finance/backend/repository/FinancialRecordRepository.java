package com.finance.backend.repository;

import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Financial record data access layer.
 * Contains both CRUD (inherited) and custom queries for filtering + dashboard aggregation.
 *
 * Why custom @Query instead of derived method names?
 * - Derived names like findByTypeAndCategoryAndDateBetween become unreadable
 * - @Query is more maintainable and easier to optimize
 */
@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // ---- Filtering (all params optional via JPQL conditions) ----

    @Query("SELECT r FROM FinancialRecord r WHERE "
            + "(:type IS NULL OR r.type = :type) AND "
            + "(:category IS NULL OR r.category = :category) AND "
            + "(:startDate IS NULL OR r.date >= :startDate) AND "
            + "(:endDate IS NULL OR r.date <= :endDate) "
            + "ORDER BY r.date DESC")
    Page<FinancialRecord> findWithFilters(
            @Param("type") TransactionType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    // ---- Dashboard aggregation queries ----

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r "
            + "WHERE r.type = :type GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumByCategoryAndType(@Param("type") TransactionType type);

    @Query("SELECT FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type, SUM(r.amount) "
            + "FROM FinancialRecord r "
            + "GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type "
            + "ORDER BY FUNCTION('YEAR', r.date) DESC, FUNCTION('MONTH', r.date) DESC")
    List<Object[]> monthlyTrends();

    List<FinancialRecord> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT r.category FROM FinancialRecord r ORDER BY r.category")
    List<String> findDistinctCategories();
}
