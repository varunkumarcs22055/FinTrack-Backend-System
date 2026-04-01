package com.finance.backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a user's role.
 * Only ADMIN can use this.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private String role; // Accepts "ADMIN", "ANALYST", "VIEWER" — validated in service
}
