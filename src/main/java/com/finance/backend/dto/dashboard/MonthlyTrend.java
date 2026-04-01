package com.finance.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monthly trend data point — year, month, income, expense.
 * Used to build line/bar charts on the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrend {

    private int year;
    private int month;
    private BigDecimal income;
    private BigDecimal expense;
}
