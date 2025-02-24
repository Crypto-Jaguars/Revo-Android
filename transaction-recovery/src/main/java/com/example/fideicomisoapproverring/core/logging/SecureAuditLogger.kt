package com.example.fideicomisoapproverring.core.logging

interface SecureAuditLogger {
    /**
     * Logs an event with metadata
     */
    suspend fun logEvent(
        eventType: String,
        message: String,
        metadata: Map<String, String> = emptyMap()
    )

    /**
     * Gets audit logs for a specific event type
     */
    suspend fun getAuditLogs(
        eventType: String,
        limit: Int = 100
    ): List<AuditLogEntry>

    /**
     * Gets audit logs for a specific transaction
     */
    suspend fun getTransactionAuditLogs(
        transactionId: String,
        limit: Int = 100
    ): List<AuditLogEntry>
}

data class AuditLogEntry(
    val eventType: String,
    val message: String,
    val metadata: Map<String, String>,
    val timestamp: java.time.Instant,
    val id: String
) 