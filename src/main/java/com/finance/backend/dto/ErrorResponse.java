package com.finance.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response returned by the GlobalExceptionHandler.
 * Ensures all errors follow a consistent JSON structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Omit null fields from JSON
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<String> details; // For validation errors — lists each field error
    private LocalDateTime timestamp;
}
