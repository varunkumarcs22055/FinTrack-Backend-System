package com.finance.backend.service;

import com.finance.backend.dto.finance.RecordRequest;
import com.finance.backend.dto.finance.RecordResponse;
import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.TransactionType;
import com.finance.backend.entity.User;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Financial records service — CRUD + filtering.
 *
 * Design decisions:
 * - createdBy is auto-set from SecurityContext (user can't fake it)
 * - Filtering uses optional parameters — all nullable in the repository query
 * - Returns Page<RecordResponse> for pagination support
 */
@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;

    /**
     * Create a new financial record.
     * The creator is automatically set from the authenticated user.
     */
    public RecordResponse createRecord(RecordRequest request) {
        TransactionType type = parseType(request.getType());
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(type)
                .category(request.getCategory().trim())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        record = recordRepository.save(record);
        return toResponse(record);
    }

    /**
     * Get all records with optional filtering and pagination.
     *
     * @param typeStr    filter by INCOME/EXPENSE (nullable)
     * @param category   filter by category (nullable)
     * @param startDate  filter from date (nullable)
     * @param endDate    filter to date (nullable)
     * @param page       page number (0-based)
     * @param size       page size
     */
    public Page<RecordResponse> getRecords(String typeStr, String category,
                                            LocalDate startDate, LocalDate endDate,
                                            int page, int size) {
        TransactionType type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            type = parseType(typeStr);
        }

        String cat = (category != null && !category.isBlank()) ? category : null;
        Pageable pageable = PageRequest.of(page, size);

        return recordRepository.findWithFilters(type, cat, startDate, endDate, pageable)
                .map(this::toResponse);
    }

    /**
     * Get a single record by ID.
     */
    public RecordResponse getRecordById(Long id) {
        FinancialRecord record = findOrThrow(id);
        return toResponse(record);
    }

    /**
     * Update an existing record.
     */
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        FinancialRecord record = findOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(parseType(request.getType()));
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        record = recordRepository.save(record);
        return toResponse(record);
    }

    /**
     * Delete a record.
     */
    public void deleteRecord(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Record not found with id: " + id);
        }
        recordRepository.deleteById(id);
    }

    /**
     * Get all distinct categories (for filter dropdowns in the frontend).
     */
    public List<String> getCategories() {
        return recordRepository.findDistinctCategories();
    }

    // ---- Private helpers ----

    private FinancialRecord findOrThrow(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
    }

    private TransactionType parseType(String type) {
        try {
            return TransactionType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid type: " + type + ". Must be INCOME or EXPENSE");
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
