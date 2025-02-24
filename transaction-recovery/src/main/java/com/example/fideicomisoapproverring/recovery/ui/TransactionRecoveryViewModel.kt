package com.example.fideicomisoapproverring.recovery.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fideicomisoapproverring.recovery.model.RecoveryResult
import com.example.fideicomisoapproverring.recovery.model.RecoveryStatus
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.recovery.model.TransactionErrorStatus
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _recoveryState = MutableStateFlow(RecoveryState())
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()

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

    fun retryRecovery() {
        val errorId = savedStateHandle.get<String>(ERROR_ID_KEY) ?: return
        viewModelScope.launch {
            recoveryService.retryRecovery(errorId)
        }
    }

    fun cancelRecovery() {
        val errorId = savedStateHandle.get<String>(ERROR_ID_KEY)
        errorId?.let { id ->
            recoveryService.cancelRecovery(id)
            _recoveryState.value = _recoveryState.value.copy(
                status = RecoveryStatus.FAILED,
                attemptCount = 0,
                maxAttempts = 0
            )
        }
    }

    private fun updateState(result: RecoveryResult) {
        _recoveryState.update { currentState ->
            currentState.copy(
                error = result.error,
                status = when (result.status) {
                    TransactionErrorStatus.ANALYZING -> RecoveryStatus.ANALYZING
                    TransactionErrorStatus.RECOVERING -> RecoveryStatus.ATTEMPTING
                    TransactionErrorStatus.RECOVERED -> RecoveryStatus.SUCCEEDED
                    TransactionErrorStatus.FAILED -> RecoveryStatus.FAILED
                    TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED -> RecoveryStatus.MANUAL_INTERVENTION_REQUIRED
                    TransactionErrorStatus.NOT_FOUND -> RecoveryStatus.FAILED
                },
                attemptCount = currentState.attemptCount + 1,
                maxAttempts = currentState.maxAttempts
            )
        }
    }

    companion object {
        private const val ERROR_ID_KEY = "error_id"
        private const val MAX_RECOVERY_ATTEMPTS = 3
    }
} 