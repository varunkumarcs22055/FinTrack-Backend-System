package com.finance.backend.config;

import com.finance.backend.entity.*;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with initial data on startup.
 *
 * Creates:
 * - 1 ADMIN user (admin / admin123) for immediate testing
 * - Sample financial records so the dashboard isn't empty
 *
 * Only runs if the database is empty (safe for restarts).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Seeding database with initial data...");

        // Create default ADMIN user
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@fintrack.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        // Create sample ANALYST user
        User analyst = userRepository.save(User.builder()
                .username("analyst")
                .email("analyst@fintrack.com")
                .password(passwordEncoder.encode("analyst123"))
                .role(Role.ANALYST)
                .build());

        // Create sample VIEWER user
        userRepository.save(User.builder()
                .username("viewer")
                .email("viewer@fintrack.com")
                .password(passwordEncoder.encode("viewer123"))
                .role(Role.VIEWER)
                .build());

        // Seed sample financial records
        createRecord(admin, "5000.00", TransactionType.INCOME, "Salary", "2026-03-01", "March salary");
        createRecord(admin, "1200.00", TransactionType.INCOME, "Freelance", "2026-03-05", "Website project");
        createRecord(admin, "800.00", TransactionType.EXPENSE, "Rent", "2026-03-01", "Monthly rent");
        createRecord(admin, "150.00", TransactionType.EXPENSE, "Utilities", "2026-03-03", "Electricity bill");
        createRecord(admin, "200.00", TransactionType.EXPENSE, "Groceries", "2026-03-07", "Weekly groceries");
        createRecord(admin, "50.00", TransactionType.EXPENSE, "Transport", "2026-03-10", "Uber rides");
        createRecord(admin, "5000.00", TransactionType.INCOME, "Salary", "2026-02-01", "February salary");
        createRecord(admin, "600.00", TransactionType.EXPENSE, "Rent", "2026-02-01", "Monthly rent");
        createRecord(admin, "300.00", TransactionType.INCOME, "Freelance", "2026-02-15", "Logo design");
        createRecord(admin, "100.00", TransactionType.EXPENSE, "Entertainment", "2026-02-20", "Movie tickets");
        createRecord(admin, "5000.00", TransactionType.INCOME, "Salary", "2026-01-01", "January salary");
        createRecord(admin, "750.00", TransactionType.EXPENSE, "Rent", "2026-01-01", "Monthly rent");
        createRecord(admin, "180.00", TransactionType.EXPENSE, "Groceries", "2026-01-12", "Weekly groceries");
        createRecord(admin, "2000.00", TransactionType.INCOME, "Bonus", "2026-01-15", "Year-end bonus");
        createRecord(admin, "90.00", TransactionType.EXPENSE, "Transport", "2026-01-18", "Metro pass");

        log.info("Database seeded successfully! Default users:");
        log.info("  ADMIN:   admin   / admin123");
        log.info("  ANALYST: analyst / analyst123");
        log.info("  VIEWER:  viewer  / viewer123");
    }

    private void createRecord(User creator, String amount, TransactionType type,
                               String category, String date, String description) {
        recordRepository.save(FinancialRecord.builder()
                .amount(new BigDecimal(amount))
                .type(type)
                .category(category)
                .date(LocalDate.parse(date))
                .description(description)
                .createdBy(creator)
                .build());
    }
}
