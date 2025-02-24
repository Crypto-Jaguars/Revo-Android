package com.example.fideicomisoapproverring.recovery.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.core.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.model.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecoveryStatusActivity : AppCompatActivity() {
    @Inject
    lateinit var auditLogger: SecureAuditLogger

    private val viewModel: RecoveryStatusViewModel by viewModels()

    private lateinit var transactionIdText: MaterialTextView
    private lateinit var statusText: MaterialTextView
    private lateinit var errorText: MaterialTextView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var statusCard: MaterialCardView
    private lateinit var errorCard: MaterialCardView
    private lateinit var actionButtonsContainer: MaterialCardView
    private lateinit var retryButton: MaterialButton
    private lateinit var supportButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_status)

        initializeViews()
        setupClickListeners()
        setupObservers()

        // Get transaction ID from intent
        intent.getStringExtra(EXTRA_TRANSACTION_ID)?.let { transactionId ->
            transactionIdText.text = transactionId
            viewModel.startMonitoring(transactionId)
        } ?: run {
            showError("No transaction ID provided")
            finish()
        }
    }

    private fun initializeViews() {
        transactionIdText = findViewById(R.id.transactionIdText)
        statusText = findViewById(R.id.statusText)
        errorText = findViewById(R.id.errorText)
        progressIndicator = findViewById(R.id.progressIndicator)
        statusCard = findViewById(R.id.statusCard)
        errorCard = findViewById(R.id.errorCard)
        actionButtonsContainer = findViewById(R.id.actionButtonsContainer)
        retryButton = findViewById(R.id.retryButton)
        supportButton = findViewById(R.id.supportButton)
        cancelButton = findViewById(R.id.cancelButton)
    }

    private fun setupClickListeners() {
        retryButton.setOnClickListener {
            viewModel.retryRecovery()
        }

        supportButton.setOnClickListener {
            viewModel.requestSupport()
        }

        cancelButton.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recoveryState.collect { state ->
                    updateUI(state)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userActions.collect { action ->
                    handleUserAction(action)
                }
            }
        }
    }

    private fun updateUI(state: RecoveryState) {
        when (state) {
            is RecoveryState.Analyzing -> showAnalyzing()
            is RecoveryState.Recovering -> showRecovering(state.progress)
            is RecoveryState.RequiresAction -> showUserActionRequired(state.action)
            is RecoveryState.Completed -> showCompleted(state.result)
            is RecoveryState.Failed -> showFailed(state.error)
        }
    }

    private fun showAnalyzing() {
        statusText.text = "Analyzing transaction..."
        progressIndicator.isVisible = true
        progressIndicator.isIndeterminate = true
        errorCard.isVisible = false
        actionButtonsContainer.isVisible = false
    }

    private fun showRecovering(progress: Int) {
        statusText.text = "Recovering transaction..."
        progressIndicator.isVisible = true
        progressIndicator.isIndeterminate = false
        progressIndicator.progress = progress
        errorCard.isVisible = false
        actionButtonsContainer.isVisible = false
    }

    private fun showUserActionRequired(action: UserAction) {
        progressIndicator.isVisible = false
        errorCard.isVisible = true
        actionButtonsContainer.isVisible = true

        when (action) {
            is UserAction.AddFunds -> {
                statusText.text = "Additional funds required"
                errorText.text = "Insufficient funds. You need ${action.required} ${action.currency} but only have ${action.available} ${action.currency}."
                retryButton.isVisible = false
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
            is UserAction.RequestSupport -> {
                statusText.text = "Support required"
                errorText.text = "This transaction requires manual intervention from our support team."
                retryButton.isVisible = false
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
            is UserAction.ReconnectWallet -> {
                statusText.text = "Wallet connection required"
                errorText.text = "Please reconnect your wallet to continue."
                retryButton.isVisible = true
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
            is UserAction.WaitForNetwork -> {
                statusText.text = "Network congestion"
                errorText.text = "The network is currently congested. Please try again later."
                retryButton.isVisible = true
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
            is UserAction.VerifyEscrow -> {
                statusText.text = "Escrow verification required"
                errorText.text = "Please verify the escrow contract details."
                retryButton.isVisible = true
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
            is UserAction.Retry -> {
                statusText.text = "Recovery failed"
                errorText.text = "The recovery process failed. Would you like to try again?"
                retryButton.isVisible = true
                supportButton.isVisible = true
                cancelButton.isVisible = true
            }
        }
    }

    private fun showCompleted(result: TransactionStatus) {
        statusText.text = "Recovery completed"
        progressIndicator.isVisible = false
        errorCard.isVisible = false
        actionButtonsContainer.isVisible = false
        showSuccessMessage()
        finish()
    }

    private fun showFailed(error: TransactionError) {
        statusText.text = "Recovery failed"
        progressIndicator.isVisible = false
        errorCard.isVisible = true
        errorText.text = error.getUserFriendlyMessage()
        actionButtonsContainer.isVisible = true
        retryButton.isVisible = true
        supportButton.isVisible = true
        cancelButton.isVisible = true
    }

    private fun showCancelConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cancel Recovery")
            .setMessage("Are you sure you want to cancel the recovery process? This action cannot be undone.")
            .setPositiveButton("Cancel Recovery") { _, _ ->
                viewModel.cancelRecovery()
                finish()
            }
            .setNegativeButton("Continue Recovery", null)
            .show()
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Transaction recovery completed successfully",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun showError(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun handleUserAction(action: UserAction) {
        when (action) {
            is UserAction.Retry -> viewModel.retryRecovery()
            is UserAction.Cancel -> viewModel.cancelRecovery()
            is UserAction.RequestSupport -> viewModel.requestSupport()
        }
    }

    companion object {
        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    }
}

sealed class RecoveryState {
    object Analyzing : RecoveryState()
    data class Recovering(val progress: Int) : RecoveryState()
    data class RequiresAction(val action: UserAction) : RecoveryState()
    data class Completed(val result: TransactionStatus) : RecoveryState()
    data class Failed(val error: TransactionError) : RecoveryState()
}

sealed class UserAction {
    object Retry : UserAction()
    object Cancel : UserAction()
    object RequestSupport : UserAction()
} 