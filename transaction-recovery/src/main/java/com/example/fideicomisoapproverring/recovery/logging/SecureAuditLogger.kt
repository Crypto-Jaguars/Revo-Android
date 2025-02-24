package com.example.fideicomisoapproverring.recovery.logging

import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.recovery.model.TransactionErrorStatus
import com.example.fideicomisoapproverring.util.AppLogger
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureAuditLogger @Inject constructor() {
    private val logEntries = Collections.synchronizedList(mutableListOf<AuditLogEntry>())
    
    fun logTransactionError(errorId: String, message: String) {
        AppLogger.Recovery.error("Transaction Error [$errorId]: $message")
    }

    fun logRecoveryAttempt(errorId: String, attempt: Int) {
        AppLogger.Recovery.info("Recovery Attempt [$errorId]: Attempt #$attempt")
    }

    fun logRecoverySuccess(errorId: String) {
        AppLogger.Recovery.info("Recovery Success [$errorId]: Transaction recovered successfully")
    }

    fun logRecoveryFailure(errorId: String, reason: String) {
        AppLogger.Recovery.error("Recovery Failed [$errorId]: $reason")
    }

    fun logManualIntervention(errorId: String, ticketId: String, reason: String) {
        AppLogger.Recovery.warning("Manual Intervention Required [$errorId]: $reason (Ticket: $ticketId)")
    }

    fun logRecoveryCancelled(errorId: String) {
        AppLogger.Recovery.info("Recovery Cancelled [$errorId]")
    }

    fun logTransactionRollback(transactionId: String, status: String) {
        AppLogger.Recovery.info("Transaction Rollback [$transactionId]: $status")
    }

    fun logEscrowVerification(transactionId: String, status: String) {
        AppLogger.Recovery.info("Escrow Verification [$transactionId]: $status")
    }
    
    fun logTransactionError(error: TransactionError) {
        val entry = AuditLogEntry(
            timestamp = System.currentTimeMillis(),
            type = LogEntryType.ERROR,
            errorId = error.id,
            transactionId = error.transactionId,
            message = error.message,
            hash = generateLogHash(error)
        )
        logEntries.add(entry)
        AppLogger.Transaction.error("Transaction error logged: ${error.id}")
    }
    
    fun logRecoverySuccess(error: TransactionError) {
        val entry = AuditLogEntry(
            timestamp = System.currentTimeMillis(),
            type = LogEntryType.RECOVERY,
            errorId = error.id,
            transactionId = error.transactionId,
            message = "Recovery successful",
            hash = generateLogHash(error)
        )
        logEntries.add(entry)
        AppLogger.Recovery.info("Recovery success logged: ${error.id}")
    }
    
    fun logManualIntervention(error: TransactionError, reason: String) {
        val entry = AuditLogEntry(
            timestamp = System.currentTimeMillis(),
            type = LogEntryType.MANUAL_INTERVENTION,
            errorId = error.id,
            transactionId = error.transactionId,
            message = reason,
            hash = generateLogHash(error)
        )
        logEntries.add(entry)
        AppLogger.Recovery.info("Manual intervention logged: ${error.id}")
    }
    
    fun getLogEntries(errorId: String): List<AuditLogEntry> {
        return logEntries.filter { it.errorId == errorId }
    }
    
    private fun generateLogHash(error: TransactionError): String {
        val data = "${error.id}${error.timestamp}${error.transactionId}${error.message}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    data class AuditLogEntry(
        val timestamp: Long,
        val type: LogEntryType,
        val errorId: String,
        val transactionId: String,
        val message: String,
        val hash: String
    )
    
    enum class LogEntryType {
        ERROR,
        RECOVERY,
        MANUAL_INTERVENTION
    }
} 