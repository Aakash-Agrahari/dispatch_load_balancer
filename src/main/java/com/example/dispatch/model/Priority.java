package com.example.dispatch.model;

/**
 * Delivery order priority. Ordinal order matters: HIGH is processed first,
 * then MEDIUM, then LOW, when building the dispatch plan.
 */
public enum Priority {
    HIGH,
    MEDIUM,
    LOW
}
