package com.example.fideicomisoapproverring.recovery.service

import com.example.fideicomisoapproverring.recovery.model.EscrowStatus

interface EscrowService {
    suspend fun getEscrowStatus(): EscrowStatus
} 