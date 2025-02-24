package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.recovery.model.RecoveryResult
import com.example.fideicomisoapproverring.recovery.model.RecoveryStatus
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface TransactionRecoveryService {
    val _recoveryStatus: MutableStateFlow<Map<String, RecoveryStatus>>
    val recoveryStatus: StateFlow<Map<String, RecoveryStatus>>
    
    suspend fun reportError(error: TransactionError): String
    
    suspend fun attemptRecovery(error: TransactionError): RecoveryResult
    
    suspend fun getRecoveryStatus(errorId: String): RecoveryResult
    
    suspend fun validateTransactionIntegrity(transactionId: String): Boolean
    
    suspend fun rollbackTransaction(transactionId: String): RecoveryResult
    
    fun getActiveRecoveries(): Flow<List<TransactionError>>
    
    suspend fun requestManualIntervention(error: TransactionError, reason: String): String
    
    fun initiateRecovery(error: TransactionError)
    
    fun cancelRecovery(errorId: String)
} 