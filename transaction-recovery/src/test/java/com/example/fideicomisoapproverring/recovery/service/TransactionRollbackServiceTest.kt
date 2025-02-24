package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.core.wallet.WalletManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.stellar.sdk.responses.TransactionResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionRollbackServiceTest {
    private lateinit var rollbackService: TransactionRollbackService
    private lateinit var stellarTransactionManager: StellarTransactionManager
    private lateinit var walletManager: WalletManager
    private lateinit var auditLogger: SecureAuditLogger
    private lateinit var mockTransaction: TransactionResponse

    @Before
    fun setup() {
        stellarTransactionManager = mockk()
        walletManager = mockk()
        auditLogger = mockk(relaxed = true)
        mockTransaction = mockk()

        rollbackService = TransactionRollbackService(
            stellarTransactionManager,
            walletManager,
            auditLogger
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test insufficient funds rollback`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = InsufficientFundsError(
            transactionId = transactionId,
            message = "Insufficient funds",
            requiredAmount = "100",
            availableAmount = "50",
            currency = "XLM"
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        coEvery { 
            stellarTransactionManager.submitTransaction(
                sourceAccount = any(),
                destinationAccount = any(),
                amount = any(),
                memo = any()
            )
        } returns mockTransaction

        // When
        rollbackService.initiateRollback(transactionId, error)

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.Completed)
        assertEquals(transactionId, state?.transactionId)
        coVerify { 
            stellarTransactionManager.submitTransaction(
                sourceAccount = "destination_account",
                destinationAccount = "source_account",
                amount = "100",
                memo = any()
            )
        }
    }

    @Test
    fun `test escrow rollback`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = EscrowError(
            transactionId = transactionId,
            message = "Escrow verification failed",
            escrowContractAddress = "escrow_address",
            escrowState = "FAILED",
            participantAddresses = listOf("address1", "address2")
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId
        every { mockTransaction.sourceAccount } returns "escrow_address"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        coEvery { 
            stellarTransactionManager.releaseEscrow(
                escrowAccount = any(),
                beneficiary = any(),
                amount = any()
            )
        } returns mockTransaction

        // When
        rollbackService.initiateRollback(transactionId, error)

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.Completed)
        assertEquals(transactionId, state?.transactionId)
        coVerify { 
            stellarTransactionManager.releaseEscrow(
                escrowAccount = "escrow_address",
                beneficiary = "destination_account",
                amount = "100"
            )
        }
    }

    @Test
    fun `test wallet connection rollback requiring manual intervention`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = WalletConnectionError(
            transactionId = transactionId,
            message = "Wallet disconnected",
            lastConnectedTimestamp = System.currentTimeMillis(),
            connectionAttempts = 3,
            walletType = "Stellar"
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId
        every { walletManager.isConnected() } returns false

        // When
        rollbackService.initiateRollback(transactionId, error)

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.RequiresManualIntervention)
        assertEquals(transactionId, state?.transactionId)
        assertEquals(
            "Wallet connection required for rollback",
            (state as RollbackState.RequiresManualIntervention).reason
        )
    }

    @Test
    fun `test network congestion rollback with partial refund`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = NetworkCongestionError(
            transactionId = transactionId,
            message = "Network congestion detected",
            retryAfter = 5000L,
            networkLatency = 2000L,
            congestionLevel = CongestionLevel.HIGH
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        coEvery { 
            stellarTransactionManager.submitTransaction(
                sourceAccount = any(),
                destinationAccount = any(),
                amount = any(),
                memo = any()
            )
        } returns mockTransaction

        // When
        rollbackService.initiateRollback(transactionId, error)

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.Completed)
        assertEquals(transactionId, state?.transactionId)
        coVerify { 
            stellarTransactionManager.submitTransaction(
                sourceAccount = "destination_account",
                destinationAccount = "source_account",
                amount = "99.9", // 0.1% fee
                memo = any()
            )
        }
    }

    @Test
    fun `test unknown error rollback requiring manual intervention`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = UnknownError(
            transactionId = transactionId,
            message = "Unknown error occurred",
            stackTrace = "stack trace"
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId

        // When
        rollbackService.initiateRollback(transactionId, error)

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.RequiresManualIntervention)
        assertEquals(transactionId, state?.transactionId)
        assertEquals(
            "Unknown error requires manual review",
            (state as RollbackState.RequiresManualIntervention).reason
        )
    }

    @Test
    fun `test rollback failure handling`() = runTest {
        // Given
        val transactionId = "test_transaction"
        val error = InsufficientFundsError(
            transactionId = transactionId,
            message = "Insufficient funds",
            requiredAmount = "100",
            availableAmount = "50",
            currency = "XLM"
        )

        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.hash } returns transactionId
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        coEvery { 
            stellarTransactionManager.submitTransaction(
                sourceAccount = any(),
                destinationAccount = any(),
                amount = any(),
                memo = any()
            )
        } throws RuntimeException("Network error")

        // When
        try {
            rollbackService.initiateRollback(transactionId, error)
        } catch (e: Exception) {
            // Expected exception
        }

        // Then
        val state = rollbackService.rollbackState.value[transactionId]
        assertTrue(state is RollbackState.Failed)
        assertEquals(transactionId, state?.transactionId)
        assertEquals("Network error", (state as RollbackState.Failed).reason)
    }
} 