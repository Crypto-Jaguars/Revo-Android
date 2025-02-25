package com.example.fideicomisoapproverring.recovery.di

import com.example.fideicomisoapproverring.recovery.service.StellarTransactionRecoveryService
import com.example.fideicomisoapproverring.recovery.service.TransactionRecoveryService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecoveryModule {
    @Binds
    @Singleton
    abstract fun bindTransactionRecoveryService(
        impl: StellarTransactionRecoveryService
    ): TransactionRecoveryService

    companion object {
        @Provides
        @Singleton
        fun provideCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
    }
} 