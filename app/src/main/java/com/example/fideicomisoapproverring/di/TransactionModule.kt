package com.example.fideicomisoapproverring.di

import com.example.fideicomisoapproverring.recovery.logging.SecureAuditLogger
import com.example.fideicomisoapproverring.recovery.transaction.AtomicTransactionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.stellar.sdk.Server
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TransactionModule {
    companion object {
        @Provides
        @Singleton
        fun provideServer(): Server {
            return Server("https://horizon-testnet.stellar.org")
        }
        
        @Provides
        @Singleton
        fun provideSecureAuditLogger(): SecureAuditLogger {
            return SecureAuditLogger()
        }
        
        @Provides
        @Singleton
        fun provideAtomicTransactionManager(
            server: Server,
            secureAuditLogger: SecureAuditLogger
        ): AtomicTransactionManager {
            return AtomicTransactionManager(server, secureAuditLogger)
        }
    }
} 