package com.example.fideicomisoapproverring.recovery.util

import com.example.fideicomisoapproverring.recovery.model.RecoveryResult
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.util.AppLogger
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureAuditLogger @Inject constructor() {
    fun logError(errorId: String, error: TransactionError) {
        val timestamp = Instant.now()
        AppLogger.Recovery.info("""
            [AUDIT] Error Logged
            Timestamp: $timestamp
            Error ID: $errorId
            Transaction ID: ${error.transactionId}
            Type: ${error.type}
            Message: ${error.message}
            Details: ${error.details}
        """.trimIndent())
    }

    fun logRecoveryResult(errorId: String, result: RecoveryResult) {
        val timestamp = Instant.now()
        AppLogger.Recovery.info("""
            [AUDIT] Recovery Result
            Timestamp: $timestamp
            Error ID: $errorId
            Status: ${result.status}
            Message: ${result.message}
            Details: ${result.recoveryDetails}
        """.trimIndent())
    }

    fun logException(errorId: String, exception: Exception) {
        val timestamp = Instant.now()
        AppLogger.Recovery.error("""
            [AUDIT] Exception Logged
            Timestamp: $timestamp
            Error ID: $errorId
            Exception: ${exception.javaClass.simpleName}
            Message: ${exception.message}
            Stack Trace: ${exception.stackTraceToString()}
        """.trimIndent())
    }

    fun logRecoveryCancelled(errorId: String) {
        val timestamp = Instant.now()
        AppLogger.Recovery.info("""
            [AUDIT] Recovery Cancelled
            Timestamp: $timestamp
            Error ID: $errorId
        """.trimIndent())
    }
} 