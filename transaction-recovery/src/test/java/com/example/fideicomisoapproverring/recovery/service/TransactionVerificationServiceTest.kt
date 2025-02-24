package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.example.fideicomisoapproverring.recovery.util.TestCoroutineRule
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.core.wallet.WalletManager
import com.example.fideicomisoapproverring.recovery.forensics.BlockchainState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.clearAllMocks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.stellar.sdk.responses.TransactionResponse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineRule::class)
class TransactionVerificationServiceTest {
    private lateinit var verificationService: TransactionVerificationService
    private lateinit var stellarTransactionManager: StellarTransactionManager
    private lateinit var walletManager: WalletManager
    private lateinit var auditLogger: SecureAuditLogger
    private lateinit var mockTransaction: TransactionResponse
    private lateinit var mockBlockchainState: BlockchainState
    private lateinit var mockSmartContractState: SmartContractState
    private lateinit var mockEscrowState: EscrowState
    private lateinit var testCoroutineRule: TestCoroutineRule

    @BeforeEach
    fun setup(testCoroutineRule: TestCoroutineRule) {
        this.testCoroutineRule = testCoroutineRule
        stellarTransactionManager = mockk()
        walletManager = mockk()
        auditLogger = mockk(relaxed = true)
        mockTransaction = mockk()
        mockBlockchainState = BlockchainState(
            lastBlockHeight = 1000L,
            averageBlockTime = 5.0f,
            networkCongestion = 0.5f,
            gasPrice = "100",
            isHealthy = true,
            protocolVersion = 19,
            networkUptime = 99.9f
        )
        mockSmartContractState = SmartContractState(
            contractAddress = "contract_address",
            isValid = false
        )
        mockEscrowState = EscrowState(
            escrowAddress = "escrow_address",
            isValid = false
        )

        verificationService = TransactionVerificationService(
            stellarTransactionManager,
            walletManager,
            auditLogger
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test successful verification flow`() = testCoroutineRule.runTest {
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
    fun `test verification failure due to invalid transaction`() = testCoroutineRule.runTest {
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
    fun `test verification failure due to unhealthy blockchain`() = testCoroutineRule.runTest {
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
    fun `test smart contract verification failure`() = testCoroutineRule.runTest {
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
    fun `test escrow verification failure`() = testCoroutineRule.runTest {
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
    fun `test verification failure due to inconsistent state`() = testCoroutineRule.runTest {
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
    fun `test verification error handling`() = testCoroutineRule.runTest {
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

data class SmartContractState(
    val contractAddress: String,
    val isValid: Boolean
)

data class EscrowState(
    val escrowAddress: String,
    val isValid: Boolean
) 