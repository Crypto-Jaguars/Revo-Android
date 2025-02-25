package com.example.fideicomisoapproverring.recovery.model

import java.time.Instant

/**
 * Enhanced escrow verification model with comprehensive validation parameters
 */
data class EscrowVerificationModel(
    val escrowId: String,
    val contractAddress: String,
    val participants: List<String>,
    val conditions: List<EscrowCondition>,
    val timeConstraints: EscrowTimeConstraints,
    val verificationStatus: EscrowVerificationStatus,
    val lastVerifiedAt: Instant = Instant.now()
)

data class EscrowCondition(
    val type: EscrowConditionType,
    val parameters: Map<String, String>,
    val isSatisfied: Boolean,
    val verificationTimestamp: Instant = Instant.now()
)

data class EscrowTimeConstraints(
    val createdAt: Instant,
    val expiresAt: Instant?,
    val lockPeriod: Long?, // in seconds
    val releaseSchedule: List<ScheduledRelease>
)

data class ScheduledRelease(
    val amount: String,
    val scheduledTime: Instant,
    val beneficiary: String,
    val conditions: List<String>
)

enum class EscrowConditionType {
    MULTI_SIG,
    TIME_LOCK,
    ORACLE_VALIDATION,
    EXTERNAL_TRIGGER,
    ATOMIC_SWAP,
    THRESHOLD_CONDITION
}

enum class EscrowVerificationStatus {
    PENDING,
    IN_PROGRESS,
    VERIFIED,
    FAILED,
    EXPIRED,
    DISPUTED
} 