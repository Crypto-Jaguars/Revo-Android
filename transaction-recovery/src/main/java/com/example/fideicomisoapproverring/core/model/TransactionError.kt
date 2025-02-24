package com.example.fideicomisoapproverring.core.model

import java.time.Instant

enum class ErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

sealed class TransactionError {
    abstract val transactionId: String
    abstract val message: String
    abstract val timestamp: Instant
    abstract val severity: ErrorSeverity

    data class NetworkError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.MEDIUM
    ) : TransactionError()

    data class BlockchainError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH
    ) : TransactionError()

    data class WalletError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH
    ) : TransactionError()

    data class ValidationError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.LOW
    ) : TransactionError()

    data class TimeoutError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.MEDIUM
    ) : TransactionError()

    data class UnknownError(
        override val transactionId: String,
        override val message: String,
        override val timestamp: Instant,
        override val severity: ErrorSeverity = ErrorSeverity.HIGH
    ) : TransactionError()
} 