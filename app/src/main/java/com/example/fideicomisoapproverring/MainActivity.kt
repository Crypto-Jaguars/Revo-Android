// MainActivity.kt
package com.example.fideicomisoapproverring

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.guests.ui.views.DashboardView
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = Routes.Home.value) {
                composable(route = Routes.Home.value) {
                    RingCoreTheme(
                        darkTheme = true,
                    ) {
                        DashboardView(navController = navController)
                    }
                }

                composable(route = Routes.Wallet.value) {
                }

                composable(route = Routes.Activity.value) {
                }

                composable(route = Routes.Search.value) {
                }
            }
        }
    }
}
