package com.finance.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a financial transaction (income or expense).
 *
 * Design decisions:
 * - BigDecimal for amount — never use double/float for money
 * - Unidirectional ManyToOne to User — avoids lazy-loading issues and
 *   circular serialization; we only need to know WHO created a record
 * - Category is free-text (not an enum) for flexibility
 * - date is the transaction date; createdAt is the audit timestamp
 */
@Entity
@Table(name = "financial_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String description;

    /**
     * The user who created this record.
     * Unidirectional — User entity has no reference back to records.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
