package com.example.fideicomisoapproverring.transaction

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.fideicomisoapproverring.R
import com.google.android.material.progressindicator.CircularProgressIndicator

class TransactionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val statusIcon: ImageView
    private val statusText: TextView
    private val progressIndicator: CircularProgressIndicator
    private val recoverySteps: TextView
    private val transactionId: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_transaction_status, this, true)
        
        statusIcon = findViewById(R.id.statusIcon)
        statusText = findViewById(R.id.statusText)
        progressIndicator = findViewById(R.id.progressIndicator)
        recoverySteps = findViewById(R.id.recoverySteps)
        transactionId = findViewById(R.id.transactionId)
    }

    fun showTransactionInProgress(message: String = "Processing Transaction") {
        statusIcon.visibility = View.GONE
        progressIndicator.visibility = View.VISIBLE
        statusText.text = message
        recoverySteps.visibility = View.GONE
    }

    fun showTransactionSuccess(message: String = "Transaction Successful") {
        val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.gentle_bounce)
        
        progressIndicator.visibility = View.GONE
        statusIcon.setImageResource(R.drawable.check_circle)
        statusIcon.visibility = View.VISIBLE
        statusIcon.startAnimation(bounceAnimation)
        statusText.text = message
        recoverySteps.visibility = View.GONE
    }

    fun showTransactionError(
        error: TransactionErrorHandler.TransactionError,
        friendlyMessage: String,
        steps: String
    ) {
        val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake)
        
        progressIndicator.visibility = View.GONE
        statusIcon.setImageResource(R.drawable.error_circle)
        statusIcon.visibility = View.VISIBLE
        statusIcon.startAnimation(shakeAnimation)
        statusText.text = friendlyMessage
        recoverySteps.text = steps
        recoverySteps.visibility = View.VISIBLE
        transactionId.text = "Transaction ID: ${error.transactionId}"
        transactionId.visibility = View.VISIBLE
    }

    fun showRetryingTransaction(attempt: Int, maxAttempts: Int) {
        progressIndicator.visibility = View.VISIBLE
        statusIcon.visibility = View.GONE
        statusText.text = "Retrying Transaction (Attempt $attempt of $maxAttempts)"
        recoverySteps.visibility = View.GONE
    }

    fun showRollbackInProgress() {
        progressIndicator.visibility = View.VISIBLE
        statusIcon.visibility = View.GONE
        statusText.text = "Rolling Back Transaction"
        recoverySteps.visibility = View.GONE
    }

    fun showRollbackComplete() {
        val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.gentle_bounce)
        
        progressIndicator.visibility = View.GONE
        statusIcon.setImageResource(R.drawable.rollback_complete)
        statusIcon.visibility = View.VISIBLE
        statusIcon.startAnimation(bounceAnimation)
        statusText.text = "Transaction Rolled Back Successfully"
        recoverySteps.visibility = View.GONE
    }
} 