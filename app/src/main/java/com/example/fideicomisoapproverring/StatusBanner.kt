package com.example.fideicomisoapproverring
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight






    @Composable
    fun StatusBanner(status: ConnectionStatus?, onDismiss: () -> Unit) {
        status?.let {
            val backgroundColor = when (it) {
                ConnectionStatus.SUCCESS -> Color(0xFF1DB954) // Verde
                ConnectionStatus.WARNING -> Color(0xFFFFC107) // Amarillo
                ConnectionStatus.ERROR -> Color(0xFFFF4444) // Rojo
            }

            val icon = when (it) {
                ConnectionStatus.SUCCESS -> R.drawable.check
                ConnectionStatus.WARNING -> R.drawable.info
                ConnectionStatus.ERROR -> R.drawable.cancel
            }

            val message = when (it) {
                ConnectionStatus.SUCCESS -> "Connection Successfully"
                ConnectionStatus.WARNING -> "Action Required: Please check your input."
                ConnectionStatus.ERROR -> "Error: Unable to connect."
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Status Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }

