package com.example.fideicomisoapproverring.recovery.model

/**
 * Represents a structured guidance for recovery actions
 */
data class RecoveryActionGuidance(
    val actionType: ActionType,
    val title: String,
    val description: String,
    val steps: List<ActionStep>,
    val expectedOutcome: String,
    val alternativeActions: List<AlternativeAction>,
    val preventionTips: List<String>
)

/**
 * Represents a single step in the recovery action guidance
 */
data class ActionStep(
    val order: Int,
    val instruction: String,
    val details: String? = null,
    val isOptional: Boolean = false,
    val estimatedDuration: String? = null
)

/**
 * Represents an alternative action that can be taken
 */
data class AlternativeAction(
    val title: String,
    val description: String,
    val conditions: List<String>,
    val consequence: String
)

/**
 * Factory for creating recovery action guidance based on different scenarios
 */
object RecoveryActionGuidanceFactory {
    fun createGuidance(error: TransactionError, systemState: SystemState): RecoveryActionGuidance {
        return when (error) {
            is NetworkCongestionError -> createNetworkCongestionGuidance(error, systemState)
            is InsufficientFundsError -> createInsufficientFundsGuidance(error)
            is SmartContractError -> createSmartContractGuidance(error)
            is WalletConnectionError -> createWalletConnectionGuidance()
            is EscrowVerificationError -> createEscrowVerificationGuidance(error)
            else -> createGenericErrorGuidance(error)
        }
    }

    private fun createNetworkCongestionGuidance(
        error: NetworkCongestionError,
        systemState: SystemState
    ): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.RETRY,
            title = "Network Congestion Recovery",
            description = "The network is experiencing high traffic. Here's how to proceed:",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Wait for network conditions to improve",
                    details = "Current congestion level: ${error.congestionLevel}",
                    estimatedDuration = "5-10 minutes"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Check transaction fee settings",
                    details = "Consider increasing the fee for faster processing"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Retry the transaction",
                    details = "Use the 'Retry' button when ready"
                )
            ),
            expectedOutcome = "Transaction will be processed when network congestion decreases",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Increase Transaction Fee",
                    description = "Speed up processing by increasing the fee",
                    conditions = listOf("Additional fee required", "Wallet has sufficient balance"),
                    consequence = "Higher transaction cost but faster processing"
                ),
                AlternativeAction(
                    title = "Schedule for Later",
                    description = "Set up a notification for when network traffic is lower",
                    conditions = listOf("Non-urgent transaction"),
                    consequence = "Delayed processing but standard fees"
                )
            ),
            preventionTips = listOf(
                "Schedule non-urgent transactions during off-peak hours",
                "Keep a buffer for transaction fees",
                "Monitor network conditions before initiating large transactions"
            )
        )
    }

    private fun createInsufficientFundsGuidance(error: InsufficientFundsError): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.WALLET_VERIFICATION,
            title = "Insufficient Funds Resolution",
            description = "Your wallet doesn't have enough funds to complete this transaction.",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Check required amount",
                    details = "Required: ${error.required}, Available: ${error.available}"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Add funds to your wallet",
                    details = "Transfer the required additional amount"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Verify updated balance",
                    details = "Ensure funds are confirmed in your wallet"
                ),
                ActionStep(
                    order = 4,
                    instruction = "Retry the transaction",
                    details = "Initiate the transaction again"
                )
            ),
            expectedOutcome = "Transaction will complete successfully with sufficient funds",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Modify Transaction Amount",
                    description = "Reduce the transaction amount to match available funds",
                    conditions = listOf("Transaction amount is flexible"),
                    consequence = "Partial transaction completion"
                )
            ),
            preventionTips = listOf(
                "Maintain sufficient balance for planned transactions",
                "Account for transaction fees in total amount",
                "Set up balance alerts to prevent insufficient funds"
            )
        )
    }

    private fun createSmartContractGuidance(error: SmartContractError): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.MANUAL_INTERVENTION,
            title = "Smart Contract Error Resolution",
            description = "There was an issue with the smart contract execution.",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Review error details",
                    details = "Error code: ${error.errorCode}"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Contact support team",
                    details = "Provide transaction ID and error code"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Wait for contract verification",
                    details = "Support team will analyze the contract state"
                )
            ),
            expectedOutcome = "Support team will resolve the contract issue or provide alternative steps",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Initiate Contract Rollback",
                    description = "Request to reverse the contract state",
                    conditions = listOf("Rollback is possible", "No dependent transactions"),
                    consequence = "Contract will be reset to previous state"
                )
            ),
            preventionTips = listOf(
                "Verify contract conditions before execution",
                "Keep contract parameters within recommended ranges",
                "Monitor contract state during execution"
            )
        )
    }

    private fun createWalletConnectionGuidance(): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.WALLET_VERIFICATION,
            title = "Wallet Connection Recovery",
            description = "Your wallet connection needs to be restored.",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Check wallet application",
                    details = "Ensure your wallet app is running"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Verify wallet unlock status",
                    details = "Unlock your wallet if needed"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Reconnect wallet",
                    details = "Use the 'Connect Wallet' button"
                )
            ),
            expectedOutcome = "Wallet will be reconnected and ready for transaction",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Use Alternative Wallet",
                    description = "Switch to a different connected wallet",
                    conditions = listOf("Alternative wallet available", "Has sufficient funds"),
                    consequence = "Transaction will use different wallet"
                )
            ),
            preventionTips = listOf(
                "Keep wallet application updated",
                "Don't let wallet session expire during transactions",
                "Maintain stable internet connection"
            )
        )
    }

    private fun createEscrowVerificationGuidance(error: EscrowVerificationError): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.ESCROW_VERIFICATION,
            title = "Escrow Verification Required",
            description = "The escrow contract needs verification before proceeding.",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Review escrow details",
                    details = "Check contract ID and parameters"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Verify participant signatures",
                    details = "Ensure all required signatures are present"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Confirm escrow conditions",
                    details = "Validate release conditions and timing"
                )
            ),
            expectedOutcome = "Escrow contract will be verified and transaction can proceed",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Request Manual Verification",
                    description = "Have support team verify the escrow",
                    conditions = listOf("Support team available"),
                    consequence = "Longer verification time but additional security"
                )
            ),
            preventionTips = listOf(
                "Review escrow terms before transaction",
                "Keep required signatures ready",
                "Monitor escrow contract status"
            )
        )
    }

    private fun createGenericErrorGuidance(error: TransactionError): RecoveryActionGuidance {
        return RecoveryActionGuidance(
            actionType = ActionType.MANUAL_INTERVENTION,
            title = "Transaction Error Recovery",
            description = "An unexpected error occurred during the transaction.",
            steps = listOf(
                ActionStep(
                    order = 1,
                    instruction = "Document error details",
                    details = "Error: ${error.message}"
                ),
                ActionStep(
                    order = 2,
                    instruction = "Contact support",
                    details = "Report the issue to our support team"
                ),
                ActionStep(
                    order = 3,
                    instruction = "Await support response",
                    details = "Support team will analyze the error"
                )
            ),
            expectedOutcome = "Support team will help resolve the issue",
            alternativeActions = listOf(
                AlternativeAction(
                    title = "Retry Transaction",
                    description = "Attempt the transaction again",
                    conditions = listOf("Error is temporary"),
                    consequence = "May encounter same error if issue persists"
                )
            ),
            preventionTips = listOf(
                "Keep transaction details for reference",
                "Monitor transaction status",
                "Report unusual behavior promptly"
            )
        )
    }
} 