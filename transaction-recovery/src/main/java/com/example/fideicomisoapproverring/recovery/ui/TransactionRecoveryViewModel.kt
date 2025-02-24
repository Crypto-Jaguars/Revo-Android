package com.example.fideicomisoapproverring.recovery.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.forensics.TransactionForensicsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class RecoveryState(
    val error: TransactionError? = null,
    val status: RecoveryStatus = RecoveryStatus.ANALYZING,
    val attemptCount: Int = 0,
    val maxAttempts: Int = MAX_RECOVERY_ATTEMPTS
)

@HiltViewModel
class TransactionRecoveryViewModel @Inject constructor(
    private val recoveryService: TransactionRecoveryService,
    private val forensicsService: TransactionForensicsService,
    private val auditLogger: SecureAuditLogger
) : ViewModel() {

    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Analyzing)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

    private val _userActions = MutableSharedFlow<UserAction>()
    val userActions: SharedFlow<UserAction> = _userActions.asSharedFlow()

    private var currentTransactionId: String? = null
    private var recoveryAttempts = 0
    private val maxRecoveryAttempts = 3

    init {
        viewModelScope.launch {
            recoveryService.recoveryStatus.collect { statusMap ->
                val errorId = savedStateHandle.get<String>(ERROR_ID_KEY)
                errorId?.let { id ->
                    statusMap[id]?.let { status ->
                        _recoveryState.value = _recoveryState.value.copy(status = status)
                    }
                }
            }
        }
    }

    fun loadError(errorId: String) {
        savedStateHandle[ERROR_ID_KEY] = errorId
        viewModelScope.launch {
            val error = recoveryService.getError(errorId)
            _recoveryState.value = _recoveryState.value.copy(
                error = error,
                status = recoveryService.recoveryStatus.value[errorId] ?: RecoveryStatus.ANALYZING
            )
        }
    }

    fun startMonitoring(transactionId: String) {
        currentTransactionId = transactionId
        viewModelScope.launch {
            try {
                auditLogger.logEvent(
                    "RECOVERY_MONITORING_START",
                    "Started monitoring recovery for transaction: $transactionId",
                    mapOf(
                        "timestamp" to Instant.now().toString(),
                        "transaction_id" to transactionId
                    )
                )

                // Start forensics analysis
                val forensicsReport = forensicsService.analyzeTransaction(transactionId)
                
                when {
                    forensicsReport.isRecoverable -> {
                        _recoveryState.value = RecoveryState.Recovering(0)
                        startRecovery(transactionId)
                    }
                    forensicsReport.requiresUserAction -> {
                        _recoveryState.value = RecoveryState.RequiresAction(
                            determineRequiredAction(forensicsReport)
                        )
                    }
                    else -> {
                        _recoveryState.value = RecoveryState.Failed(forensicsReport.error)
                    }
                }

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun retryRecovery() {
        currentTransactionId?.let { transactionId ->
            if (recoveryAttempts < maxRecoveryAttempts) {
                recoveryAttempts++
                viewModelScope.launch {
                    try {
                        _recoveryState.value = RecoveryState.Recovering(0)
                        startRecovery(transactionId)
                    } catch (e: Exception) {
                        handleError(e)
                    }
                }
            } else {
                _recoveryState.value = RecoveryState.Failed(
                    TransactionError.MaxRetriesExceeded(
                        transactionId = transactionId,
                        message = "Maximum recovery attempts reached",
                        timestamp = Instant.now()
                    )
                )
            }
        }
    }

    fun cancelRecovery() {
        currentTransactionId?.let { transactionId ->
            viewModelScope.launch {
                try {
                    auditLogger.logEvent(
                        "RECOVERY_CANCELLED",
                        "Recovery cancelled for transaction: $transactionId",
                        mapOf(
                            "timestamp" to Instant.now().toString(),
                            "transaction_id" to transactionId,
                            "attempts" to recoveryAttempts.toString()
                        )
                    )
                    recoveryService.cancelRecovery(transactionId)
                    _recoveryState.value = RecoveryState.Cancelled
                } catch (e: Exception) {
                    handleError(e)
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
                        "Support requested for transaction: $transactionId",
                        mapOf(
                            "timestamp" to Instant.now().toString(),
                            "transaction_id" to transactionId,
                            "recovery_attempts" to recoveryAttempts.toString()
                        )
                    )
                    _userActions.emit(UserAction.RequestSupport)
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    private suspend fun startRecovery(transactionId: String) {
        recoveryService.startRecovery(transactionId)
            .catch { e -> handleError(e) }
            .collect { status ->
                when (status) {
                    is RecoveryStatus.InProgress -> {
                        _recoveryState.value = RecoveryState.Recovering(status.progress)
                    }
                    is RecoveryStatus.RequiresAction -> {
                        _recoveryState.value = RecoveryState.RequiresAction(
                            determineRequiredAction(status)
                        )
                    }
                    is RecoveryStatus.Completed -> {
                        _recoveryState.value = RecoveryState.Completed(status.result)
                        auditLogger.logEvent(
                            "RECOVERY_COMPLETED",
                            "Recovery completed for transaction: $transactionId",
                            mapOf(
                                "timestamp" to Instant.now().toString(),
                                "transaction_id" to transactionId,
                                "attempts" to recoveryAttempts.toString(),
                                "result" to status.result.toString()
                            )
                        )
                    }
                    is RecoveryStatus.Failed -> {
                        _recoveryState.value = RecoveryState.Failed(status.error)
                        auditLogger.logEvent(
                            "RECOVERY_FAILED",
                            "Recovery failed for transaction: $transactionId",
                            mapOf(
                                "timestamp" to Instant.now().toString(),
                                "transaction_id" to transactionId,
                                "attempts" to recoveryAttempts.toString(),
                                "error" to status.error.toString()
                            )
                        )
                    }
                }
            }
    }

    private fun determineRequiredAction(status: RecoveryStatus.RequiresAction): UserAction {
        return when (status.actionType) {
            ActionType.WALLET_VERIFICATION -> UserAction.VerifyWallet
            ActionType.ESCROW_VERIFICATION -> UserAction.VerifyEscrow
            ActionType.MANUAL_INTERVENTION -> UserAction.RequestSupport
            ActionType.RETRY -> UserAction.Retry
        }
    }

    private fun determineRequiredAction(report: ForensicsReport): UserAction {
        return when {
            report.error is WalletConnectionError -> UserAction.VerifyWallet
            report.error is EscrowError -> UserAction.VerifyEscrow
            report.error.severity == ErrorSeverity.HIGH -> UserAction.RequestSupport
            else -> UserAction.Retry
        }
    }

    private fun handleError(error: Throwable) {
        currentTransactionId?.let { transactionId ->
            auditLogger.logEvent(
                "RECOVERY_ERROR",
                "Error during recovery for transaction: $transactionId",
                mapOf(
                    "timestamp" to Instant.now().toString(),
                    "transaction_id" to transactionId,
                    "error" to error.message.toString(),
                    "attempts" to recoveryAttempts.toString()
                )
            )
            
            _recoveryState.value = RecoveryState.Failed(
                TransactionError.UnknownError(
                    transactionId = transactionId,
                    message = "An unexpected error occurred: ${error.message}",
                    timestamp = Instant.now()
                )
            )
        }
    }

    companion object {
        private const val ERROR_ID_KEY = "error_id"
        private const val MAX_RECOVERY_ATTEMPTS = 3
    }
}

sealed class RecoveryState {
    object Analyzing : RecoveryState()
    data class Recovering(val progress: Int) : RecoveryState()
    data class RequiresAction(val action: UserAction) : RecoveryState()
    data class Completed(val result: TransactionStatus) : RecoveryState()
    data class Failed(val error: TransactionError) : RecoveryState()
    object Cancelled : RecoveryState()
}

sealed class UserAction {
    object VerifyWallet : UserAction()
    object VerifyEscrow : UserAction()
    object RequestSupport : UserAction()
    object Retry : UserAction()
    object Cancel : UserAction()
} 