package com.example.fideicomisoapproverring.recovery.forensics

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.util.MainCoroutineRule
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.stellar.model.StellarOperation
import com.example.fideicomisoapproverring.stellar.model.StellarTransaction
import com.example.fideicomisoapproverring.stellar.model.TransactionStatus
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class TransactionForensicsServiceTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var forensicsService: TransactionForensicsService
    private lateinit var stellarTransactionManager: StellarTransactionManager
    private lateinit var recoveryService: TransactionRecoveryService
    private lateinit var auditLogger: SecureAuditLogger

    @Before
    fun setup() {
        stellarTransactionManager = mockk()
        recoveryService = mockk()
        auditLogger = mockk(relaxed = true)
        forensicsService = TransactionForensicsService(
            stellarTransactionManager,
            recoveryService,
            auditLogger
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test analyze network congestion error`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val timestamp = Instant.now()
        val error = NetworkCongestionError(
            message = "High network congestion",
            timestamp = timestamp,
            congestionLevel = CongestionLevel.HIGH
        )

        val mockTransaction = StellarTransaction(
            id = transactionId,
            createdAt = timestamp.minusSeconds(60),
            lastModified = timestamp,
            sourceAccount = "source_account",
            fee = 100L,
            operations = listOf(
                StellarOperation(
                    type = "payment",
                    sourceAccount = "source_account",
                    amount = "100",
                    asset = "XLM",
                    destination = "destination_account"
                )
            ),
            memo = "Test payment",
            signatures = listOf("signature1"),
            status = TransactionStatus.FAILED
        )

        val stateHistory = listOf(
            TransactionState.Initial(transactionId),
            TransactionState.Processing(transactionId, 50, "Processing payment"),
            TransactionState.Error(transactionId, error, true)
        )

        coEvery { stellarTransactionManager.getTransaction(transactionId) } returns mockTransaction
        coEvery { recoveryService.getTransactionStateHistory(transactionId) } returns stateHistory

        // When
        forensicsService.analyzeError(transactionId, error)
        val report = forensicsService.forensicsState.first()[transactionId]

        // Then
        assertEquals(transactionId, report?.transactionId)
        assertEquals("NetworkCongestionError", report?.errorDetails?.type)
        assertEquals(error.message, report?.errorDetails?.message)
        assertEquals(error.severity, report?.errorDetails?.severity)
        assertEquals(error.recoverable, report?.errorDetails?.recoverable)

        assertEquals(mockTransaction.createdAt, report?.transactionDetails?.createdAt)
        assertEquals(mockTransaction.lastModified, report?.transactionDetails?.lastModified)
        assertEquals(mockTransaction.operations.map { it.type }, report?.transactionDetails?.operations)
        assertEquals(mockTransaction.operations.map { it.sourceAccount }.distinct(), report?.transactionDetails?.participants)

        assertEquals(stateHistory, report?.stateHistory)
        assertTrue(report?.recommendations?.isNotEmpty() == true)
        assertTrue(report?.recommendations?.any { it.contains("network congestion") } == true)

        coVerify { 
            auditLogger.logEvent(
                "FORENSICS_ANALYSIS_COMPLETED",
                any(),
                withArg { metadata ->
                    metadata["error_type"] == "NetworkCongestionError"
                }
            )
        }
    }

    @Test
    fun `test error pattern detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val baseTimestamp = Instant.now()
        
        // Create multiple errors of the same type
        repeat(6) { index ->
            val error = NetworkCongestionError(
                message = "Network congestion error $index",
                timestamp = baseTimestamp.plusSeconds(index * 3600L),
                congestionLevel = CongestionLevel.HIGH
            )

            val mockTransaction = StellarTransaction(
                id = "${transactionId}_$index",
                createdAt = error.timestamp.minusSeconds(60),
                lastModified = error.timestamp,
                sourceAccount = "source_account",
                fee = 100L,
                operations = listOf(
                    StellarOperation(
                        type = "payment",
                        sourceAccount = "source_account",
                        amount = "100",
                        asset = "XLM",
                        destination = "destination_account"
                    )
                ),
                memo = "Test payment",
                signatures = listOf("signature1"),
                status = TransactionStatus.FAILED
            )

            val stateHistory = listOf(
                TransactionState.Initial("${transactionId}_$index"),
                TransactionState.Error("${transactionId}_$index", error, true)
            )

            coEvery { stellarTransactionManager.getTransaction("${transactionId}_$index") } returns mockTransaction
            coEvery { recoveryService.getTransactionStateHistory("${transactionId}_$index") } returns stateHistory

            // When
            forensicsService.analyzeError("${transactionId}_$index", error)
        }

        // Then
        coVerify(exactly = 1) { 
            auditLogger.logEvent(
                "ERROR_PATTERN_DETECTED",
                any(),
                withArg { metadata ->
                    metadata["error_type"] == "NetworkCongestionError"
                }
            )
        }
    }
} 