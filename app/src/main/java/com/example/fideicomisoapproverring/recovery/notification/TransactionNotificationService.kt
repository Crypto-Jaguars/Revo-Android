package com.example.fideicomisoapproverring.recovery.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.RecoveryStatus
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.recovery.ui.RecoveryStatusActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auditLogger: SecureAuditLogger
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        private const val CHANNEL_ID_TRANSACTION = "transaction_status"
        private const val CHANNEL_ID_RECOVERY = "recovery_status"
        private const val NOTIFICATION_GROUP_TRANSACTIONS = "group_transactions"
        
        private const val NOTIFICATION_ID_OFFSET = 1000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val transactionChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTION,
                "Transaction Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Updates about transaction status and progress"
                enableVibration(true)
                setShowBadge(true)
            }

            val recoveryChannel = NotificationChannel(
                CHANNEL_ID_RECOVERY,
                "Recovery Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical updates about transaction recovery"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(transactionChannel, recoveryChannel))
        }
    }

    fun showTransactionStatusNotification(
        transactionId: String,
        status: RecoveryStatus,
        error: TransactionError? = null
    ) {
        val notificationId = generateNotificationId(transactionId)
        val contentIntent = createContentIntent(transactionId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSACTION)
            .setSmallIcon(R.drawable.ic_transaction)
            .setContentTitle(getStatusTitle(status))
            .setContentText(getStatusMessage(status, error))
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP_TRANSACTIONS)
            .setContentIntent(contentIntent)
            .setPriority(getNotificationPriority(status))
            .build()

        try {
            notificationManager.notify(notificationId, notification)
            auditLogger.logEvent(
                "NOTIFICATION_SHOWN",
                "Transaction status notification displayed",
                mapOf(
                    "transaction_id" to transactionId,
                    "status" to status.toString(),
                    "error" to (error?.message ?: "none")
                )
            )
        } catch (e: SecurityException) {
            auditLogger.logEvent(
                "NOTIFICATION_ERROR",
                "Failed to show notification: ${e.message}",
                mapOf("transaction_id" to transactionId)
            )
        }
    }

    fun showRecoveryActionNotification(
        transactionId: String,
        action: String,
        isUrgent: Boolean = false
    ) {
        val notificationId = generateNotificationId(transactionId) + 1
        val contentIntent = createContentIntent(transactionId)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RECOVERY)
            .setSmallIcon(R.drawable.ic_recovery)
            .setContentTitle("Action Required")
            .setContentText(action)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP_TRANSACTIONS)
            .setContentIntent(contentIntent)
            .setPriority(if (isUrgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
            auditLogger.logEvent(
                "ACTION_NOTIFICATION_SHOWN",
                "Recovery action notification displayed",
                mapOf(
                    "transaction_id" to transactionId,
                    "action" to action,
                    "urgent" to isUrgent.toString()
                )
            )
        } catch (e: SecurityException) {
            auditLogger.logEvent(
                "NOTIFICATION_ERROR",
                "Failed to show action notification: ${e.message}",
                mapOf("transaction_id" to transactionId)
            )
        }
    }

    fun cancelNotifications(transactionId: String) {
        val notificationId = generateNotificationId(transactionId)
        notificationManager.cancel(notificationId)
        notificationManager.cancel(notificationId + 1)
        
        auditLogger.logEvent(
            "NOTIFICATIONS_CANCELLED",
            "Cancelled notifications for transaction",
            mapOf("transaction_id" to transactionId)
        )
    }

    private fun createContentIntent(transactionId: String): PendingIntent {
        val intent = Intent(context, RecoveryStatusActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(RecoveryStatusActivity.EXTRA_TRANSACTION_ID, transactionId)
        }

        return PendingIntent.getActivity(
            context,
            generateNotificationId(transactionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStatusTitle(status: RecoveryStatus): String = when (status) {
        is RecoveryStatus.Analyzing -> "Analyzing Transaction"
        is RecoveryStatus.InProgress -> "Recovery in Progress"
        is RecoveryStatus.RequiresAction -> "Action Required"
        is RecoveryStatus.Completed -> "Recovery Completed"
        is RecoveryStatus.Failed -> "Recovery Failed"
    }

    private fun getStatusMessage(status: RecoveryStatus, error: TransactionError?): String = when (status) {
        is RecoveryStatus.Analyzing -> "Analyzing transaction status..."
        is RecoveryStatus.InProgress -> "Recovery in progress: ${status.progress}%"
        is RecoveryStatus.RequiresAction -> "Action needed: ${status.message}"
        is RecoveryStatus.Completed -> "Transaction has been recovered successfully"
        is RecoveryStatus.Failed -> error?.message ?: "Recovery failed"
    }

    private fun getNotificationPriority(status: RecoveryStatus): Int = when (status) {
        is RecoveryStatus.RequiresAction,
        is RecoveryStatus.Failed -> NotificationCompat.PRIORITY_HIGH
        else -> NotificationCompat.PRIORITY_DEFAULT
    }

    private fun generateNotificationId(transactionId: String): Int {
        return NOTIFICATION_ID_OFFSET + transactionId.hashCode()
    }
} 