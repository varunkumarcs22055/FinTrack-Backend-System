package com.finance.backend.entity;

/**
 * User roles in the system.
 * 
 * ADMIN   — Full access: manage users, records, and dashboard
 * ANALYST — Read access to records and dashboard
 * VIEWER  — Dashboard access only
 */
public enum Role {
    ADMIN,
    ANALYST,
    VIEWER
}
