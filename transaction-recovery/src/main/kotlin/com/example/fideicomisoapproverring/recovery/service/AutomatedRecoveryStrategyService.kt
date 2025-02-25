package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.forensics.TransactionForensicsService
import com.example.fideicomisoapproverring.recovery.ml.TransactionErrorPredictor
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.pow

/**
 * Service responsible for implementing automated recovery strategies using ML-based decision making.
 * This service coordinates multiple recovery approaches and adapts strategies based on success rates.
 */
@Singleton
class AutomatedRecoveryStrategyService @Inject constructor(
    private val stellarTransactionManager: StellarTransactionManager,
    private val forensicsService: TransactionForensicsService,
    private val errorPredictor: TransactionErrorPredictor,
    private val recoveryService: TransactionRecoveryService,
    private val rollbackService: TransactionRollbackService,
    private val auditLogger: SecureAuditLogger
) {
    private val strategyMutex = Mutex()
    private val _recoveryStrategies = MutableStateFlow<Map<String, RecoveryStrategy>>(emptyMap())
    val recoveryStrategies: StateFlow<Map<String, RecoveryStrategy>> = _recoveryStrategies.asStateFlow()

    data class RecoveryStrategy(
        val transactionId: String,
        val error: TransactionError,
        val approaches: List<RecoveryApproach>,
        val currentApproach: RecoveryApproach,
        val attempts: Int = 0,
        val lastAttempt: Instant = Instant.now(),
        val successProbability: Float = 0f
    )

    data class RecoveryApproach(
        val type: TransactionRecoveryStrategy,
        val parameters: Map<String, Any> = emptyMap(),
        val successRate: Float = 0f,
        val averageRecoveryTime: Long = 0L
    )

    /**
     * Initiates automated recovery for a transaction error
     */
    suspend fun initiateRecovery(error: TransactionError): RecoveryResult {
        val transactionId = error.transactionId
        auditLogger.logEvent(
            "AUTOMATED_RECOVERY_INITIATED",
            "Starting automated recovery for transaction: $transactionId",
            mapOf("error_type" to (error::class.simpleName ?: "Unknown"))
        )

        return try {
            val forensicsReport = forensicsService.analyzeTransaction(transactionId)
            val prediction = errorPredictor.predictErrors(transactionId)
            val strategy = determineRecoveryStrategy(error, forensicsReport, prediction)
            
            executeRecoveryStrategy(strategy)
        } catch (e: Exception) {
            handleRecoveryError(error, e)
        }
    }

    /**
     * Determines the optimal recovery strategy based on ML predictions and forensics
     */
    private suspend fun determineRecoveryStrategy(
        error: TransactionError,
        forensicsReport: ForensicsReport,
        prediction: ErrorPrediction
    ): RecoveryStrategy {
        val approaches = when (error) {
            is TransactionError.NetworkCongestionError -> listOf(
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.WAIT_AND_RETRY,
                    parameters = mapOf(
                        "initial_delay" to 5000L,
                        "max_delay" to 30000L,
                        "backoff_factor" to 1.5f
                    )
                ),
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.RETRY,
                    parameters = mapOf(
                        "max_attempts" to 3
                    )
                )
            )
            is TransactionError.SmartContractError -> listOf(
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.COMPENSATING_ACTION,
                    parameters = mapOf(
                        "verify_state" to true,
                        "revert_changes" to true
                    )
                ),
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.ROLLBACK,
                    parameters = mapOf(
                        "preserve_state" to true
                    )
                )
            )
            is TransactionError.BlockchainError,
            is TransactionError.EscrowError,
            is TransactionError.InsufficientFundsError -> listOf(
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.PARTIAL_ROLLBACK,
                    parameters = mapOf(
                        "target_state" to "ESCROW_INITIALIZED"
                    )
                ),
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.MANUAL_RESOLUTION
                )
            )
            else -> listOf(
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.RETRY
                ),
                RecoveryApproach(
                    type = TransactionRecoveryStrategy.ROLLBACK
                )
            )
        }

        val successProbability = calculateSuccessProbability(
            error,
            forensicsReport,
            prediction,
            approaches
        )

        return RecoveryStrategy(
            transactionId = error.transactionId,
            error = error,
            approaches = approaches,
            currentApproach = selectBestApproach(approaches, prediction),
            successProbability = successProbability
        )
    }

    /**
     * Executes the selected recovery strategy
     */
    private suspend fun executeRecoveryStrategy(strategy: RecoveryStrategy): RecoveryResult {
        strategyMutex.withLock {
            _recoveryStrategies.value = _recoveryStrategies.value + (strategy.transactionId to strategy)
        }

        return try {
            val result = when (strategy.currentApproach.type) {
                TransactionRecoveryStrategy.RETRY -> executeRetryStrategy(strategy)
                TransactionRecoveryStrategy.ROLLBACK -> executeRollbackStrategy(strategy)
                TransactionRecoveryStrategy.PARTIAL_ROLLBACK -> executePartialRollbackStrategy(strategy)
                TransactionRecoveryStrategy.COMPENSATING_ACTION -> executeCompensatingAction(strategy)
                TransactionRecoveryStrategy.WAIT_AND_RETRY -> executeWaitAndRetryStrategy(strategy)
                TransactionRecoveryStrategy.MANUAL_RESOLUTION -> escalateToManualResolution(strategy)
                TransactionRecoveryStrategy.ESCALATE -> escalateToSystemAdministrators(strategy)
            }

            updateStrategySuccess(strategy, result)
            result
        } catch (e: Exception) {
            handleStrategyExecutionError(strategy, e)
        }
    }

    private suspend fun executeRetryStrategy(strategy: RecoveryStrategy): RecoveryResult {
        val maxAttempts = strategy.currentApproach.parameters["max_attempts"] as? Int ?: 3
        
        return if (strategy.attempts < maxAttempts) {
            recoveryService.attemptRecovery(strategy.error)
        } else {
            RecoveryResult(
                status = TransactionErrorStatus.FAILED,
                message = "Maximum retry attempts exceeded",
                error = strategy.error
            )
        }
    }

    private suspend fun executeRollbackStrategy(strategy: RecoveryStrategy): RecoveryResult {
        return rollbackService.initiateRollback(strategy.transactionId, strategy.error)
    }

    private suspend fun executePartialRollbackStrategy(strategy: RecoveryStrategy): RecoveryResult {
        val targetState = strategy.currentApproach.parameters["target_state"] as? String ?: "INITIAL"
        return rollbackService.initiatePartialRollback(
            transactionId = strategy.transactionId,
            targetState = targetState,
            error = strategy.error
        )
    }

    private suspend fun executeCompensatingAction(strategy: RecoveryStrategy): RecoveryResult {
        val verifyState = strategy.currentApproach.parameters["verify_state"] as? Boolean ?: true
        val revertChanges = strategy.currentApproach.parameters["revert_changes"] as? Boolean ?: true

        return recoveryService.executeCompensatingAction(
            error = strategy.error,
            verifyState = verifyState,
            revertChanges = revertChanges
        )
    }

    private suspend fun executeWaitAndRetryStrategy(strategy: RecoveryStrategy): RecoveryResult {
        val initialDelay = strategy.currentApproach.parameters["initial_delay"] as? Long ?: 5000L
        val maxDelay = strategy.currentApproach.parameters["max_delay"] as? Long ?: 30000L
        val backoffFactor = strategy.currentApproach.parameters["backoff_factor"] as? Float ?: 1.5f

        val delay = calculateBackoffDelay(
            strategy.attempts,
            initialDelay,
            maxDelay,
            backoffFactor
        )

        kotlinx.coroutines.delay(delay)
        return recoveryService.retryWithDelay(strategy.error, delay)
    }

    private suspend fun escalateToManualResolution(strategy: RecoveryStrategy): RecoveryResult {
        val ticketId = recoveryService.requestManualIntervention(
            transactionId = strategy.transactionId,
            error = strategy.error,
            reason = "Automated recovery strategies exhausted"
        )

        return RecoveryResult(
            status = TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED,
            message = "Escalated to manual resolution. Ticket: $ticketId",
            error = strategy.error,
            recoveryDetails = mapOf("ticket_id" to ticketId)
        )
    }

    private suspend fun escalateToSystemAdministrators(strategy: RecoveryStrategy): RecoveryResult {
        auditLogger.logEvent(
            "RECOVERY_ESCALATED",
            "Recovery escalated to system administrators",
            mapOf(
                "transaction_id" to strategy.transactionId,
                "error_type" to (strategy.error::class.simpleName ?: "Unknown"),
                "attempts" to strategy.attempts.toString()
            )
        )

        return RecoveryResult(
            status = TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED,
            message = "Escalated to system administrators",
            error = strategy.error
        )
    }

    private fun calculateBackoffDelay(
        attempts: Int,
        initialDelay: Long,
        maxDelay: Long,
        backoffFactor: Float
    ): Long {
        val delay = (initialDelay * backoffFactor.pow(attempts)).toLong()
        return delay.coerceAtMost(maxDelay)
    }

    private suspend fun calculateSuccessProbability(
        error: TransactionError,
        forensicsReport: ForensicsReport,
        prediction: ErrorPrediction,
        approaches: List<RecoveryApproach>
    ): Float {
        val baseProb = when (error.severity) {
            ErrorSeverity.CRITICAL -> 0.1f
            ErrorSeverity.HIGH -> 0.3f
            ErrorSeverity.MEDIUM -> 0.6f
            ErrorSeverity.LOW -> 0.8f
        }

        val networkFactor = when (forensicsReport.systemState.networkStatus) {
            NetworkStatus.HEALTHY -> 1.0f
            NetworkStatus.CONGESTED -> 0.7f
            NetworkStatus.DEGRADED -> 0.5f
            NetworkStatus.OFFLINE -> 0.1f
        }

        val predictionFactor = 1.0f - prediction.probability

        return (baseProb * networkFactor * predictionFactor).coerceIn(0f, 1f)
    }

    private fun selectBestApproach(
        approaches: List<RecoveryApproach>,
        prediction: ErrorPrediction
    ): RecoveryApproach {
        return approaches.maxByOrNull { approach ->
            approach.successRate * (1.0f - prediction.probability)
        } ?: approaches.first()
    }

    private suspend fun updateStrategySuccess(
        strategy: RecoveryStrategy,
        result: RecoveryResult
    ) {
        val updatedStrategy = strategy.copy(
            attempts = strategy.attempts + 1,
            lastAttempt = Instant.now(),
            successProbability = if (result.status == TransactionErrorStatus.RECOVERED) 1f else 0f
        )

        strategyMutex.withLock {
            _recoveryStrategies.value = _recoveryStrategies.value + (strategy.transactionId to updatedStrategy)
        }
    }

    private suspend fun handleRecoveryError(
        error: TransactionError,
        exception: Exception
    ): RecoveryResult {
        auditLogger.logEvent(
            "AUTOMATED_RECOVERY_ERROR",
            "Error during automated recovery",
            mapOf(
                "transaction_id" to error.transactionId,
                "error_type" to (error::class.simpleName ?: "Unknown"),
                "exception" to (exception.message ?: "Unknown error")
            )
        )

        return RecoveryResult(
            status = TransactionErrorStatus.FAILED,
            message = "Automated recovery failed: ${exception.message}",
            error = error
        )
    }

    private suspend fun handleStrategyExecutionError(
        strategy: RecoveryStrategy,
        exception: Exception
    ): RecoveryResult {
        auditLogger.logEvent(
            "STRATEGY_EXECUTION_ERROR",
            "Error executing recovery strategy",
            mapOf(
                "transaction_id" to strategy.transactionId,
                "strategy_type" to strategy.currentApproach.type.name,
                "exception" to (exception.message ?: "Unknown error")
            )
        )

        return RecoveryResult(
            status = TransactionErrorStatus.FAILED,
            message = "Strategy execution failed: ${exception.message}",
            error = strategy.error
        )
    }

    companion object {
        private const val MAX_RECOVERY_ATTEMPTS = 5
        private const val MIN_SUCCESS_PROBABILITY = 0.2f
    }
} 