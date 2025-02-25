package com.example.fideicomisoapproverring.core.logging;

import android.util.Log;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u0003\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001:\u0004#$%&B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000fJ\"\u0010\u001e\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000f2\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010 J\u0016\u0010!\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000fJ\"\u0010\"\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000f2\n\b\u0002\u0010\u001f\u001a\u0004\u0018\u00010 R\u001f\u0010\u0003\u001a\u00060\u0004R\u00020\u00008FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001f\u0010\t\u001a\u00060\nR\u00020\u00008FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082D\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u0010\u001a\u00060\u0011R\u00020\u00008FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\b\u001a\u0004\b\u0012\u0010\u0013R\u001f\u0010\u0015\u001a\u00060\u0016R\u00020\u00008FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\b\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006\'"}, d2 = {"Lcom/example/fideicomisoapproverring/core/logging/AppLogger;", "", "()V", "network", "Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Network;", "getNetwork", "()Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Network;", "network$delegate", "Lkotlin/Lazy;", "recovery", "Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Recovery;", "getRecovery", "()Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Recovery;", "recovery$delegate", "tag", "", "transaction", "Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Transaction;", "getTransaction", "()Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Transaction;", "transaction$delegate", "wallet", "Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Wallet;", "getWallet", "()Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Wallet;", "wallet$delegate", "d", "", "component", "message", "e", "throwable", "", "i", "w", "Network", "Recovery", "Transaction", "Wallet", "core_debug"})
public final class AppLogger {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tag = "RevolutionaryFarmers";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy recovery$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy transaction$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy wallet$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy network$delegate = null;
    
    @javax.inject.Inject()
    public AppLogger() {
        super();
    }
    
    public final void d(@org.jetbrains.annotations.NotNull()
    java.lang.String component, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void i(@org.jetbrains.annotations.NotNull()
    java.lang.String component, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void w(@org.jetbrains.annotations.NotNull()
    java.lang.String component, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.lang.Throwable throwable) {
    }
    
    public final void e(@org.jetbrains.annotations.NotNull()
    java.lang.String component, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.lang.Throwable throwable) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.fideicomisoapproverring.core.logging.AppLogger.Recovery getRecovery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.fideicomisoapproverring.core.logging.AppLogger.Transaction getTransaction() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.fideicomisoapproverring.core.logging.AppLogger.Wallet getWallet() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.fideicomisoapproverring.core.logging.AppLogger.Network getNetwork() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\f"}, d2 = {"Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Network;", "", "(Lcom/example/fideicomisoapproverring/core/logging/AppLogger;)V", "debug", "", "message", "", "error", "throwable", "", "info", "warning", "core_debug"})
    public final class Network {
        
        public Network() {
            super();
        }
        
        public final void debug(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void info(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void warning(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\f"}, d2 = {"Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Recovery;", "", "(Lcom/example/fideicomisoapproverring/core/logging/AppLogger;)V", "debug", "", "message", "", "error", "throwable", "", "info", "warning", "core_debug"})
    public final class Recovery {
        
        public Recovery() {
            super();
        }
        
        public final void debug(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void info(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void warning(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\f"}, d2 = {"Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Transaction;", "", "(Lcom/example/fideicomisoapproverring/core/logging/AppLogger;)V", "debug", "", "message", "", "error", "throwable", "", "info", "warning", "core_debug"})
    public final class Transaction {
        
        public Transaction() {
            super();
        }
        
        public final void debug(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void info(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void warning(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0003\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\f"}, d2 = {"Lcom/example/fideicomisoapproverring/core/logging/AppLogger$Wallet;", "", "(Lcom/example/fideicomisoapproverring/core/logging/AppLogger;)V", "debug", "", "message", "", "error", "throwable", "", "info", "warning", "core_debug"})
    public final class Wallet {
        
        public Wallet() {
            super();
        }
        
        public final void debug(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void info(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void warning(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
        
        public final void error(@org.jetbrains.annotations.NotNull()
        java.lang.String message, @org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
        }
    }
}