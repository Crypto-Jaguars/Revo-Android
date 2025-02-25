package com.example.fideicomisoapproverring.core.wallet;

import com.example.fideicomisoapproverring.core.model.TransactionError;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005J\u000e\u0010\u0006\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0007J%\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\n0\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fH&\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\tH&J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005J\u001e\u0010\u0014\u001a\u00020\u00122\u0006\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0016J\b\u0010\u0017\u001a\u00020\u0012H&J\u000e\u0010\u0018\u001a\u00020\u0019H\u00a6@\u00a2\u0006\u0002\u0010\u0007J\u0016\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005J\u0016\u0010\u001c\u001a\u00020\u00122\u0006\u0010\u0004\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005J\u001e\u0010\u001d\u001a\u00020\u00122\u0006\u0010\u001b\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0016\u00a8\u0006\u001f"}, d2 = {"Lcom/example/fideicomisoapproverring/core/wallet/WalletManager;", "", "exportBackup", "", "password", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTransactionHistory", "Lkotlinx/coroutines/flow/Flow;", "", "limit", "", "(Ljava/lang/Integer;)Lkotlinx/coroutines/flow/Flow;", "getWalletAddress", "getWalletStatus", "Lcom/example/fideicomisoapproverring/core/wallet/WalletStatus;", "hasSufficientFunds", "", "amount", "importBackup", "backupData", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isLocked", "lock", "", "signTransaction", "transactionData", "unlock", "verifySignature", "signature", "core_debug"})
public abstract interface WalletManager {
    
    /**
     * Get the current wallet address
     * @return The wallet address as a string
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getWalletAddress(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Get the current balance of the wallet
     * @return The balance as a BigDecimal
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBalance(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Sign a transaction with the wallet's private key
     * @param transactionData The transaction data to sign
     * @return The signed transaction data
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object signTransaction(@org.jetbrains.annotations.NotNull()
    java.lang.String transactionData, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Verify if a transaction was signed by this wallet
     * @param transactionData The transaction data
     * @param signature The signature to verify
     * @return true if the signature is valid, false otherwise
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifySignature(@org.jetbrains.annotations.NotNull()
    java.lang.String transactionData, @org.jetbrains.annotations.NotNull()
    java.lang.String signature, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * Get the transaction history for this wallet
     * @param limit Optional limit on the number of transactions to return
     * @return A flow of transaction data
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<java.lang.String>> getTransactionHistory(@org.jetbrains.annotations.Nullable()
    java.lang.Integer limit);
    
    /**
     * Check if the wallet has sufficient funds for a transaction
     * @param amount The amount to check
     * @return true if sufficient funds are available, false otherwise
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object hasSufficientFunds(@org.jetbrains.annotations.NotNull()
    java.lang.String amount, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * Lock the wallet to prevent unauthorized access
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object lock(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Unlock the wallet for use
     * @param password The password to unlock the wallet
     * @return true if successfully unlocked, false otherwise
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object unlock(@org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * Check if the wallet is currently locked
     * @return true if locked, false if unlocked
     */
    public abstract boolean isLocked();
    
    /**
     * Export the wallet's encrypted backup
     * @param password The password to encrypt the backup
     * @return The encrypted backup data
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object exportBackup(@org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Import a wallet from an encrypted backup
     * @param backupData The encrypted backup data
     * @param password The password to decrypt the backup
     * @return true if import was successful, false otherwise
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object importBackup(@org.jetbrains.annotations.NotNull()
    java.lang.String backupData, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion);
    
    /**
     * Get the current status of the wallet
     * @return A flow of wallet status updates
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.example.fideicomisoapproverring.core.wallet.WalletStatus> getWalletStatus();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}