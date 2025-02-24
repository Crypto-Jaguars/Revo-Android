private fun updateUI(state: RecoveryState) {
    binding.apply {
        // Update error type and basic message
        errorTypeText.text = getErrorTypeText(state.error)
        messageText.text = getErrorDetailsText(state.error)
        
        // Show/hide progress indicator based on status
        progressIndicator.visibility = when (state.status) {
            RecoveryStatus.ANALYZING,
            RecoveryStatus.ATTEMPTING -> View.VISIBLE
            else -> View.GONE
        }
        
        // Update status text with more detailed progress information
        statusText.text = getStatusText(state.status)
        
        // Show action guidance
        actionGuidance.apply {
            text = getActionGuidanceText(state.error)
            visibility = View.VISIBLE
        }
        
        // Update recovery attempt counter if applicable
        recoveryDetailsText.apply {
            visibility = if (state.attemptCount > 0) View.VISIBLE else View.GONE
            text = getString(R.string.recovery_attempt_count, state.attemptCount, state.maxAttempts)
        }
        
        // Show error prevention tips when appropriate
        errorPreventionTips.apply {
            visibility = if (shouldShowPreventionTips(state.error)) View.VISIBLE else View.GONE
            text = getPreventionTips(state.error)
        }
        
        // Configure action buttons based on error type
        setupActionButtons(state)
    }
}

private fun getErrorTypeText(error: TransactionError): String = when (error) {
    is NetworkCongestionError -> getString(R.string.error_network_congestion)
    is InsufficientFundsError -> getString(R.string.error_insufficient_funds)
    is SmartContractError -> getString(R.string.error_smart_contract)
    is WalletConnectionError -> getString(R.string.error_wallet_connection)
    is EscrowVerificationError -> getString(R.string.error_escrow_verification)
    else -> getString(R.string.error_unknown)
}

private fun getErrorDetailsText(error: TransactionError): String = when (error) {
    is NetworkCongestionError -> getString(R.string.error_network_congestion_details)
    is InsufficientFundsError -> getString(
        R.string.error_insufficient_funds_details,
        error.required,
        error.available
    )
    is SmartContractError -> getString(
        R.string.error_smart_contract_details,
        error.errorCode
    )
    is WalletConnectionError -> getString(R.string.error_wallet_connection_details)
    is EscrowVerificationError -> getString(R.string.error_escrow_verification_details)
    else -> getString(R.string.error_unknown_details)
}

private fun getActionGuidanceText(error: TransactionError): String = when (error) {
    is NetworkCongestionError -> getString(R.string.error_network_congestion_action)
    is InsufficientFundsError -> getString(R.string.error_insufficient_funds_action)
    is SmartContractError -> getString(R.string.error_smart_contract_action)
    is WalletConnectionError -> getString(R.string.error_wallet_connection_action)
    is EscrowVerificationError -> getString(R.string.error_escrow_verification_action)
    else -> getString(R.string.error_unknown_action)
}

private fun getStatusText(status: RecoveryStatus): String = when (status) {
    RecoveryStatus.ANALYZING -> getString(R.string.recovery_status_analyzing)
    RecoveryStatus.ATTEMPTING -> getString(R.string.recovery_status_attempting)
    RecoveryStatus.SUCCEEDED -> getString(R.string.recovery_status_succeeded)
    RecoveryStatus.FAILED -> getString(R.string.recovery_status_failed)
    RecoveryStatus.MANUAL_INTERVENTION_REQUIRED -> getString(R.string.recovery_status_manual_intervention)
}

private fun shouldShowPreventionTips(error: TransactionError): Boolean {
    return when (error) {
        is NetworkCongestionError,
        is InsufficientFundsError,
        is WalletConnectionError -> true
        else -> false
    }
}

private fun getPreventionTips(error: TransactionError): String {
    val tips = StringBuilder(getString(R.string.tip_prevent_errors))
    tips.append("\n")
    
    when (error) {
        is NetworkCongestionError -> tips.append(getString(R.string.tip_network_congestion))
        is InsufficientFundsError -> tips.append(getString(R.string.tip_funds))
        is WalletConnectionError -> tips.append(getString(R.string.tip_wallet_connection))
        is SmartContractError -> tips.append(getString(R.string.tip_contracts))
    }
    
    return tips.toString()
}

private fun setupActionButtons(state: RecoveryState) {
    binding.apply {
        when (state.error) {
            is NetworkCongestionError -> {
                primaryButton.text = getString(R.string.action_retry_later)
                secondaryButton.text = getString(R.string.action_retry)
            }
            is InsufficientFundsError -> {
                primaryButton.text = getString(R.string.action_add_funds)
                secondaryButton.text = getString(R.string.action_support)
            }
            is WalletConnectionError -> {
                primaryButton.text = getString(R.string.action_verify_wallet)
                secondaryButton.text = getString(R.string.action_support)
            }
            is EscrowVerificationError -> {
                primaryButton.text = getString(R.string.action_verify_escrow)
                secondaryButton.text = getString(R.string.action_support)
            }
            else -> {
                primaryButton.text = getString(R.string.action_support)
                secondaryButton.text = getString(R.string.action_retry)
            }
        }
    }
} 