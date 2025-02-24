package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for handling secure manual intervention in transaction recovery.
 * Implements role-based access control and comprehensive audit logging.
 */
@Singleton
class SecureManualInterventionService @Inject constructor(
    private val auditLogger: SecureAuditLogger,
    private val recoveryService: TransactionRecoveryService,
    private val authenticationService: AuthenticationService,
    private val accessControlService: AccessControlService
) {
    private val interventionMutex = Mutex()
    private val _interventionRequests = MutableStateFlow<Map<String, InterventionRequest>>(emptyMap())
    val interventionRequests: StateFlow<Map<String, InterventionRequest>> = _interventionRequests.asStateFlow()

    /**
     * Creates a new manual intervention request with secure access controls
     */
    suspend fun createInterventionRequest(
        error: TransactionError,
        reason: String,
        requiredRole: AdminRole,
        priority: InterventionPriority = InterventionPriority.MEDIUM
    ): String {
        interventionMutex.withLock {
            val ticketId = generateSecureTicketId()
            val request = InterventionRequest(
                ticketId = ticketId,
                transactionId = error.transactionId,
                errorId = error.id,
                reason = reason,
                requiredRole = requiredRole,
                priority = priority,
                status = InterventionStatus.PENDING,
                createdAt = Instant.now(),
                lastUpdated = Instant.now()
            )
            
            _interventionRequests.value = _interventionRequests.value + (ticketId to request)
            
            auditLogger.logEvent(
                "MANUAL_INTERVENTION_REQUESTED",
                "Manual intervention request created",
                mapOf(
                    "ticket_id" to ticketId,
                    "transaction_id" to error.transactionId,
                    "error_id" to error.id,
                    "required_role" to requiredRole.name,
                    "priority" to priority.name
                )
            )
            
            return ticketId
        }
    }

    /**
     * Approves a manual intervention request with role verification
     */
    suspend fun approveIntervention(
        ticketId: String,
        adminId: String,
        notes: String
    ): InterventionResult {
        interventionMutex.withLock {
            val request = _interventionRequests.value[ticketId] ?: throw IllegalArgumentException("Invalid ticket ID")
            
            // Verify admin has required role
            if (!accessControlService.hasRequiredRole(adminId, request.requiredRole)) {
                auditLogger.logEvent(
                    "MANUAL_INTERVENTION_UNAUTHORIZED",
                    "Unauthorized intervention attempt",
                    mapOf(
                        "ticket_id" to ticketId,
                        "admin_id" to adminId,
                        "required_role" to request.requiredRole.name
                    )
                )
                throw UnauthorizedAccessException("Insufficient privileges for this intervention")
            }
            
            // Update request status
            val updatedRequest = request.copy(
                status = InterventionStatus.APPROVED,
                lastUpdated = Instant.now(),
                approvedBy = adminId,
                approvalNotes = notes
            )
            _interventionRequests.value = _interventionRequests.value + (ticketId to updatedRequest)
            
            auditLogger.logEvent(
                "MANUAL_INTERVENTION_APPROVED",
                "Manual intervention request approved",
                mapOf(
                    "ticket_id" to ticketId,
                    "admin_id" to adminId,
                    "transaction_id" to request.transactionId,
                    "notes" to notes
                )
            )
            
            return InterventionResult(
                ticketId = ticketId,
                status = InterventionStatus.APPROVED,
                message = "Intervention approved by admin: $adminId"
            )
        }
    }

    /**
     * Rejects a manual intervention request
     */
    suspend fun rejectIntervention(
        ticketId: String,
        adminId: String,
        reason: String
    ): InterventionResult {
        interventionMutex.withLock {
            val request = _interventionRequests.value[ticketId] ?: throw IllegalArgumentException("Invalid ticket ID")
            
            // Verify admin has required role
            if (!accessControlService.hasRequiredRole(adminId, request.requiredRole)) {
                auditLogger.logEvent(
                    "MANUAL_INTERVENTION_UNAUTHORIZED",
                    "Unauthorized intervention attempt",
                    mapOf(
                        "ticket_id" to ticketId,
                        "admin_id" to adminId,
                        "required_role" to request.requiredRole.name
                    )
                )
                throw UnauthorizedAccessException("Insufficient privileges for this intervention")
            }
            
            // Update request status
            val updatedRequest = request.copy(
                status = InterventionStatus.REJECTED,
                lastUpdated = Instant.now(),
                rejectedBy = adminId,
                rejectionReason = reason
            )
            _interventionRequests.value = _interventionRequests.value + (ticketId to updatedRequest)
            
            auditLogger.logEvent(
                "MANUAL_INTERVENTION_REJECTED",
                "Manual intervention request rejected",
                mapOf(
                    "ticket_id" to ticketId,
                    "admin_id" to adminId,
                    "transaction_id" to request.transactionId,
                    "reason" to reason
                )
            )
            
            return InterventionResult(
                ticketId = ticketId,
                status = InterventionStatus.REJECTED,
                message = "Intervention rejected by admin: $adminId"
            )
        }
    }

    /**
     * Retrieves the current status of an intervention request
     */
    suspend fun getInterventionStatus(ticketId: String): InterventionRequest? {
        return _interventionRequests.value[ticketId]
    }

    /**
     * Lists all active intervention requests for a specific role
     */
    suspend fun listInterventionRequests(adminId: String, role: AdminRole): List<InterventionRequest> {
        if (!accessControlService.hasRequiredRole(adminId, role)) {
            throw UnauthorizedAccessException("Insufficient privileges to list interventions")
        }
        
        return _interventionRequests.value.values
            .filter { it.status == InterventionStatus.PENDING && it.requiredRole == role }
            .sortedByDescending { it.priority }
    }

    private fun generateSecureTicketId(): String {
        return "MI-${java.util.UUID.randomUUID().toString().substring(0, 8)}"
    }
}

/**
 * Represents a manual intervention request
 */
data class InterventionRequest(
    val ticketId: String,
    val transactionId: String,
    val errorId: String,
    val reason: String,
    val requiredRole: AdminRole,
    val priority: InterventionPriority,
    val status: InterventionStatus,
    val createdAt: Instant,
    val lastUpdated: Instant,
    val approvedBy: String? = null,
    val approvalNotes: String? = null,
    val rejectedBy: String? = null,
    val rejectionReason: String? = null
)

/**
 * Result of an intervention operation
 */
data class InterventionResult(
    val ticketId: String,
    val status: InterventionStatus,
    val message: String
)

/**
 * Priority levels for intervention requests
 */
enum class InterventionPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Status of an intervention request
 */
enum class InterventionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED,
    FAILED
}

/**
 * Admin roles for manual intervention
 */
enum class AdminRole {
    SUPPORT_AGENT,
    RECOVERY_SPECIALIST,
    SYSTEM_ADMIN,
    SECURITY_ADMIN
}

class UnauthorizedAccessException(message: String) : Exception(message) 