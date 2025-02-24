package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.core.model.TransactionError
import com.example.fideicomisoapproverring.core.model.TransactionStatus
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.core.wallet.WalletManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.stellar.sdk.responses.TransactionResponse
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class TransactionMonitorServiceTest {
    @get:Rule
    val mainDispatcherRule = TestDispatcherRule()

    @MockK
    private lateinit var stellarTransactionManager: StellarTransactionManager

    @MockK
    private lateinit var walletManager: WalletManager

    @MockK
    private lateinit var auditLogger: SecureAuditLogger

    @MockK
    private lateinit var recoveryService: TransactionRecoveryService

    @MockK
    private lateinit var transactionResponse: TransactionResponse

    private lateinit var monitorService: TransactionMonitorService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        monitorService = TransactionMonitorService(
            stellarTransactionManager,
            walletManager,
            auditLogger,
            recoveryService,
            TestScope()
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `startMonitoring should start monitoring transaction`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns transactionResponse
        every { transactionResponse.isSuccessful() } returns true
        every { auditLogger.logEvent(any(), any(), any()) } just Runs

        // When
        monitorService.startMonitoring(transactionId)

        // Then
        val transactions = monitorService.monitoredTransactions.first()
        assertTrue(transactions.containsKey(transactionId))
        assertEquals(TransactionStatus.PENDING, transactions[transactionId])
    }

    @Test
    fun `stopMonitoring should stop monitoring transaction`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns transactionResponse
        every { transactionResponse.isSuccessful() } returns true
        every { auditLogger.logEvent(any(), any(), any()) } just Runs

        // When
        monitorService.startMonitoring(transactionId)
        monitorService.stopMonitoring(transactionId)

        // Then
        val transactions = monitorService.monitoredTransactions.first()
        assertFalse(transactions.containsKey(transactionId))
    }

    @Test
    fun `monitorTransaction should handle successful transaction`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns transactionResponse
        every { transactionResponse.isSuccessful() } returns true
        every { auditLogger.logEvent(any(), any(), any()) } just Runs

        // When
        monitorService.startMonitoring(transactionId)

        // Then
        coVerify { stellarTransactionManager.getTransaction(transactionId) }
        verify { transactionResponse.isSuccessful() }
        verify { auditLogger.logEvent(any(), any(), any()) }
    }

    @Test
    fun `monitorTransaction should handle failed transaction`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } returns transactionResponse
        every { transactionResponse.isSuccessful() } returns false
        every { auditLogger.logEvent(any(), any(), any()) } just Runs
        coEvery { recoveryService.reportError(any()) } returns "error-id"

        // When
        monitorService.startMonitoring(transactionId)

        // Then
        coVerify { stellarTransactionManager.getTransaction(transactionId) }
        verify { transactionResponse.isSuccessful() }
        verify { auditLogger.logEvent(any(), any(), any()) }
        coVerify { recoveryService.reportError(any()) }
    }

    @Test
    fun `monitorTransaction should handle network error`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } throws java.net.SocketTimeoutException()
        every { auditLogger.logEvent(any(), any(), any()) } just Runs
        coEvery { recoveryService.reportError(any()) } returns "error-id"

        // When
        monitorService.startMonitoring(transactionId)

        // Then
        coVerify { stellarTransactionManager.getTransaction(transactionId) }
        verify { auditLogger.logEvent(any(), any(), any()) }
        coVerify { recoveryService.reportError(any()) }
    }

    @Test
    fun `monitorTransaction should handle max retries`() = runTest {
        // Given
        val transactionId = "test-transaction"
        coEvery { stellarTransactionManager.getTransaction(any()) } throws Exception("Test error")
        every { auditLogger.logEvent(any(), any(), any()) } just Runs
        coEvery { recoveryService.reportError(any()) } returns "error-id"

        // When
        monitorService.startMonitoring(transactionId)

        // Then
        coVerify(exactly = 3) { stellarTransactionManager.getTransaction(transactionId) }
        verify { auditLogger.logEvent(any(), any(), any()) }
        coVerify { recoveryService.reportError(any()) }
    }
}

class TestDispatcherRule : TestWatcher() {
    private val testDispatcher = StandardTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
} 