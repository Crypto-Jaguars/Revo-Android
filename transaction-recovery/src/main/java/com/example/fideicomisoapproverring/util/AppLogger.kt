package com.example.fideicomisoapproverring.util

import android.util.Log
import com.example.fideicomisoapproverring.BuildConfig

object AppLogger {
    private const val TAG_PREFIX = "RevolutionaryFarmers"

    fun d(component: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX:$component", message)
        }
    }

    fun i(component: String, message: String) {
        Log.i("$TAG_PREFIX:$component", message)
    }

    fun w(component: String, message: String, throwable: Throwable? = null) {
        Log.w("$TAG_PREFIX:$component", message, throwable)
    }

    fun e(component: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG_PREFIX:$component", message, throwable)
    }

    // Specialized logging methods for different components
    object Recovery {
        private const val TAG = "$TAG_PREFIX:Recovery"

        fun debug(message: String) = Log.d(TAG, message)
        fun info(message: String) = Log.i(TAG, message)
        fun warning(message: String) = Log.w(TAG, message)
        fun error(message: String) = Log.e(TAG, message)
        fun error(message: String, throwable: Throwable) = Log.e(TAG, message, throwable)
    }

    object Transaction {
        private const val TAG = "$TAG_PREFIX:Transaction"

        fun debug(message: String) = Log.d(TAG, message)
        fun info(message: String) = Log.i(TAG, message)
        fun warning(message: String) = Log.w(TAG, message)
        fun error(message: String) = Log.e(TAG, message)
        fun error(message: String, throwable: Throwable) = Log.e(TAG, message, throwable)
    }

    object Wallet {
        private const val TAG = "$TAG_PREFIX:Wallet"

        fun debug(message: String) = Log.d(TAG, message)
        fun info(message: String) = Log.i(TAG, message)
        fun warning(message: String) = Log.w(TAG, message)
        fun error(message: String) = Log.e(TAG, message)
        fun error(message: String, throwable: Throwable) = Log.e(TAG, message, throwable)
    }

    object Network {
        private const val TAG = "$TAG_PREFIX:Network"

        fun debug(message: String) = Log.d(TAG, message)
        fun info(message: String) = Log.i(TAG, message)
        fun warning(message: String) = Log.w(TAG, message)
        fun error(message: String) = Log.e(TAG, message)
        fun error(message: String, throwable: Throwable) = Log.e(TAG, message, throwable)
    }
} 