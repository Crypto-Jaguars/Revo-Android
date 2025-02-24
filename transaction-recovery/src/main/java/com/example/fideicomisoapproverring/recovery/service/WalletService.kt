package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.recovery.model.WalletStatus

interface WalletService {
    suspend fun getWalletStatus(): WalletStatus
} 