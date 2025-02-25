package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.AdminRole
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for managing role-based access control for manual interventions.
 * Implements secure role verification and access logging.
 */
@Singleton
class AccessControlService @Inject constructor(
    private val auditLogger: SecureAuditLogger
) {
    private val accessMutex = Mutex()
    private val adminRoles = mutableMapOf<String, Set<AdminRole>>()
    private val roleHierarchy = mapOf(
        AdminRole.SECURITY_ADMIN to setOf(
            AdminRole.SECURITY_ADMIN,
            AdminRole.SYSTEM_ADMIN,
            AdminRole.RECOVERY_SPECIALIST,
            AdminRole.SUPPORT_AGENT
        ),
        AdminRole.SYSTEM_ADMIN to setOf(
            AdminRole.SYSTEM_ADMIN,
            AdminRole.RECOVERY_SPECIALIST,
            AdminRole.SUPPORT_AGENT
        ),
        AdminRole.RECOVERY_SPECIALIST to setOf(
            AdminRole.RECOVERY_SPECIALIST,
            AdminRole.SUPPORT_AGENT
        ),
        AdminRole.SUPPORT_AGENT to setOf(
            AdminRole.SUPPORT_AGENT
        )
    )

    /**
     * Assigns roles to an admin
     */
    suspend fun assignRoles(adminId: String, roles: Set<AdminRole>) {
        accessMutex.withLock {
            adminRoles[adminId] = roles
            auditLogger.logEvent(
                "ROLES_ASSIGNED",
                "Roles assigned to admin",
                mapOf(
                    "admin_id" to adminId,
                    "roles" to roles.joinToString(",") { it.name },
                    "timestamp" to Instant.now().toString()
                )
            )
        }
    }

    /**
     * Removes roles from an admin
     */
    suspend fun removeRoles(adminId: String, roles: Set<AdminRole>) {
        accessMutex.withLock {
            val currentRoles = adminRoles[adminId] ?: emptySet()
            adminRoles[adminId] = currentRoles - roles
            auditLogger.logEvent(
                "ROLES_REMOVED",
                "Roles removed from admin",
                mapOf(
                    "admin_id" to adminId,
                    "removed_roles" to roles.joinToString(",") { it.name },
                    "remaining_roles" to (currentRoles - roles).joinToString(",") { it.name },
                    "timestamp" to Instant.now().toString()
                )
            )
        }
    }

    /**
     * Checks if an admin has a specific role or higher in the hierarchy
     */
    suspend fun hasRequiredRole(adminId: String, requiredRole: AdminRole): Boolean {
        return accessMutex.withLock {
            val adminRoleSet = adminRoles[adminId] ?: return@withLock false
            
            // Check if admin has any role that includes the required role in its hierarchy
            adminRoleSet.any { role ->
                roleHierarchy[role]?.contains(requiredRole) ?: false
            }.also { hasAccess ->
                auditLogger.logEvent(
                    "ACCESS_CHECK",
                    "Role access check performed",
                    mapOf(
                        "admin_id" to adminId,
                        "required_role" to requiredRole.name,
                        "has_access" to hasAccess.toString(),
                        "timestamp" to Instant.now().toString()
                    )
                )
            }
        }
    }

    /**
     * Gets all roles assigned to an admin
     */
    suspend fun getAdminRoles(adminId: String): Set<AdminRole> {
        return accessMutex.withLock {
            adminRoles[adminId] ?: emptySet()
        }
    }

    /**
     * Checks if an admin exists in the system
     */
    suspend fun adminExists(adminId: String): Boolean {
        return accessMutex.withLock {
            adminRoles.containsKey(adminId)
        }
    }

    /**
     * Lists all admins with a specific role
     */
    suspend fun listAdminsWithRole(role: AdminRole): List<String> {
        return accessMutex.withLock {
            adminRoles.entries
                .filter { (_, roles) -> roles.contains(role) }
                .map { it.key }
        }
    }
} 