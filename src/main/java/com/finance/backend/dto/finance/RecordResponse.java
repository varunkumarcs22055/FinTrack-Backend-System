package com.finance.backend.dto.finance;

import com.finance.backend.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for financial records.
 * Includes the creator's username instead of the full User object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String description;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
