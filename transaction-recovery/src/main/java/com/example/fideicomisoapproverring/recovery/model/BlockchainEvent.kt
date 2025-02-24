package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Represents various blockchain events that can occur during transaction processing
 */
sealed interface BlockchainEvent {
    val transactionId: String
    val timestamp: Instant
    
    data class TransactionSubmitted(
        override val transactionId: String,
        override val timestamp: Instant,
        val sequence: Long,
        val fee: Long
    ) : BlockchainEvent
    
    data class TransactionConfirmed(
        override val transactionId: String,
        override val timestamp: Instant,
        val blockHeight: Long,
        val confirmations: Int
    ) : BlockchainEvent
    
    data class TransactionFailed(
        override val transactionId: String,
        override val timestamp: Instant,
        val error: TransactionError,
        val canRetry: Boolean
    ) : BlockchainEvent
    
    data class SmartContractEvent(
        override val transactionId: String,
        override val timestamp: Instant = Instant.now(),
        val contractAddress: String,
        val eventName: String,
        val parameters: Map<String, String>
    ) : BlockchainEvent
    
    data class EscrowEvent(
        override val transactionId: String,
        override val timestamp: Instant = Instant.now(),
        val escrowId: String,
        val eventType: EscrowEventType,
        val participants: List<String>
    ) : BlockchainEvent
    
    data class NetworkEvent(
        override val transactionId: String,
        override val timestamp: Instant,
        val eventType: NetworkEventType,
        val details: Map<String, String>
    ) : BlockchainEvent
}

enum class EscrowEventType {
    CREATED,
    FUNDED,
    RELEASED,
    REFUNDED,
    DISPUTED,
    RESOLVED
}

enum class NetworkEventType {
    CONGESTION_INCREASED,
    CONGESTION_DECREASED,
    FEE_SPIKE,
    PROTOCOL_UPGRADE,
    NETWORK_RESET
} 