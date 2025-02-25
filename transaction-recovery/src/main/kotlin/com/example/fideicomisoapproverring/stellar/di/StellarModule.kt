package com.example.fideicomisoapproverring.stellar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.stellar.sdk.Server
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StellarModule {
    @Provides
    @Singleton
    fun provideServer(): Server {
        return Server("https://horizon-testnet.stellar.org")
    }
} 