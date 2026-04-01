package com.finance.backend.controller;

import com.finance.backend.dto.dashboard.CategoryTotal;
import com.finance.backend.dto.dashboard.DashboardSummary;
import com.finance.backend.dto.dashboard.MonthlyTrend;
import com.finance.backend.dto.finance.RecordResponse;
import com.finance.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Dashboard endpoints — accessible to ALL authenticated users (ADMIN, ANALYST, VIEWER).
 *
 * No @PreAuthorize needed — SecurityConfig requires authentication for all
 * non-public endpoints, and dashboard is open to all roles.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Summary analytics for the finance dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary",
            description = "Total income, total expenses, net balance, record count")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/category-totals")
    @Operation(summary = "Get category-wise totals",
            description = "Income and expense breakdown by category")
    public ResponseEntity<Map<String, List<CategoryTotal>>> getCategoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    @GetMapping("/monthly-trends")
    @Operation(summary = "Get monthly trends",
            description = "Income and expense by month for trend charts")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrends() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends());
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions",
            description = "Last 10 transactions for activity feed")
    public ResponseEntity<List<RecordResponse>> getRecentTransactions() {
        return ResponseEntity.ok(dashboardService.getRecentTransactions());
    }
}
