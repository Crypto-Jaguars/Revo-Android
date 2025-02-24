package com.example.fideicomisoapproverring.core.model

sealed class TransactionError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class InsufficientFundsError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class TimeoutError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class BlockchainError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class WalletError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)

    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TransactionError(message, cause)
}

enum class TransactionStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    RECOVERY_NEEDED,
    RECOVERY_IN_PROGRESS,
    RECOVERY_COMPLETED,
    RECOVERY_FAILED
}

data class TransactionResult(
    val transactionId: String,
    val status: TransactionStatus,
    val error: TransactionError? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class TransactionVerification(
    val transactionId: String,
    val isValid: Boolean,
    val confirmations: Int,
    val timestamp: Long,
    val error: TransactionError? = null
) 