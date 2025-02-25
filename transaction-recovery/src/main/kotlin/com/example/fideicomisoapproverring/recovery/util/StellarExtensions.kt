package com.example.fideicomisoapproverring.recovery.util

import org.stellar.sdk.Transaction
import org.stellar.sdk.responses.TransactionResponse
import java.math.BigInteger

fun TransactionResponse.isSuccessful(): Boolean {
    return this.successful
}

fun Transaction.Builder.setTimeout(seconds: Long): Transaction.Builder = apply {
    setTimeout(BigInteger.valueOf(seconds))
} 