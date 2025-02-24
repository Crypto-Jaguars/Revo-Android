package com.example.fideicomisoapproverring.stellar.model

import java.time.Instant

data class StellarTransaction(
    val transactionId: String,
    val escrowAddress: String,
    val sourceAccount: String,
    val destinationAccount: String,
    val amount: String,
    val signatures: List<TransactionSignature>,
    val timestamp: Instant = Instant.now()
)

data class TransactionSignature(
    val accountId: String,
    val signature: ByteArray,
    val timestamp: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionSignature
        if (accountId != other.accountId) return false
        if (!signature.contentEquals(other.signature)) return false
        return timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = accountId.hashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class EscrowData(
    val participants: List<String>,
    val conditions: List<EscrowConditionData>,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val lockPeriod: Long?,
    val releaseSchedule: List<ReleaseScheduleData>
)

data class EscrowConditionData(
    val type: String,
    val parameters: Map<String, String>
)

data class ReleaseScheduleData(
    val amount: String,
    val scheduledTime: Instant,
    val beneficiary: String,
    val conditions: List<String>
)

data class OracleData(
    val isValid: Boolean,
    val data: Map<String, String>,
    val timestamp: Instant = Instant.now()
) 