package com.example.fideicomisoapproverring.recovery.forensics

import com.example.fideicomisoapproverring.recovery.model.TransactionError
import java.time.Instant

data class ForensicsReport(
    val transactionId: String,
    val timestamp: Instant,
    val error: TransactionError,
    val isRecoverable: Boolean,
    val requiresUserAction: Boolean,
    val recommendedAction: String?,
    val errorPatterns: List<ErrorPattern>,
    val systemState: SystemState,
    val recoveryProbability: Float,
    val additionalData: Map<String, String> = emptyMap()
)

data class ErrorPattern(
    val patternId: String,
    val description: String,
    val frequency: Int,
    val firstOccurrence: Instant,
    val lastOccurrence: Instant,
    val relatedTransactions: List<String>
)

data class SystemState(
    val networkStatus: NetworkStatus,
    val walletStatus: WalletStatus,
    val escrowStatus: EscrowStatus,
    val blockchainState: BlockchainState
)

enum class NetworkStatus {
    HEALTHY,
    CONGESTED,
    DEGRADED,
    OFFLINE
}

enum class WalletStatus {
    CONNECTED,
    DISCONNECTED,
    SYNCING,
    ERROR
}

enum class EscrowStatus {
    ACTIVE,
    LOCKED,
    PENDING,
    RELEASED,
    ERROR
}

data class BlockchainState(
    val lastBlockHeight: Long,
    val averageBlockTime: Float,
    val networkCongestion: Float,
    val gasPrice: String
) 