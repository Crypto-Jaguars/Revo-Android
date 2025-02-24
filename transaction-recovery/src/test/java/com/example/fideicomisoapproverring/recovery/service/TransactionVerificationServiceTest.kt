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

class TransactionVerificationServiceTest {
    private lateinit var verificationService: TransactionVerificationService
    private lateinit var stellarTransactionManager: StellarTransactionManager
    private lateinit var walletManager: WalletManager
    private lateinit var auditLogger: SecureAuditLogger
    private lateinit var mockTransaction: TransactionResponse
    private lateinit var mockBlockchainState: StellarTransactionManager.BlockchainState
    private lateinit var mockSmartContractState: StellarTransactionManager.SmartContractState
    private lateinit var mockEscrowState: StellarTransactionManager.EscrowState

    @Before
    fun setup() {
        stellarTransactionManager = mockk()
        walletManager = mockk()
        auditLogger = mockk(relaxed = true)
        mockTransaction = mockk()
        mockBlockchainState = mockk()
        mockSmartContractState = mockk()
        mockEscrowState = mockk()

        verificationService = TransactionVerificationService(
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
    fun `test successful verification flow`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        every { mockTransaction.isSmartContractTransaction() } returns false
        every { mockTransaction.isEscrowTransaction() } returns false
        every { mockTransaction.isConsistent() } returns true
        every { walletManager.isConnected() } returns true

        coEvery { stellarTransactionManager.getBlockchainState() } returns mockBlockchainState
        every { mockBlockchainState.isHealthy } returns true

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Completed)
        assertEquals(transactionId, state?.transactionId)
    }

    @Test
    fun `test verification failure due to invalid transaction`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns ""
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Basic transaction validation failed", (state as VerificationState.Failed).reason)
    }

    @Test
    fun `test verification failure due to unhealthy blockchain`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"

        coEvery { stellarTransactionManager.getBlockchainState() } returns mockBlockchainState
        every { mockBlockchainState.isHealthy } returns false

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Blockchain state verification failed", (state as VerificationState.Failed).reason)
    }

    @Test
    fun `test smart contract verification failure`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        every { mockTransaction.isSmartContractTransaction() } returns true
        every { mockTransaction.contractAddress } returns "contract_address"

        coEvery { stellarTransactionManager.getBlockchainState() } returns mockBlockchainState
        every { mockBlockchainState.isHealthy } returns true

        coEvery { stellarTransactionManager.getSmartContractState(any()) } returns mockSmartContractState
        every { mockSmartContractState.isValid } returns false

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Smart contract verification failed", (state as VerificationState.Failed).reason)
    }

    @Test
    fun `test escrow verification failure`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        every { mockTransaction.isSmartContractTransaction() } returns false
        every { mockTransaction.isEscrowTransaction() } returns true
        every { mockTransaction.escrowAddress } returns "escrow_address"

        coEvery { stellarTransactionManager.getBlockchainState() } returns mockBlockchainState
        every { mockBlockchainState.isHealthy } returns true

        coEvery { stellarTransactionManager.getEscrowState(any()) } returns mockEscrowState
        every { mockEscrowState.isValid } returns false

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Escrow state verification failed", (state as VerificationState.Failed).reason)
    }

    @Test
    fun `test verification failure due to inconsistent state`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns mockTransaction
        every { mockTransaction.sourceAccount } returns "source_account"
        every { mockTransaction.destinationAccount } returns "destination_account"
        every { mockTransaction.amount } returns "100"
        every { mockTransaction.isSmartContractTransaction() } returns false
        every { mockTransaction.isEscrowTransaction() } returns false
        every { mockTransaction.isConsistent() } returns false

        coEvery { stellarTransactionManager.getBlockchainState() } returns mockBlockchainState
        every { mockBlockchainState.isHealthy } returns true
        every { walletManager.isConnected() } returns true

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Final consistency check failed", (state as VerificationState.Failed).reason)
    }

    @Test
    fun `test verification error handling`() = runTest {
        // Given
        val transactionId = "test_transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } throws RuntimeException("Network error")

        // When
        verificationService.startVerification(transactionId)

        // Then
        val state = verificationService.verificationState.value[transactionId]
        assertTrue(state is VerificationState.Failed)
        assertEquals("Verification error", (state as VerificationState.Failed).reason)
        assertEquals("Network error", state.details)
    }
} 