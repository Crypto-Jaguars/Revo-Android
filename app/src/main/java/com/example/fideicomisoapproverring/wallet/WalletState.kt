package com.example.fideicomisoapproverring.wallet

sealed class WalletState {
    object Disconnected : WalletState()
    object Connecting : WalletState()
    data class Connected(val publicKey: String) : WalletState()
    data class Error(val message: String) : WalletState()
} 