package com.example.fideicomisoapproverring.di

import com.example.fideicomisoapproverring.recovery.transaction.AtomicTransactionManager
import com.example.fideicomisoapproverring.stellar.StellarTransactionManager
import com.example.fideicomisoapproverring.wallet.WalletManager
import com.example.fideicomisoapproverring.util.AppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TransactionModule {
    companion object {
        @Provides
        @Singleton
        fun provideAppLogger(): AppLogger {
            return AppLogger()
        }
        
        @Provides
        @Singleton
        fun provideAtomicTransactionManager(
            stellarTransactionManager: StellarTransactionManager,
            walletManager: WalletManager,
            logger: AppLogger
        ): AtomicTransactionManager {
            return AtomicTransactionManager(stellarTransactionManager, walletManager, logger)
        }
    }
} 