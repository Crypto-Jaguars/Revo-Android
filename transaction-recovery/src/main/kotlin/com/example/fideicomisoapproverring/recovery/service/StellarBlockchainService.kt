package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.forensics.BlockchainState
import com.example.fideicomisoapproverring.stellar.model.NetworkState
import com.example.fideicomisoapproverring.stellar.model.NetworkQuality
import org.stellar.sdk.Server
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.requests.EventListener
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Optional

@Singleton
class StellarBlockchainService @Inject constructor(
    private val server: Server,
    private val auditLogger: SecureAuditLogger
) : BlockchainService {

    private val _blockchainEvents = MutableSharedFlow<BlockchainEvent>()
    private val blockchainEvents: SharedFlow<BlockchainEvent> = _blockchainEvents.asSharedFlow()

    override suspend fun getTransactionError(transactionId: String): TransactionError? {
        return try {
            val transaction = server.transactions().transaction(transactionId)
            if (!transaction.successful) {
                createTransactionError(transaction)
            } else {
                null
            }
        } catch (e: Exception) {
            auditLogger.logEvent(
                "BLOCKCHAIN_ERROR",
                "Failed to get transaction error",
                mapOf(
                    "transaction_id" to transactionId,
                    "error" to (e.message ?: "Unknown error")
                )
            )
            TransactionError.UnknownError(
                transactionId = transactionId,
                message = "Failed to retrieve transaction: ${e.message}",
                timestamp = Instant.now()
            )
        }
    }

    override suspend fun getBlockchainState(): BlockchainState {
        val ledger = server.ledgers().order(RequestBuilder.Order.DESC).limit(1).execute()
        val latestLedger = ledger.records[0]
        
        return BlockchainState(
            lastBlockHeight = latestLedger.sequence,
            averageBlockTime = calculateAverageBlockTime(),
            networkCongestion = calculateNetworkCongestion(),
            gasPrice = latestLedger.baseFeeInStroops.toString()
        )
    }

    override suspend fun getNetworkState(): NetworkState {
        val health = server.root()
        val ledger = server.ledgers().order(RequestBuilder.Order.DESC).limit(1).execute()
        val latestLedger = ledger.records[0]
        
        return NetworkState(
            congestion = calculateNetworkCongestion(),
            averageTransactionTime = calculateAverageTransactionTime(),
            currentBaseFee = latestLedger.baseFeeInStroops,
            ledgerVersion = latestLedger.sequence.toInt(),
            lastLedgerCloseTime = latestLedger.closedAt.toInstant().toEpochMilli(),
            protocolVersion = health.protocolVersion,
            networkQuality = determineNetworkQuality()
        )
    }

    override suspend fun canRetryTransaction(transactionId: String): Boolean {
        val transaction = server.transactions().transaction(transactionId)
        return when {
            transaction.successful -> false
            transaction.resultXdr.contains("tx_too_late") -> true
            transaction.resultXdr.contains("tx_insufficient_fee") -> true
            else -> false
        }
    }

    override suspend fun estimateRetryGasPrice(transactionId: String): Long {
        val transaction = server.transactions().transaction(transactionId)
        val baseFee = server.ledgers().order(RequestBuilder.Order.DESC).limit(1).execute()
            .records[0].baseFeeInStroops
        
        return when {
            transaction.resultXdr.contains("tx_insufficient_fee") -> transaction.feePaid * 2
            else -> baseFee * 2
        }
    }

    override suspend fun isTransactionFinalized(transactionId: String): Boolean {
        val transaction = server.transactions().transaction(transactionId)
        return transaction.ledger != null
    }

    override suspend fun getTransactionConfirmations(transactionId: String): Int? {
        val transaction = server.transactions().transaction(transactionId)
        if (transaction.ledger == null) return null
        
        val currentLedger = server.ledgers().order(RequestBuilder.Order.DESC).limit(1).execute()
            .records[0].sequence
        
        return (currentLedger - transaction.ledger).toInt()
    }

    override fun monitorTransactionEvents(transactionId: String): Flow<BlockchainEvent> {
        return flow {
            server.transactions()
                .cursor("now")
                .stream(object : EventListener<TransactionResponse> {
                    override fun onEvent(response: TransactionResponse) {
                        if (response.hash == transactionId) {
                            val event = when {
                                response.successful -> BlockchainEvent.TransactionConfirmed(
                                    transactionId = transactionId,
                                    timestamp = Instant.now(),
                                    blockHeight = response.ledger,
                                    confirmations = 1
                                )
                                else -> BlockchainEvent.TransactionFailed(
                                    transactionId = transactionId,
                                    timestamp = Instant.now(),
                                    error = createTransactionError(response)!!,
                                    canRetry = canRetryTransaction(transactionId)
                                )
                            }
                            _blockchainEvents.tryEmit(event)
                        }
                    }
                    
                    override fun onFailure(error: Optional<Throwable>, code: Optional<Int>) {
                        auditLogger.logEvent(
                            "BLOCKCHAIN_MONITOR_ERROR",
                            "Error monitoring blockchain events",
                            mapOf(
                                "transaction_id" to transactionId,
                                "error" to (error.orElse(null)?.message ?: "Unknown error")
                            )
                        )
                        _blockchainEvents.tryEmit(BlockchainEvent.NetworkEvent(
                            transactionId = transactionId,
                            timestamp = Instant.now(),
                            eventType = NetworkEventType.NETWORK_RESET,
                            details = mapOf("error" to (error.orElse(null)?.message.orEmpty()))
                        ))
                    }
                })
        }.catch { e ->
            emit(BlockchainEvent.NetworkEvent(
                transactionId = transactionId,
                timestamp = Instant.now(),
                eventType = NetworkEventType.NETWORK_RESET,
                details = mapOf("error" to e.message.orEmpty())
            ))
        }
    }

    private fun createTransactionError(transaction: TransactionResponse): TransactionError? {
        return when {
            transaction.resultXdr.contains("tx_insufficient_balance") -> 
                TransactionError.InsufficientFundsError(
                    transactionId = transaction.hash,
                    message = "Insufficient balance for transaction",
                    timestamp = Instant.now(),
                    requiredAmount = transaction.feePaid.toString(),
                    availableAmount = "0" // Would need to query account for actual balance
                )
            transaction.resultXdr.contains("tx_too_late") ->
                TransactionError.NetworkCongestionError(
                    transactionId = transaction.hash,
                    message = "Transaction expired due to network congestion",
                    timestamp = Instant.now(),
                    retryAfter = DEFAULT_RETRY_INTERVAL
                )
            transaction.resultXdr.contains("tx_failed") ->
                TransactionError.SmartContractError(
                    transactionId = transaction.hash,
                    message = "Smart contract execution failed",
                    timestamp = Instant.now(),
                    contractAddress = transaction.sourceAccount,
                    errorCode = transaction.resultXdr
                )
            else -> null
        }
    }

    private suspend fun calculateAverageBlockTime(): Float {
        val ledgers = server.ledgers()
            .order(RequestBuilder.Order.DESC)
            .limit(MAX_LEDGER_QUERY)
            .execute()
            .records
        
        var totalTime = 0L
        for (i in 0 until ledgers.size - 1) {
            totalTime += ledgers[i].closedAt.toInstant().toEpochMilli() - 
                        ledgers[i + 1].closedAt.toInstant().toEpochMilli()
        }
        
        return totalTime.toFloat() / (ledgers.size - 1)
    }

    private suspend fun calculateNetworkCongestion(): Float {
        val ledgers = server.ledgers()
            .order(RequestBuilder.Order.DESC)
            .limit(5)
            .execute()
            .records
        
        val maxOperations = ledgers.maxOf { it.operationCount }
        val averageOperations = ledgers.map { it.operationCount }.average()
        
        return (averageOperations / maxOperations).toFloat()
    }

    private suspend fun calculateAverageTransactionTime(): Long {
        val transactions = server.transactions()
            .order(RequestBuilder.Order.DESC)
            .limit(MAX_LEDGER_QUERY)
            .execute()
            .records
        
        return transactions.map { it.ledger }.average().toLong()
    }

    private suspend fun determineNetworkQuality(): NetworkQuality {
        val congestion = calculateNetworkCongestion()
        return when {
            congestion < CONGESTION_THRESHOLD_LOW -> NetworkQuality.EXCELLENT
            congestion < CONGESTION_THRESHOLD_MEDIUM -> NetworkQuality.NORMAL
            congestion < CONGESTION_THRESHOLD_HIGH -> NetworkQuality.DEGRADED
            else -> NetworkQuality.POOR
        }
    }

    private companion object {
        const val MAX_LEDGER_QUERY = 100
        const val DEFAULT_RETRY_INTERVAL = 5000L
        const val GAS_PRICE_SAFETY_MARGIN = 1.2
        const val MAX_GAS_PRICE_INCREASE = 2.0
        const val MIN_GAS_PRICE_INCREASE = 1.1
        const val CONGESTION_THRESHOLD_HIGH = 0.8f
        const val CONGESTION_THRESHOLD_MEDIUM = 0.6f
        const val CONGESTION_THRESHOLD_LOW = 0.3f
    }

    /**
     * Optimizes gas fee based on network conditions and transaction priority
     */
    suspend fun optimizeGasFee(transactionId: String, priority: TransactionPriority = TransactionPriority.NORMAL): Long {
        val networkState = getNetworkState()
        val baseFee = networkState.currentBaseFee
        
        val multiplier = when (priority) {
            TransactionPriority.LOW -> 1.0
            TransactionPriority.NORMAL -> GAS_PRICE_SAFETY_MARGIN
            TransactionPriority.HIGH -> MAX_GAS_PRICE_INCREASE
        }

        val congestionMultiplier = (1.0 + networkState.congestion).coerceAtMost(MAX_GAS_PRICE_INCREASE)
        val suggestedFee = (baseFee * multiplier * congestionMultiplier).toLong()

        auditLogger.logEvent(
            "GAS_FEE_OPTIMIZATION",
            "Optimized gas fee for transaction",
            mapOf(
                "transaction_id" to transactionId,
                "base_fee" to baseFee.toString(),
                "priority" to priority.name,
                "congestion" to networkState.congestion.toString(),
                "suggested_fee" to suggestedFee.toString()
            )
        )

        return suggestedFee
    }

    /**
     * Enhanced smart contract error handling with detailed diagnostics
     */
    suspend fun handleSmartContractError(transactionId: String, error: SmartContractError): SmartContractDiagnostics {
        val transaction = server.transactions().transaction(transactionId)
        val operations = transaction.operations
        
        val contractOperation = operations.find { op ->
            op.type == "invoke_host_function" || op.type == "execute_contract"
        }

        val diagnostics = SmartContractDiagnostics(
            contractAddress = error.contractAddress,
            errorCode = error.errorCode,
            operationType = contractOperation?.type ?: "unknown",
            stackTrace = parseContractStackTrace(transaction.resultXdr),
            gasUsed = transaction.feePaid,
            timestamp = Instant.now()
        )

        auditLogger.logEvent(
            "SMART_CONTRACT_DIAGNOSTICS",
            "Generated smart contract error diagnostics",
            mapOf(
                "transaction_id" to transactionId,
                "contract_address" to diagnostics.contractAddress,
                "error_code" to diagnostics.errorCode,
                "operation_type" to diagnostics.operationType
            )
        )

        return diagnostics
    }

    private fun parseContractStackTrace(resultXdr: String): List<String> {
        return resultXdr.split(";")
            .filter { it.contains("contract_error") }
            .map { it.trim() }
    }
} 