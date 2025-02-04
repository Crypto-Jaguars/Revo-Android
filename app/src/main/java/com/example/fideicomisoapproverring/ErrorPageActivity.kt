package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class ErrorPageActivity : AppCompatActivity() {

    companion object {
        const val ERROR_TYPE = "error_type"
        const val ERROR_404 = "404"
        const val ERROR_NETWORK = "network"
        const val ERROR_TRANSACTION = "transaction"
        const val ERROR_SYSTEM = "system"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_page)
        
        // Apply animations
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        
        // Setup background gradient
        window.decorView.setBackgroundResource(R.drawable.farm_gradient_background)
        
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<ConstraintLayout>(R.id.errorPageLayout).startAnimation(fadeIn)
        
        val errorType = intent.getStringExtra(ERROR_TYPE) ?: ERROR_SYSTEM
        setupErrorPage(errorType)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    private fun setupErrorPage(errorType: String) {
        val illustration = findViewById<ImageView>(R.id.errorIllustration)
        val title = findViewById<TextView>(R.id.errorTitle)
        val message = findViewById<TextView>(R.id.errorMessage)
        val primaryButton = findViewById<Button>(R.id.primaryActionButton)
        val secondaryButton = findViewById<Button>(R.id.secondaryActionButton)

        when (errorType) {
            ERROR_404 -> setup404Error(illustration, title, message, primaryButton, secondaryButton)
            ERROR_NETWORK -> setupNetworkError(illustration, title, message, primaryButton, secondaryButton)
            ERROR_TRANSACTION -> setupTransactionError(illustration, title, message, primaryButton, secondaryButton)
            else -> setupSystemError(illustration, title, message, primaryButton, secondaryButton)
        }

        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.gentle_bounce)
        illustration.startAnimation(bounceAnimation)
    }

    private fun setup404Error(
        illustration: ImageView,
        title: TextView,
        message: TextView,
        primaryButton: Button,
        secondaryButton: Button
    ) {
        illustration.setImageResource(R.drawable.lost_in_fields)
        title.text = "Lost in the Fields"
        message.text = "Looks like we've wandered into an empty field. The crop you're looking for isn't growing here."
        primaryButton.text = "Return to Home"
        secondaryButton.text = "Contact Support"

        primaryButton.setOnClickListener {
            startActivity(Intent(this, FindEscrowActivity::class.java))
            finish()
        }

        setupErrorReporting(ERROR_404, secondaryButton)
    }

    private fun setupNetworkError(
        illustration: ImageView,
        title: TextView,
        message: TextView,
        primaryButton: Button,
        secondaryButton: Button
    ) {
        illustration.setImageResource(R.drawable.no_signal)
        title.text = "Connection Lost"
        message.text = "Seems like we're having trouble connecting to the farm. Check your internet connection and try again."
        primaryButton.text = "Try Again"
        secondaryButton.text = "Work Offline"

        primaryButton.setOnClickListener {
            recreate()
        }

        setupErrorReporting(ERROR_NETWORK, secondaryButton)
    }

    private fun setupTransactionError(
        illustration: ImageView,
        title: TextView,
        message: TextView,
        primaryButton: Button,
        secondaryButton: Button
    ) {
        illustration.setImageResource(R.drawable.transaction_failed)
        title.text = getString(R.string.error_transaction_title)
        message.text = getString(R.string.error_transaction_message)
        primaryButton.text = getString(R.string.button_try_again)
        secondaryButton.text = getString(R.string.button_contact_support)

        primaryButton.setOnClickListener {
            finish()
        }

        setupErrorReporting(ERROR_TRANSACTION, secondaryButton)
    }

    private fun setupSystemError(
        illustration: ImageView,
        title: TextView,
        message: TextView,
        primaryButton: Button,
        secondaryButton: Button
    ) {
        illustration.setImageResource(R.drawable.system_error)
        title.text = getString(R.string.error_system_title)
        message.text = getString(R.string.error_system_message)
        primaryButton.text = getString(R.string.button_go_home)
        secondaryButton.text = getString(R.string.button_contact_support)

        primaryButton.setOnClickListener {
            val intent = Intent(this, FindEscrowActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        setupErrorReporting(ERROR_SYSTEM, secondaryButton)
    }

    private fun setupErrorReporting(errorType: String, secondaryButton: Button) {
        secondaryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@revolutionaryfarmers.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Error Report: $errorType")
                putExtra(Intent.EXTRA_TEXT, "Error Type: $errorType\nDevice: ${android.os.Build.MODEL}")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send Error Report"))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 