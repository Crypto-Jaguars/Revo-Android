package com.example.fideicomisoapproverring.recovery.state

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.TransactionState
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SecureTransactionStateManagerTest {
    private lateinit var stateManager: SecureTransactionStateManager
    private lateinit var stellarTransactionManager: StellarTransactionManager
    private lateinit var auditLogger: SecureAuditLogger

    @Before
    fun setup() {
        stellarTransactionManager = mockk()
        auditLogger = mockk(relaxed = true)
        stateManager = SecureTransactionStateManager(stellarTransactionManager, auditLogger)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test initial state transition`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val initialState = TransactionState.Initial(transactionId)

        // When
        stateManager.updateTransactionState(transactionId, initialState)

        // Then
        val state = stateManager.getTransactionState(transactionId)
        assertEquals(initialState, state)
        coVerify { auditLogger.logEvent(any(), any(), any()) }
    }

    @Test
    fun `test valid state transition sequence`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val initialState = TransactionState.Initial(transactionId)
        val processingState = TransactionState.Processing(transactionId, 50, "Processing transaction")
        val verifyingState = TransactionState.Verifying(
            transactionId,
            TransactionState.VerificationStage.BLOCKCHAIN_STATE,
            75
        )

        // When
        stateManager.updateTransactionState(transactionId, initialState)
        stateManager.updateTransactionState(transactionId, processingState)
        stateManager.updateTransactionState(transactionId, verifyingState)

        // Then
        val state = stateManager.getTransactionState(transactionId)
        assertEquals(verifyingState, state)
        coVerify(exactly = 3) { auditLogger.logEvent(any(), any(), any()) }
    }

    @Test
    fun `test invalid initial state transition`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val processingState = TransactionState.Processing(transactionId, 50, "Processing transaction")

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            stateManager.updateTransactionState(transactionId, processingState)
        }
    }

    @Test
    fun `test invalid transition from terminal state`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val initialState = TransactionState.Initial(transactionId)
        val terminalState = TransactionState.Terminal(
            transactionId,
            TransactionState.TransactionOutcome.SUCCESS,
            "Transaction completed successfully"
        )
        val processingState = TransactionState.Processing(transactionId, 50, "Processing transaction")

        // When
        stateManager.updateTransactionState(transactionId, initialState)
        stateManager.updateTransactionState(transactionId, terminalState)

        // Then
        assertFailsWith<IllegalStateException> {
            stateManager.updateTransactionState(transactionId, processingState)
        }
    }

    @Test
    fun `test cleanup of old transactions`() = runTest {
        // Given
        val transactionId1 = "test_transaction_1"
        val transactionId2 = "test_transaction_2"
        val maxAge = 1000L // 1 second

        val terminalState1 = TransactionState.Terminal(
            transactionId1,
            TransactionState.TransactionOutcome.SUCCESS,
            "Completed"
        )
        val terminalState2 = TransactionState.Terminal(
            transactionId2,
            TransactionState.TransactionOutcome.SUCCESS,
            "Completed"
        )

        // When
        stateManager.updateTransactionState(transactionId1, TransactionState.Initial(transactionId1))
        stateManager.updateTransactionState(transactionId1, terminalState1)
        stateManager.updateTransactionState(transactionId2, TransactionState.Initial(transactionId2))
        stateManager.updateTransactionState(transactionId2, terminalState2)

        // Simulate time passing
        coEvery { System.currentTimeMillis() } returns terminalState1.timestamp + maxAge + 100

        // Clean up old transactions
        stateManager.cleanupOldTransactions(maxAge)

        // Then
        assertNull(stateManager.getTransactionState(transactionId1))
        assertNull(stateManager.getTransactionState(transactionId2))
        coVerify(exactly = 2) { auditLogger.logEvent("TRANSACTION_STATE_CLEANED", any(), any()) }
    }

    @Test
    fun `test get transactions in state`() = runTest {
        // Given
        val transactionId1 = "test_transaction_1"
        val transactionId2 = "test_transaction_2"
        val transactionId3 = "test_transaction_3"

        val processingState1 = TransactionState.Processing(transactionId1, 50, "Processing")
        val processingState2 = TransactionState.Processing(transactionId2, 75, "Processing")
        val verifyingState = TransactionState.Verifying(
            transactionId3,
            TransactionState.VerificationStage.SMART_CONTRACT,
            80
        )

        // When
        stateManager.updateTransactionState(transactionId1, TransactionState.Initial(transactionId1))
        stateManager.updateTransactionState(transactionId1, processingState1)
        stateManager.updateTransactionState(transactionId2, TransactionState.Initial(transactionId2))
        stateManager.updateTransactionState(transactionId2, processingState2)
        stateManager.updateTransactionState(transactionId3, TransactionState.Initial(transactionId3))
        stateManager.updateTransactionState(transactionId3, verifyingState)

        // Then
        val processingTransactions = stateManager.getTransactionsInState(TransactionState.Processing::class.java)
        assertEquals(2, processingTransactions.size)
        assertTrue(processingTransactions.contains(transactionId1))
        assertTrue(processingTransactions.contains(transactionId2))

        val verifyingTransactions = stateManager.getTransactionsInState(TransactionState.Verifying::class.java)
        assertEquals(1, verifyingTransactions.size)
        assertTrue(verifyingTransactions.contains(transactionId3))
    }

    @Test
    fun `test state flow updates`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val initialState = TransactionState.Initial(transactionId)
        val processingState = TransactionState.Processing(transactionId, 50, "Processing transaction")

        // When
        stateManager.updateTransactionState(transactionId, initialState)
        val stateAfterInitial = stateManager.stateFlow.first()

        stateManager.updateTransactionState(transactionId, processingState)
        val stateAfterProcessing = stateManager.stateFlow.first()

        // Then
        assertEquals(initialState, stateAfterInitial[transactionId])
        assertEquals(processingState, stateAfterProcessing[transactionId])
    }
} 