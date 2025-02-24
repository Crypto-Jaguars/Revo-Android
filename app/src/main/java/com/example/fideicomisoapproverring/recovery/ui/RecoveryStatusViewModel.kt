package com.example.fideicomisoapproverring.recovery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.service.TransactionForensicsService
import com.example.fideicomisoapproverring.recovery.service.TransactionNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryStatusViewModel @Inject constructor(
    private val recoveryService: TransactionRecoveryService,
    private val forensicsService: TransactionForensicsService,
    private val auditLogger: SecureAuditLogger,
    private val notificationService: TransactionNotificationService
) : ViewModel() {

    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Analyzing)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

    private val _userActions = MutableSharedFlow<UserAction>()
    val userActions: SharedFlow<UserAction> = _userActions.asSharedFlow()

    private var currentTransactionId: String? = null

    fun startMonitoring(transactionId: String) {
        currentTransactionId = transactionId
        viewModelScope.launch {
            try {
                auditLogger.logEvent(
                    "RECOVERY_MONITORING_START",
                    "Started monitoring recovery for transaction: $transactionId"
                )

                // Start forensics analysis
                val forensicsReport = forensicsService.analyzeTransaction(transactionId)
                
                when {
                    forensicsReport.isRecoverable -> {
                        _recoveryState.value = RecoveryState.Recovering(0)
                        notificationService.showTransactionStatusNotification(
                            transactionId,
                            RecoveryStatus.InProgress(0, "Starting recovery")
                        )
                        startRecovery(transactionId)
                    }
                    forensicsReport.requiresUserAction -> {
                        val action = determineRequiredAction(forensicsReport)
                        _recoveryState.value = RecoveryState.RequiresAction(action)
                        notificationService.showRecoveryActionNotification(
                            transactionId,
                            getActionMessage(action),
                            isUrgent = forensicsReport.error.severity == ErrorSeverity.HIGH
                        )
                    }
                    else -> {
                        _recoveryState.value = RecoveryState.Failed(forensicsReport.error)
                        notificationService.showTransactionStatusNotification(
                            transactionId,
                            RecoveryStatus.Failed(forensicsReport.error, 0, false),
                            forensicsReport.error
                        )
                    }
                }

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun startRecovery(transactionId: String) {
        viewModelScope.launch {
            recoveryService.startRecovery(transactionId)
                .catch { e -> handleError(e) }
                .collect { status ->
                    when (status) {
                        is RecoveryStatus.InProgress -> {
                            _recoveryState.value = RecoveryState.Recovering(status.progress)
                            notificationService.showTransactionStatusNotification(
                                transactionId,
                                status
                            )
                        }
                        is RecoveryStatus.RequiresAction -> {
                            val action = determineRequiredAction(status)
                            _recoveryState.value = RecoveryState.RequiresAction(action)
                            notificationService.showRecoveryActionNotification(
                                transactionId,
                                getActionMessage(action),
                                isUrgent = true
                            )
                        }
                        is RecoveryStatus.Completed -> {
                            _recoveryState.value = RecoveryState.Completed(status.result)
                            notificationService.showTransactionStatusNotification(
                                transactionId,
                                status
                            )
                            notificationService.cancelNotifications(transactionId)
                        }
                        is RecoveryStatus.Failed -> {
                            _recoveryState.value = RecoveryState.Failed(status.error)
                            notificationService.showTransactionStatusNotification(
                                transactionId,
                                status,
                                status.error
                            )
                        }
                    }
                }
        }
    }

    private fun getActionMessage(action: UserAction): String = when (action) {
        is UserAction.AddFunds -> "Additional funds required: ${action.required} ${action.currency}"
        is UserAction.RequestSupport -> "Manual support intervention required"
        is UserAction.ReconnectWallet -> "Please reconnect your wallet"
        is UserAction.WaitForNetwork -> "Network congestion detected, please wait"
        is UserAction.VerifyEscrow -> "Escrow verification required"
        is UserAction.Retry -> "Please retry the transaction"
        else -> "Action required for transaction recovery"
    }

    fun retryRecovery() {
        currentTransactionId?.let { transactionId ->
            viewModelScope.launch {
                try {
                    _recoveryState.value = RecoveryState.Analyzing
                    auditLogger.logEvent(
                        "RECOVERY_RETRY",
                        "Retrying recovery for transaction: $transactionId"
                    )
                    // Reset state and retry
                    startMonitoring(transactionId)
                } catch (e: Exception) {
                    auditLogger.logEvent(
                        "RECOVERY_RETRY_ERROR",
                        "Error retrying recovery: ${e.message}",
                        mapOf("transaction_id" to transactionId)
                    )
                    _recoveryState.value = RecoveryState.Failed(
                        UnknownError(
                            transactionId = transactionId,
                            message = "Failed to retry recovery: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    fun requestSupport() {
        currentTransactionId?.let { transactionId ->
            viewModelScope.launch {
                try {
                    auditLogger.logEvent(
                        "SUPPORT_REQUESTED",
                        "Support requested for transaction: $transactionId"
                    )
                    _userActions.emit(UserAction.RequestSupport)
                } catch (e: Exception) {
                    auditLogger.logEvent(
                        "SUPPORT_REQUEST_ERROR",
                        "Error requesting support: ${e.message}",
                        mapOf("transaction_id" to transactionId)
                    )
                }
            }
        }
    }

    fun cancelRecovery() {
        currentTransactionId?.let { transactionId ->
            viewModelScope.launch {
                try {
                    auditLogger.logEvent(
                        "RECOVERY_CANCELLED",
                        "Recovery cancelled for transaction: $transactionId"
                    )
                    // Stop monitoring and clean up
                    stopMonitoring()
                } catch (e: Exception) {
                    auditLogger.logEvent(
                        "RECOVERY_CANCEL_ERROR",
                        "Error cancelling recovery: ${e.message}",
                        mapOf("transaction_id" to transactionId)
                    )
                }
            }
        }
    }

    private fun stopMonitoring() {
        currentTransactionId = null
        _recoveryState.value = RecoveryState.Analyzing
    }

    private suspend fun handleRecoveryState(state: RecoveryState) {
        when (state) {
            is RecoveryState.RequiresAction -> {
                when (state.action) {
                    is UserAction.AddFunds -> {
                        _userActions.emit(state.action)
                        auditLogger.logEvent(
                            "FUNDS_REQUIRED",
                            "Additional funds required",
                            mapOf(
                                "required" to state.action.required,
                                "available" to state.action.available,
                                "currency" to state.action.currency
                            )
                        )
                    }
                    is UserAction.RequestSupport -> {
                        _userActions.emit(state.action)
                        auditLogger.logEvent(
                            "SUPPORT_NEEDED",
                            "Manual support intervention required"
                        )
                    }
                    is UserAction.ReconnectWallet -> {
                        _userActions.emit(state.action)
                        auditLogger.logEvent(
                            "WALLET_RECONNECT_REQUIRED",
                            "Wallet reconnection required"
                        )
                    }
                    else -> {
                        _userActions.emit(state.action)
                        auditLogger.logEvent(
                            "USER_ACTION_REQUIRED",
                            "User action required: ${state.action::class.simpleName}"
                        )
                    }
                }
            }
            is RecoveryState.Failed -> {
                auditLogger.logEvent(
                    "RECOVERY_FAILED",
                    "Recovery failed",
                    mapOf(
                        "error_type" to state.error::class.simpleName,
                        "error_message" to state.error.message
                    )
                )
            }
            is RecoveryState.Completed -> {
                auditLogger.logEvent(
                    "RECOVERY_COMPLETED",
                    "Recovery completed successfully"
                )
            }
            else -> {
                // Other states don't require special handling
            }
        }
    }

    private fun handleError(e: Exception) {
        auditLogger.logEvent(
            "RECOVERY_MONITORING_ERROR",
            "Error monitoring recovery: ${e.message}",
            emptyMap()
        )
        _recoveryState.value = RecoveryState.Failed(
            UnknownError(
                message = "Failed to monitor recovery: ${e.message}"
            )
        )
    }

    private fun determineRequiredAction(forensicsReport: TransactionForensicsReport): UserAction {
        // Implementation of determineRequiredAction method
        // This is a placeholder and should be replaced with the actual implementation
        return UserAction.RequestSupport
    }
} 