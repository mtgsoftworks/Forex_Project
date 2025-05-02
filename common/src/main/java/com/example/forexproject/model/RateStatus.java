package com.example.forexproject.model;

/**
 * Enumeration representing the status of a rate coming from a platform.
 * This can be extended as required (e.g. LIVE, STALE, CLOSED, ERROR).
 */
public enum RateStatus {
    LIVE,
    STALE,
    CLOSED,
    ERROR
}
