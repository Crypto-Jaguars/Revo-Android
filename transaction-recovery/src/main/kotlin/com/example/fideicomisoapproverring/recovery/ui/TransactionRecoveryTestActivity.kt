package com.example.fideicomisoapproverring.recovery.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.recovery.model.TransactionError
import com.example.fideicomisoapproverring.recovery.model.TransactionErrorType
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import com.example.fideicomisoapproverring.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class TransactionRecoveryTestActivity : AppCompatActivity() {
    
    @Inject
    lateinit var recoveryService: TransactionRecoveryService
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    private lateinit var statusText: TextView
    private lateinit var networkErrorButton: Button
    private lateinit var fundsErrorButton: Button
    private lateinit var contractErrorButton: Button
    private lateinit var walletErrorButton: Button
    private lateinit var escrowErrorButton: Button
    private lateinit var viewLogsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.Recovery.debug("Creating TransactionRecoveryTestActivity")
        setContentView(R.layout.activity_transaction_recovery_test)
        
        initializeViews()
        setupClickListeners()
        observeRecoveryStatus()
        
        AppLogger.Recovery.info("TransactionRecoveryTestActivity initialized")
    }
    
    private fun initializeViews() {
        AppLogger.Recovery.debug("Initializing views")
        statusText = findViewById(R.id.statusText)
        networkErrorButton = findViewById(R.id.btnNetworkError)
        fundsErrorButton = findViewById(R.id.btnInsufficientFunds)
        contractErrorButton = findViewById(R.id.btnSmartContractError)
        walletErrorButton = findViewById(R.id.btnWalletError)
        escrowErrorButton = findViewById(R.id.btnEscrowError)
        viewLogsButton = findViewById(R.id.btnViewLogs)
    }
    
    private fun setupClickListeners() {
        AppLogger.Recovery.debug("Setting up click listeners")
        
        networkErrorButton.setOnClickListener {
            AppLogger.Recovery.info("Network error simulation requested")
            simulateError(TransactionErrorType.BLOCKCHAIN_NETWORK_CONGESTION)
        }
        
        fundsErrorButton.setOnClickListener {
            AppLogger.Recovery.info("Insufficient funds error simulation requested")
            simulateError(TransactionErrorType.INSUFFICIENT_FUNDS)
        }
        
        contractErrorButton.setOnClickListener {
            AppLogger.Recovery.info("Smart contract error simulation requested")
            simulateError(TransactionErrorType.SMART_CONTRACT_FAILURE)
        }
        
        walletErrorButton.setOnClickListener {
            AppLogger.Recovery.info("Wallet connection error simulation requested")
            simulateError(TransactionErrorType.WALLET_CONNECTION_LOST)
        }
        
        escrowErrorButton.setOnClickListener {
            AppLogger.Recovery.info("Escrow verification error simulation requested")
            simulateError(TransactionErrorType.ESCROW_VERIFICATION_FAILED)
        }
        
        viewLogsButton.setOnClickListener {
            AppLogger.Recovery.info("View logs")
            // Implementation of view logs button
        }
    }
    
    private fun simulateError(errorType: TransactionErrorType) {
        val transactionId = UUID.randomUUID().toString()
        AppLogger.Recovery.info("Simulating error type: $errorType for transaction: $transactionId")
        
        val error = TransactionError(
            transactionId = transactionId,
            type = errorType,
            message = "Simulated $errorType error",
            walletAddress = "GDJHKUQY7HKMD4HKFHFHTGF45JFUY7HFHT6GHJK"
        )
        
        coroutineScope.launch {
            try {
                AppLogger.Recovery.debug("Reporting simulated error to recovery service")
                recoveryService.reportError(error).collect { result ->
                    AppLogger.Recovery.info("Error reported, initiating recovery")
                    recoveryService.initiateRecovery(error)
                    appendLog("Error reported: ${error.id}\nStatus: ${result.status}\nMessage: ${result.message}")
                }
            } catch (e: Exception) {
                AppLogger.Recovery.error("Failed to simulate error", e)
                appendLog("Error simulation failed: ${e.message}")
            }
        }
    }
    
    private fun observeRecoveryStatus() {
        AppLogger.Recovery.debug("Setting up recovery status observer")
        coroutineScope.launch {
            try {
                recoveryService.recoveryStatus.collect { statusMap ->
                    AppLogger.Recovery.debug("Recovery status updated: ${statusMap.size} active recoveries")
                    statusMap.values.forEach { error ->
                        appendLog("Recovery Status Update:\nError ID: ${error.id}\nStatus: ${error.status}")
                    }
                }
            } catch (e: Exception) {
                AppLogger.Recovery.error("Failed to observe recovery status", e)
                appendLog("Failed to observe recovery status: ${e.message}")
            }
        }
    }
    
    private fun appendLog(message: String) {
        AppLogger.Recovery.debug("Appending to log: $message")
        runOnUiThread {
            statusText.append("\n\n$message")
        }
    }
    
    override fun onDestroy() {
        AppLogger.Recovery.info("TransactionRecoveryTestActivity being destroyed")
        super.onDestroy()
    }
} 