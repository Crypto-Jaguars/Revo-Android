package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents the current status of a transaction recovery process.
 */
enum class RecoveryStatus {
    ANALYZING,       // System is analyzing the error
    ATTEMPTING,      // Recovery attempt in progress
    SUCCEEDED,       // Recovery completed successfully
    FAILED,         // Recovery attempt failed
    RETRYING,       // Retrying the recovery process
    MANUAL_INTERVENTION_REQUIRED  // Manual intervention is needed
} 