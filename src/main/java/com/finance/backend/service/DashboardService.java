package com.finance.backend.service;

import com.finance.backend.dto.dashboard.CategoryTotal;
import com.finance.backend.dto.dashboard.DashboardSummary;
import com.finance.backend.dto.dashboard.MonthlyTrend;
import com.finance.backend.dto.finance.RecordResponse;
import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.TransactionType;
import com.finance.backend.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard service — aggregates financial data for summary views.
 *
 * Uses repository-level queries for aggregation (pushed to the DB)
 * rather than loading all records into memory.
 * This is important for performance as data grows.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    /**
     * Financial summary: total income, total expenses, net balance, record count.
     */
    public DashboardSummary getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByType(TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        long totalRecords = recordRepository.count();

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .build();
    }

    /**
     * Category-wise totals for income and expense.
     */
    public Map<String, List<CategoryTotal>> getCategoryTotals() {
        List<CategoryTotal> incomeTotals = mapToCategoryTotals(
                recordRepository.sumByCategoryAndType(TransactionType.INCOME));
        List<CategoryTotal> expenseTotals = mapToCategoryTotals(
                recordRepository.sumByCategoryAndType(TransactionType.EXPENSE));

        Map<String, List<CategoryTotal>> result = new LinkedHashMap<>();
        result.put("income", incomeTotals);
        result.put("expense", expenseTotals);
        return result;
    }

    /**
     * Monthly trends — income and expense per month.
     * Groups raw query results by (year, month) and merges income/expense.
     */
    public List<MonthlyTrend> getMonthlyTrends() {
        List<Object[]> raw = recordRepository.monthlyTrends();

        // Group by year-month, merge income/expense
        Map<String, MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : raw) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type = (TransactionType) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            String key = year + "-" + month;
            MonthlyTrend trend = trendMap.computeIfAbsent(key, k ->
                    MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .income(BigDecimal.ZERO)
                            .expense(BigDecimal.ZERO)
                            .build());

            if (type == TransactionType.INCOME) {
                trend.setIncome(amount);
            } else {
                trend.setExpense(amount);
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    /**
     * Recent 10 transactions (for activity feed).
     */
    public List<RecordResponse> getRecentTransactions() {
        return recordRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private List<CategoryTotal> mapToCategoryTotals(List<Object[]> raw) {
        return raw.stream()
                .map(row -> CategoryTotal.builder()
                        .category((String) row[0])
                        .total((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    private RecordResponse toResponse(FinancialRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .description(record.getDescription())
                .createdByUsername(record.getCreatedBy().getUsername())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
