package com.example.fideicomisoapproverring.stellar.model

import java.time.Instant

/**
 * Represents a Stellar blockchain transaction with its associated metadata
 */
data class StellarTransaction(
    val id: String,
    val createdAt: Instant,
    val lastModified: Instant,
    val sourceAccount: String,
    val fee: Long,
    val operations: List<StellarOperation>,
    val memo: String?,
    val signatures: List<String>,
    val status: TransactionStatus
)

/**
 * Represents a Stellar operation within a transaction
 */
data class StellarOperation(
    val type: String,
    val sourceAccount: String,
    val amount: String?,
    val asset: String?,
    val destination: String?
)

/**
 * Represents the status of a Stellar transaction
 */
enum class TransactionStatus {
    PENDING,
    SUBMITTED,
    CONFIRMED,
    FAILED
} 