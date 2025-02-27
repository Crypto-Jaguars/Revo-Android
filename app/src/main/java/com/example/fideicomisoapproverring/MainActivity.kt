// MainActivity.kt
package com.example.fideicomisoapproverring

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start FindEscrowActivity directly
        startActivity(Intent(this, FindEscrowActivity::class.java))
        finish() // Close MainActivity
    }
}
