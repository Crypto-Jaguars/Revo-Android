package com.example.fideicomisoapproverring.guests.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fideicomisoapproverring.dashboard.R
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.theme.icons.RingCore
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcAbout
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcCategories
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcFaq
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcSettings
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcWallet

data class DrawerMenu(val icon: ImageVector?, @StringRes val title: Int, val route: String) {
    companion object {
        val defaultMenus = arrayOf(
            DrawerMenu(icon = Icons.Outlined.Home, title = R.string.label_home, route = Routes.Home.name),
            DrawerMenu(icon = Icons.Outlined.Search, title = R.string.label_search, route = Routes.Search.name),
            DrawerMenu(icon = RingCore.IcCategories, title = R.string.label_categories, route = Routes.Categories.name),
            DrawerMenu(icon = RingCore.IcSettings, title = R.string.label_settings, route = Routes.Settings.name),
            DrawerMenu(icon = RingCore.IcWallet, title = R.string.label_connect_wallet, route = Routes.Wallet.name),
            DrawerMenu(icon = null, title = R.string.label_how_it_works, route = Routes.How.name),
            DrawerMenu(icon = RingCore.IcFaq, title = R.string.label_faq_help_centre, route = Routes.FAQ.name),
            DrawerMenu(icon = RingCore.IcAbout, title = R.string.label_about_us, route = Routes.About.name),
        )
    }
}
