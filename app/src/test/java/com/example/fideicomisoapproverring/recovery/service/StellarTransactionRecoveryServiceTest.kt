package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.recovery.model.*
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.stellar.sdk.*
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.SubmitTransactionResponse
import org.stellar.sdk.responses.TransactionResponse
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StellarTransactionRecoveryServiceTest {
    
    private lateinit var recoveryService: StellarTransactionRecoveryService
    
    @Mock
    private lateinit var server: Server
    
    @Mock
    private lateinit var transactionResponse: TransactionResponse
    
    @Mock
    private lateinit var accountResponse: AccountResponse
    
    @Mock
    private lateinit var submitResponse: SubmitTransactionResponse
    
    @Before
    fun setup() {
        recoveryService = StellarTransactionRecoveryService(server)
    }
    
    @Test
    fun `test reporting network congestion error`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.BLOCKCHAIN_NETWORK_CONGESTION,
            message = "Network congestion detected",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // When
        val result = recoveryService.reportError(error).first()
        
        // Then
        assertEquals(TransactionErrorStatus.ANALYZING, result.status)
        assertEquals("Analyzing error...", result.message)
        assertEquals(error, result.error)
    }
    
    @Test
    fun `test handling insufficient funds error`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.INSUFFICIENT_FUNDS,
            message = "Insufficient funds for transaction",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // Mock account response
        `when`(server.accounts().account(error.walletAddress!!)).thenReturn(accountResponse)
        val balance = mockk<AccountResponse.Balance>(relaxed = true)
        `when`(balance.balance).thenReturn("0.0")
        `when`(balance.assetType).thenReturn("native")
        `when`(accountResponse.balances).thenReturn(listOf(balance))
        
        // When
        val result = recoveryService.attemptRecovery(error)
        
        // Then
        assertEquals(TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED, result.status)
        assertTrue(result.message.contains("Account has no available balance"))
    }
    
    @Test
    fun `test transaction integrity validation`() = runTest {
        // Given
        val transactionId = "test-tx-id"
        
        // Mock transaction response
        `when`(server.transactions().transaction(transactionId)).thenReturn(transactionResponse)
        `when`(transactionResponse.isSuccessful).thenReturn(false)
        `when`(transactionResponse.resultCodeString).thenReturn("tx_failed")
        
        // When
        val result = recoveryService.validateTransactionIntegrity(transactionId)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `test rollback transaction success`() = runTest {
        // Given
        val transactionId = "test-tx-id"
        
        // Mock responses
        `when`(server.transactions().transaction(transactionId)).thenReturn(transactionResponse)
        `when`(transactionResponse.sourceAccount).thenReturn("source-account")
        `when`(server.accounts().account("source-account")).thenReturn(accountResponse)
        `when`(server.submitTransaction(mockk<Transaction>())).thenReturn(submitResponse)
        `when`(submitResponse.isSuccess).thenReturn(true)
        `when`(submitResponse.hash).thenReturn("rollback-tx-id")
        
        // When
        val result = recoveryService.rollbackTransaction(transactionId)
        
        // Then
        assertEquals(TransactionErrorStatus.RECOVERED, result.status)
        assertTrue(result.message.contains("successfully rolled back"))
        assertEquals("rollback-tx-id", result.recoveryDetails?.get("rollback_tx"))
    }
    
    @Test
    fun `test smart contract failure handling`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.SMART_CONTRACT_FAILURE,
            message = "Smart contract execution failed",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // Mock responses
        `when`(server.transactions().transaction(error.transactionId)).thenReturn(transactionResponse)
        val resultCodes = mockk<TransactionResponse.ResultCodes>(relaxed = true)
        `when`(resultCodes.operationResultCodes).thenReturn(listOf("op_no_trust"))
        `when`(transactionResponse.extras).thenReturn(mockk(relaxed = true) {
            every { resultCodes } returns resultCodes
        })
        
        // When
        val result = recoveryService.attemptRecovery(error)
        
        // Then
        assertEquals(TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED, result.status)
        assertTrue(result.message.contains("Trust line not established"))
        assertEquals("establish_trust_line", result.recoveryDetails?.get("required_action"))
    }
    
    @Test
    fun `test escrow verification failure`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.ESCROW_VERIFICATION_FAILED,
            message = "Escrow verification failed",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // Mock responses
        `when`(server.transactions().transaction(error.transactionId)).thenReturn(transactionResponse)
        val createAccountOp = mockk<CreateAccountOperation>(relaxed = true)
        `when`(createAccountOp.destination).thenReturn("escrow-account")
        `when`(transactionResponse.operations).thenReturn(listOf(createAccountOp))
        `when`(server.accounts().account("escrow-account")).thenReturn(accountResponse)
        `when`(accountResponse.signers).thenReturn(listOf(mockk()))
        
        // When
        val result = recoveryService.attemptRecovery(error)
        
        // Then
        assertEquals(TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED, result.status)
        assertTrue(result.message.contains("missing required signers"))
        assertEquals("1", result.recoveryDetails?.get("current_signers"))
    }
    
    @Test
    fun `test system synchronization error`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.SYSTEM_SYNCHRONIZATION_ERROR,
            message = "System synchronization failed",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // Mock responses
        `when`(server.transactions().transaction(error.transactionId)).thenReturn(transactionResponse)
        `when`(transactionResponse.sourceAccount).thenReturn("source-account")
        `when`(server.accounts().account("source-account")).thenReturn(accountResponse)
        `when`(accountResponse.sequenceNumber).thenReturn(100L)
        `when`(transactionResponse.sourceAccountSequence).thenReturn(200L)
        
        // When
        val result = recoveryService.attemptRecovery(error)
        
        // Then
        assertEquals(TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED, result.status)
        assertTrue(result.message.contains("sequence number mismatch"))
        assertEquals("100", result.recoveryDetails?.get("current_sequence"))
        assertEquals("200", result.recoveryDetails?.get("transaction_sequence"))
    }
    
    @Test
    fun `test manual intervention request`() = runTest {
        // Given
        val error = TransactionError(
            type = TransactionErrorType.SMART_CONTRACT_FAILURE,
            message = "Smart contract execution failed",
            transactionId = "test-tx-id",
            walletAddress = "test-wallet"
        )
        
        // When
        val ticketId = recoveryService.requestManualIntervention(error, "Smart contract needs review")
        
        // Then
        assertTrue(ticketId.startsWith("TKT-"))
        val activeRecoveries = recoveryService.getActiveRecoveries().first()
        val updatedError = activeRecoveries.find { it.id == error.id }
        assertEquals(TransactionErrorStatus.MANUAL_INTERVENTION_REQUIRED, updatedError?.status)
        assertEquals(ticketId, updatedError?.errorDetails?.get("ticket_id"))
    }
} 