package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

enum class BlockchainType {
    STELLAR,
    ETHEREUM,
    SOLANA
}

enum class ChainStatus {
    VERIFIED,
    FAILED,
    ERROR
}

enum class BridgeStatus {
    ACTIVE,
    INACTIVE,
    ERROR
}

enum class TransactionPriority {
    LOW,
    NORMAL,
    HIGH
}

data class CrossChainVerificationResult(
    val isValid: Boolean,
    val sourceChainStatus: ChainStatus,
    val targetChainStatus: ChainStatus,
    val bridgeStatus: BridgeStatus,
    val timestamp: Instant = Instant.now()
)

data class SmartContractDiagnostics(
    val contractAddress: String,
    val errorCode: String,
    val operationType: String,
    val stackTrace: List<String>,
    val gasUsed: Long,
    val timestamp: Instant
)

interface BridgeContract {
    suspend fun verifyTransaction(transactionId: String): Boolean
    suspend fun getBridgeStatus(): BridgeStatus
    suspend fun getLastSyncTime(): Instant
} 