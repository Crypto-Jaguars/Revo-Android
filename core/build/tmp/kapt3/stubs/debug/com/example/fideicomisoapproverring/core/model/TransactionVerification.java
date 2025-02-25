package com.example.fideicomisoapproverring.core.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0015\b\u0086\b\u0018\u00002\u00020\u0001B1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\tH\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J=\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000bH\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u00052\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u0007H\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0004\u0010\u0011R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006 "}, d2 = {"Lcom/example/fideicomisoapproverring/core/model/TransactionVerification;", "", "transactionId", "", "isValid", "", "confirmations", "", "timestamp", "", "error", "Lcom/example/fideicomisoapproverring/core/model/TransactionError;", "(Ljava/lang/String;ZIJLcom/example/fideicomisoapproverring/core/model/TransactionError;)V", "getConfirmations", "()I", "getError", "()Lcom/example/fideicomisoapproverring/core/model/TransactionError;", "()Z", "getTimestamp", "()J", "getTransactionId", "()Ljava/lang/String;", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "hashCode", "toString", "core_debug"})
public final class TransactionVerification {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String transactionId = null;
    private final boolean isValid = false;
    private final int confirmations = 0;
    private final long timestamp = 0L;
    @org.jetbrains.annotations.Nullable()
    private final com.example.fideicomisoapproverring.core.model.TransactionError error = null;
    
    public TransactionVerification(@org.jetbrains.annotations.NotNull()
    java.lang.String transactionId, boolean isValid, int confirmations, long timestamp, @org.jetbrains.annotations.Nullable()
    com.example.fideicomisoapproverring.core.model.TransactionError error) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTransactionId() {
        return null;
    }
    
    public final boolean isValid() {
        return false;
    }
    
    public final int getConfirmations() {
        return 0;
    }
    
    public final long getTimestamp() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.fideicomisoapproverring.core.model.TransactionError getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final long component4() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.example.fideicomisoapproverring.core.model.TransactionError component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.fideicomisoapproverring.core.model.TransactionVerification copy(@org.jetbrains.annotations.NotNull()
    java.lang.String transactionId, boolean isValid, int confirmations, long timestamp, @org.jetbrains.annotations.Nullable()
    com.example.fideicomisoapproverring.core.model.TransactionError error) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}