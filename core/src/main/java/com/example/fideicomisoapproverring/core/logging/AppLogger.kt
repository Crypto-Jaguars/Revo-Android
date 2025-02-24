package com.example.fideicomisoapproverring.core.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor() {
    private val tag = "RevolutionaryFarmers"

    fun d(component: String, message: String) {
        Log.d("$tag:$component", message)
    }

    fun i(component: String, message: String) {
        Log.i("$tag:$component", message)
    }

    fun w(component: String, message: String, throwable: Throwable? = null) {
        Log.w("$tag:$component", message, throwable)
    }

    fun e(component: String, message: String, throwable: Throwable? = null) {
        Log.e("$tag:$component", message, throwable)
    }

    // Specialized logging methods for different components
    inner class Recovery {
        fun debug(message: String) = d("Recovery", message)
        fun info(message: String) = i("Recovery", message)
        fun warning(message: String) = w("Recovery", message)
        fun error(message: String) = e("Recovery", message)
        fun error(message: String, throwable: Throwable) = e("Recovery", message, throwable)
    }

    inner class Transaction {
        fun debug(message: String) = d("Transaction", message)
        fun info(message: String) = i("Transaction", message)
        fun warning(message: String) = w("Transaction", message)
        fun error(message: String) = e("Transaction", message)
        fun error(message: String, throwable: Throwable) = e("Transaction", message, throwable)
    }

    inner class Wallet {
        fun debug(message: String) = d("Wallet", message)
        fun info(message: String) = i("Wallet", message)
        fun warning(message: String) = w("Wallet", message)
        fun error(message: String) = e("Wallet", message)
        fun error(message: String, throwable: Throwable) = e("Wallet", message, throwable)
    }

    inner class Network {
        fun debug(message: String) = d("Network", message)
        fun info(message: String) = i("Network", message)
        fun warning(message: String) = w("Network", message)
        fun error(message: String) = e("Network", message)
        fun error(message: String, throwable: Throwable) = e("Network", message, throwable)
    }

    // Lazy-initialized component loggers
    val recovery by lazy { Recovery() }
    val transaction by lazy { Transaction() }
    val wallet by lazy { Wallet() }
    val network by lazy { Network() }
} 