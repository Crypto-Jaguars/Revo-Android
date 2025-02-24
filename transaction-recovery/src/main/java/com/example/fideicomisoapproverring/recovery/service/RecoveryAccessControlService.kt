package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.AdminRole
import com.example.fideicomisoapproverring.recovery.model.RecoveryOperation
import com.example.fideicomisoapproverring.recovery.model.RecoveryPermission
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for managing access control for recovery operations.
 * Implements role-based access control with fine-grained permissions for different recovery operations.
 */
@Singleton
class RecoveryAccessControlService @Inject constructor(
    private val auditLogger: SecureAuditLogger,
    private val accessControlService: AccessControlService,
    private val authenticationService: AuthenticationService
) {
    private val accessMutex = Mutex()
    private val operationPermissions = mutableMapOf<RecoveryOperation, Set<RecoveryPermission>>()
    
    init {
        // Initialize default permissions for recovery operations
        operationPermissions[RecoveryOperation.VIEW_STATUS] = setOf(
            RecoveryPermission.READ_BASIC,
            RecoveryPermission.READ_DETAILS
        )
        operationPermissions[RecoveryOperation.INITIATE_RECOVERY] = setOf(
            RecoveryPermission.INITIATE,
            RecoveryPermission.READ_BASIC,
            RecoveryPermission.READ_DETAILS
        )
        operationPermissions[RecoveryOperation.CANCEL_RECOVERY] = setOf(
            RecoveryPermission.CANCEL,
            RecoveryPermission.READ_BASIC
        )
        operationPermissions[RecoveryOperation.ROLLBACK_TRANSACTION] = setOf(
            RecoveryPermission.ROLLBACK,
            RecoveryPermission.READ_DETAILS
        )
        operationPermissions[RecoveryOperation.MANUAL_INTERVENTION] = setOf(
            RecoveryPermission.MANUAL_INTERVENTION,
            RecoveryPermission.READ_DETAILS
        )
    }

    /**
     * Verifies if an admin has permission to perform a recovery operation
     */
    suspend fun verifyAccess(
        adminId: String,
        sessionToken: String,
        operation: RecoveryOperation
    ): Boolean {
        return accessMutex.withLock {
            try {
                // Verify session is valid
                if (!authenticationService.validateSession(adminId, sessionToken)) {
                    auditLogger.logEvent(
                        "ACCESS_DENIED",
                        "Invalid session for recovery operation",
                        mapOf(
                            "admin_id" to adminId,
                            "operation" to operation.name,
                            "reason" to "Invalid session",
                            "timestamp" to Instant.now().toString()
                        )
                    )
                    return@withLock false
                }

                // Get required permissions for operation
                val requiredPermissions = operationPermissions[operation] ?: emptySet()
                
                // Get admin's role
                val adminRole = accessControlService.getAdminRoles(adminId).maxByOrNull { it.ordinal }
                    ?: return@withLock false
                
                // Check if admin's role has all required permissions
                val hasPermissions = requiredPermissions.all { permission ->
                    roleHasPermission(adminRole, permission)
                }

                if (!hasPermissions) {
                    auditLogger.logEvent(
                        "ACCESS_DENIED",
                        "Insufficient permissions for recovery operation",
                        mapOf(
                            "admin_id" to adminId,
                            "operation" to operation.name,
                            "admin_role" to adminRole.name,
                            "required_permissions" to requiredPermissions.joinToString(",") { it.name },
                            "timestamp" to Instant.now().toString()
                        )
                    )
                    return@withLock false
                }

                auditLogger.logEvent(
                    "ACCESS_GRANTED",
                    "Access granted for recovery operation",
                    mapOf(
                        "admin_id" to adminId,
                        "operation" to operation.name,
                        "admin_role" to adminRole.name,
                        "timestamp" to Instant.now().toString()
                    )
                )
                
                true
            } catch (e: Exception) {
                auditLogger.logEvent(
                    "ACCESS_CHECK_ERROR",
                    "Error checking access for recovery operation",
                    mapOf(
                        "admin_id" to adminId,
                        "operation" to operation.name,
                        "error" to e.message.toString(),
                        "timestamp" to Instant.now().toString()
                    )
                )
                false
            }
        }
    }

    /**
     * Checks if a role has a specific permission based on role hierarchy
     */
    private fun roleHasPermission(role: AdminRole, permission: RecoveryPermission): Boolean {
        return when (role) {
            AdminRole.SECURITY_ADMIN -> true // Has all permissions
            AdminRole.SYSTEM_ADMIN -> permission != RecoveryPermission.SECURITY_OVERRIDE
            AdminRole.RECOVERY_SPECIALIST -> when (permission) {
                RecoveryPermission.READ_BASIC,
                RecoveryPermission.READ_DETAILS,
                RecoveryPermission.INITIATE,
                RecoveryPermission.CANCEL,
                RecoveryPermission.MANUAL_INTERVENTION -> true
                else -> false
            }
            AdminRole.SUPPORT_AGENT -> when (permission) {
                RecoveryPermission.READ_BASIC,
                RecoveryPermission.READ_DETAILS -> true
                else -> false
            }
        }
    }

    /**
     * Updates permissions for a recovery operation
     */
    suspend fun updateOperationPermissions(
        adminId: String,
        operation: RecoveryOperation,
        permissions: Set<RecoveryPermission>
    ) {
        accessMutex.withLock {
            // Only SECURITY_ADMIN can modify permissions
            val adminRole = accessControlService.getAdminRoles(adminId).maxByOrNull { it.ordinal }
            if (adminRole != AdminRole.SECURITY_ADMIN) {
                throw UnauthorizedAccessException("Only Security Admins can modify operation permissions")
            }

            operationPermissions[operation] = permissions
            auditLogger.logEvent(
                "PERMISSIONS_UPDATED",
                "Updated permissions for recovery operation",
                mapOf(
                    "admin_id" to adminId,
                    "operation" to operation.name,
                    "permissions" to permissions.joinToString(",") { it.name },
                    "timestamp" to Instant.now().toString()
                )
            )
        }
    }

    /**
     * Gets the current permissions for a recovery operation
     */
    suspend fun getOperationPermissions(operation: RecoveryOperation): Set<RecoveryPermission> {
        return accessMutex.withLock {
            operationPermissions[operation] ?: emptySet()
        }
    }
} 