package com.finance.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Category-wise total for dashboard breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTotal {

    private String category;
    private BigDecimal total;
}
