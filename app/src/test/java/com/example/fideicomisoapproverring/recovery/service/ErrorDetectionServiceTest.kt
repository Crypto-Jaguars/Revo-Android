package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.wallet.WalletManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.stellar.sdk.responses.TransactionResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorDetectionServiceTest {
    private lateinit var errorDetectionService: ErrorDetectionService
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

        errorDetectionService = ErrorDetectionService(
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
    fun `test wallet connection error detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns false
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is WalletConnectionError)
        assertEquals(transactionId, error.transactionId)
        coVerify { walletManager.isConnected() }
        coVerify { auditLogger.logEvent(any(), any(), any()) }
    }

    @Test
    fun `test network congestion error detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.resultXdr } returns "tx_too_late"
        every { mockTransaction.ledger.closedAt.time } returns System.currentTimeMillis()
        every { mockTransaction.createdAt.time } returns System.currentTimeMillis() - 15000 // 15s delay

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is NetworkCongestionError)
        assertEquals(transactionId, error.transactionId)
        assertEquals(CongestionLevel.MEDIUM, (error as NetworkCongestionError).congestionLevel)
    }

    @Test
    fun `test insufficient funds error detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.resultXdr } returns "tx_insufficient_balance"

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is InsufficientFundsError)
        assertEquals(transactionId, error.transactionId)
    }

    @Test
    fun `test smart contract error detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.resultXdr } returns "op_execution_failed"
        every { mockTransaction.operations } returns listOf(mockk {
            every { type } returns "invoke_contract"
            every { sourceAccount } returns "contract123"
        })

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is SmartContractError)
        assertEquals(transactionId, error.transactionId)
        assertEquals("contract123", (error as SmartContractError).contractAddress)
    }

    @Test
    fun `test escrow error detection`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.resultXdr } returns "escrow_invalid_state"
        every { mockTransaction.operations } returns listOf(mockk {
            every { type } returns "manage_escrow"
            every { sourceAccount } returns "escrow123"
        })

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is EscrowError)
        assertEquals(transactionId, error.transactionId)
        assertEquals("escrow123", (error as EscrowError).escrowContractAddress)
    }

    @Test
    fun `test no error detection when transaction is valid`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.resultXdr } returns "tx_success"

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNull(error)
    }

    @Test
    fun `test unknown error detection on exception`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { walletManager.isConnected() } returns true
        coEvery { stellarTransactionManager.getTransaction(any()) } throws RuntimeException("Unexpected error")

        // When
        val error = errorDetectionService.analyzeTransaction(transactionId)

        // Then
        assertNotNull(error)
        assertTrue(error is UnknownError)
        assertEquals(transactionId, error.transactionId)
        assertTrue(error.message.contains("Unexpected error"))
    }
} 