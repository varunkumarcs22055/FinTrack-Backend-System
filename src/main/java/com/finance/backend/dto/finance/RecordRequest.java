package com.finance.backend.dto.finance;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating a financial record.
 * Validation ensures data integrity before it hits the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Type is required (INCOME or EXPENSE)")
    private String type; // Validated in service — cleaner error than deserialization failure

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be at most 50 characters")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;
}
