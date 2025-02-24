package com.example.fideicomisoapproverring.recovery.model

import com.example.fideicomisoapproverring.core.model.TransactionError
import java.time.Instant

/**
 * Represents a forensics report for transaction analysis
 */
data class ForensicsReport(
    val transactionId: String,
    val timestamp: Instant,
    val error: TransactionError,
    val isRecoverable: Boolean,
    val systemState: SystemState,
    val riskLevel: RiskLevel,
    val recommendedActions: List<String>,
    val details: Map<String, Any>
)

/**
 * Represents the system state during error analysis
 */
data class SystemState(
    val networkStatus: NetworkStatus,
    val walletStatus: WalletStatus,
    val escrowStatus: EscrowStatus,
    val blockchainState: BlockchainState,
    val timestamp: Instant = Instant.now()
)

/**
 * Represents the current status of the network
 */
enum class NetworkStatus {
    HEALTHY,
    CONGESTED,
    DEGRADED,
    OFFLINE
}

/**
 * Represents the current status of the wallet
 */
enum class WalletStatus {
    CONNECTED,
    DISCONNECTED,
    SYNCING,
    ERROR
}

/**
 * Represents the current status of the escrow
 */
enum class EscrowStatus {
    ACTIVE,
    INACTIVE,
    LOCKED,
    ERROR
}

/**
 * Represents the current state of the blockchain
 */
data class BlockchainState(
    val blockHeight: Long,
    val networkId: String,
    val gasPrice: Double,
    val congestionLevel: CongestionLevel,
    val timestamp: Instant = Instant.now()
)

/**
 * Represents the level of network congestion
 */
enum class CongestionLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Represents the risk level of a transaction error
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
} 