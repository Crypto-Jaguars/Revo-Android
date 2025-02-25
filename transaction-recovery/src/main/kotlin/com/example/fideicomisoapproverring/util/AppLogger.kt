package com.example.fideicomisoapproverring.util

import android.util.Log
import com.example.fideicomisoapproverring.BuildConfig

object AppLogger {
    private const val TAG = "TransactionRecovery"

    fun d(component: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG:$component", message)
        }
    }

    fun i(component: String, message: String) {
        Log.i("$TAG:$component", message)
    }

    fun w(component: String, message: String, throwable: Throwable? = null) {
        Log.w("$TAG:$component", message, throwable)
    }

    fun e(component: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG:$component", message, throwable)
    }

    // Specialized logging methods for different components
    object Recovery {
        fun debug(message: String) = Log.d("$TAG:Recovery", message)
        fun info(message: String) = Log.i("$TAG:Recovery", message)
        fun warning(message: String) = Log.w("$TAG:Recovery", message)
        fun error(message: String) = Log.e("$TAG:Recovery", message)
        fun error(message: String, throwable: Throwable) = Log.e("$TAG:Recovery", message, throwable)
    }

    object Transaction {
        fun debug(message: String) = Log.d("$TAG:Transaction", message)
        fun info(message: String) = Log.i("$TAG:Transaction", message)
        fun warning(message: String) = Log.w("$TAG:Transaction", message)
        fun error(message: String) = Log.e("$TAG:Transaction", message)
        fun error(message: String, throwable: Throwable) = Log.e("$TAG:Transaction", message, throwable)
    }

    object Wallet {
        fun debug(message: String) = Log.d("$TAG:Wallet", message)
        fun info(message: String) = Log.i("$TAG:Wallet", message)
        fun warning(message: String) = Log.w("$TAG:Wallet", message)
        fun error(message: String) = Log.e("$TAG:Wallet", message)
        fun error(message: String, throwable: Throwable) = Log.e("$TAG:Wallet", message, throwable)
    }

    object Network {
        fun debug(message: String) = Log.d("$TAG:Network", message)
        fun info(message: String) = Log.i("$TAG:Network", message)
        fun warning(message: String) = Log.w("$TAG:Network", message)
        fun error(message: String) = Log.e("$TAG:Network", message)
        fun error(message: String, throwable: Throwable) = Log.e("$TAG:Network", message, throwable)
    }
} 