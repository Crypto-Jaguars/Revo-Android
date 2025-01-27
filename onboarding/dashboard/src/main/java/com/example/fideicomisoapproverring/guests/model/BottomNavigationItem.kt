package com.example.fideicomisoapproverring.guests.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.theme.icons.RingCore
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcCalendarChecked
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcWallet
import com.example.fideicomisoapproverring.dashboard.R

sealed class BottomNavigationItem(val route: String, val icon: ImageVector, @StringRes val label: Int) {
    object Home : BottomNavigationItem(Routes.Home.value, Icons.Default.Home, R.string.label_home)
    object Wallet : BottomNavigationItem(Routes.Wallet.value, RingCore.IcWallet, R.string.label_wallet)
    object Activity : BottomNavigationItem(Routes.Activity.value, RingCore.IcCalendarChecked, R.string.label_activity)
    object Search : BottomNavigationItem(Routes.Search.value, Icons.Outlined.Search, R.string.label_search)

    companion object {
        fun values(): List<BottomNavigationItem> {
            return listOf(Home, Wallet, Activity, Search)
        }
    }
}
