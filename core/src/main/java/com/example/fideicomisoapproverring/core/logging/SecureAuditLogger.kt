package com.example.fideicomisoapproverring.core.logging

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SecureAuditLogger @Inject constructor(
    private val context: Context,
    private val appLogger: AppLogger
) {
    private val tag = "SecureAudit"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val logMutex = Mutex()
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val logFile by lazy {
        EncryptedFile.Builder(
            File(context.filesDir, "secure_audit.log"),
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    suspend fun logEvent(
        eventType: String,
        details: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        logMutex.withLock {
            val timestamp = System.currentTimeMillis()
            val formattedDate = dateFormat.format(Date(timestamp))
            
            val logEntry = buildString {
                append("[$formattedDate] ")
                append("Type: $eventType, ")
                append("Details: $details")
                if (metadata.isNotEmpty()) {
                    append(", Metadata: $metadata")
                }
            }

            // Calculate hash of the log entry for integrity verification
            val hash = calculateHash(logEntry)
            
            // Create encrypted log entry
            val secureLogEntry = "$logEntry | Hash: $hash\n"
            
            // Write to encrypted file
            try {
                logFile.openFileOutput().use { outputStream ->
                    outputStream.write(secureLogEntry.toByteArray())
                }
                
                // Also log using AppLogger for consistency
                appLogger.i(tag, "Event logged successfully: $eventType")
            } catch (e: Exception) {
                appLogger.e(tag, "Failed to write encrypted log: ${e.message}")
                // Fallback to system log in case of encryption failure
                Log.e(tag, "Encryption failed, using fallback logging: $secureLogEntry")
            }
        }
    }

    suspend fun getLogEntries(): List<String> {
        return logMutex.withLock {
            try {
                val outputStream = ByteArrayOutputStream()
                logFile.openFileInput().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
                outputStream.toString().split("\n").filter { it.isNotEmpty() }
            } catch (e: Exception) {
                appLogger.e(tag, "Failed to read encrypted logs: ${e.message}")
                emptyList()
            }
        }
    }

    private fun calculateHash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    // Specialized audit logging methods for different scenarios
    suspend fun logTransactionEvent(
        transactionId: String,
        status: String,
        details: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val enrichedMetadata = metadata.toMutableMap().apply {
            put("transactionId", transactionId)
            put("status", status)
        }
        logEvent("TRANSACTION", details, enrichedMetadata)
    }

    suspend fun logRecoveryEvent(
        transactionId: String,
        recoveryType: String,
        details: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val enrichedMetadata = metadata.toMutableMap().apply {
            put("transactionId", transactionId)
            put("recoveryType", recoveryType)
        }
        logEvent("RECOVERY", details, enrichedMetadata)
    }

    suspend fun logSecurityEvent(
        eventType: String,
        severity: String,
        details: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val enrichedMetadata = metadata.toMutableMap().apply {
            put("severity", severity)
        }
        logEvent("SECURITY_$eventType", details, enrichedMetadata)
    }

    suspend fun logUserAction(
        userId: String,
        action: String,
        details: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val enrichedMetadata = metadata.toMutableMap().apply {
            put("userId", userId)
            put("action", action)
        }
        logEvent("USER_ACTION", details, enrichedMetadata)
    }
} 