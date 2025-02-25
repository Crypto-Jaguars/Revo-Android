package com.example.fideicomisoapproverring.recovery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fideicomisoapproverring.R
import com.example.fideicomisoapproverring.databinding.DialogTransactionRecoveryBinding
import com.example.fideicomisoapproverring.recovery.model.RecoveryStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionRecoveryDialog : DialogFragment() {

    private var _binding: DialogTransactionRecoveryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionRecoveryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTransactionRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        
        arguments?.getString(ARG_ERROR_ID)?.let { errorId ->
            viewModel.loadError(errorId)
        }
    }

    private fun setupViews() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        binding.retryButton.setOnClickListener {
            viewModel.retryRecovery()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recoveryState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: RecoveryState) {
        binding.apply {
            errorTypeText.text = getErrorTypeText(state.error)
            messageText.text = state.error.message
            
            progressIndicator.visibility = when (state.status) {
                RecoveryStatus.ANALYZING,
                RecoveryStatus.ATTEMPTING -> View.VISIBLE
                else -> View.GONE
            }
            
            statusText.text = getStatusText(state.status)
            
            retryButton.visibility = when (state.status) {
                RecoveryStatus.FAILED -> View.VISIBLE
                else -> View.GONE
            }
            
            recoveryDetailsText.apply {
                visibility = if (state.attemptCount > 0) View.VISIBLE else View.GONE
                text = getString(R.string.recovery_attempt_count, state.attemptCount, state.maxAttempts)
            }
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

    private fun getStatusText(status: RecoveryStatus): String = when (status) {
        RecoveryStatus.ANALYZING -> getString(R.string.recovery_status_analyzing)
        RecoveryStatus.ATTEMPTING -> getString(R.string.recovery_status_attempting)
        RecoveryStatus.SUCCEEDED -> getString(R.string.recovery_status_succeeded)
        RecoveryStatus.FAILED -> getString(R.string.recovery_status_failed)
        RecoveryStatus.MANUAL_INTERVENTION_REQUIRED -> getString(R.string.recovery_status_manual_intervention)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ERROR_ID = "error_id"

        fun newInstance(errorId: String) = TransactionRecoveryDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_ERROR_ID, errorId)
            }
        }
    }
} 