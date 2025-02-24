package com.example.fideicomisoapproverring.guests.ui.views

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.util.Log

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fideicomisoapproverring.dashboard.R
import com.example.fideicomisoapproverring.guests.model.AgriculturalProduct
import com.example.fideicomisoapproverring.guests.model.BottomNavigationItem
import com.example.fideicomisoapproverring.guests.model.NavigationDrawerMenuItem
import com.example.fideicomisoapproverring.guests.navigation.Routes
import com.example.fideicomisoapproverring.theme.icons.RingCore
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcArrowTopRight
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcUpwardTrend
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcWallet
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme
import kotlinx.coroutines.launch
import java.util.Locale


@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    navController: NavController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onViewAllProducts: () -> Unit = {},
    onAuthenticate: () -> Unit = {},
    onMenuClick: (String) -> Unit = {},
) {
    var openAlertDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val selectedImages = remember { mutableStateListOf<Uri>() }

    // Create an ActivityResultLauncher for the image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImages.addAll(uris)
        uris.forEach { uri ->
            Log.d("DashboardView", "Selected image URI: $uri")
        }
    }

    fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.wrapContentSize(),
                drawerShape = MaterialTheme.shapes.small,
            ) {
                ModalDrawerContentView(
                    menus = NavigationDrawerMenuItem.defaultMenus,
                    onMenuClick = onMenuClick,
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundView()
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor =
                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.1F,
                                    ),
                                titleContentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.title_market_place),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = NavigationBarDefaults.containerColor.copy(alpha = 0.25F),
                    ) {
                        val navBackStackEntry = navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry.value?.destination
                        BottomNavigationItem.values().forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = item.icon,
                                        contentDescription = stringResource(item.label),
                                    )
                                },
                                label = { Text(text = stringResource(item.label)) },
                                selected =
                                    currentDestination?.hierarchy?.any {
                                        it.hasRoute(
                                            item.route,
                                            null,
                                        )
                                    } == true,
                                onClick = {
                                    // TODO: Remove conditional clause when stubbed screens have been built
                                    if (item.route != Routes.Home.value) {
                                        openAlertDialog.value = true
                                    } else {
                                        navController.navigate(item.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // re-selecting the same item
                                            launchSingleTop = true
                                            // Restore state when re-selecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                },
                            )
                        }
                    }
                },
                containerColor = Color.Transparent,
            ) { contentPadding ->
                Column(
                    modifier =
                        Modifier
                            .wrapContentHeight()
                            .padding(contentPadding)
                            .padding(horizontal = 8.dp)
                            .verticalScroll(state = rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    HeadlineBannerView(onAuthenticate = onAuthenticate)

                    Spacer(modifier = Modifier.height(16.dp))

                    TradeStatisticCardView(
                        icon = RingCore.IcUpwardTrend,
                        label = stringResource(R.string.label_market_cap),
                        value = stringResource(R.string.currency_symbol_usd, String.format(Locale.getDefault(), "%.2f", 1820F)),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TradeStatisticCardView(
                        icon = RingCore.IcWallet,
                        label = stringResource(R.string.label_volume_24hours),
                        value = stringResource(R.string.currency_symbol_usd, String.format(Locale.getDefault(), "%.2f", 82000F)),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TradeStatisticCardView(
                        icon = RingCore.IcWallet,
                        label = stringResource(R.string.label_active_traders),
                        value = stringResource(R.string.currency_symbol_usd, String.format(Locale.getDefault(), "%.2f", 2400000F)),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AuthenticateWalletCardView(onAuthenticate = onAuthenticate)

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.title_trending_products),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TrendingProductsView(onViewAllProducts = onViewAllProducts)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            openImagePicker()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.upload_product))
                    }
                }
            }

            if (openAlertDialog.value) {
                ConnectWalletDialog(
                    onDismiss = { openAlertDialog.value = false },
                    onConfirm = { openAlertDialog.value = false },
                )
            }
        }
    }
}

@Composable
fun ModalDrawerContentView(
    menus: Array<NavigationDrawerMenuItem>,
    onMenuClick: (String) -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25F))
                .wrapContentSize()
                .clip(shape = MaterialTheme.shapes.large),
    ) {
        menus.forEach {
            if (it.title == R.string.label_connect_wallet) {
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = stringResource(it.title)) },
                    badge = { it.icon?.let { Icon(imageVector = it, contentDescription = null) } },
                    selected = false,
                    onClick = { onMenuClick(it.route) },
                )
                HorizontalDivider()
            } else {
                NavigationDrawerItem(
                    label = { Text(text = stringResource(it.title)) },
                    badge = { it.icon?.let { Icon(imageVector = it, contentDescription = null) } },
                    selected = false,
                    onClick = { onMenuClick(it.route) },
                )
            }
        }
    }
}

@Composable
fun TradeStatisticCardView(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors().copy(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.1F,
                    ),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .background(color = Color.Transparent),
        ) {
            Row(
                modifier = Modifier.background(color = Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = icon, contentDescription = null)

                Spacer(Modifier.width(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun AuthenticateWalletCardView(
    modifier: Modifier = Modifier,
    onAuthenticate: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors().copy(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.26F,
                    ),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp,
            ),
    ) {
        ConstraintLayout(
            modifier =
                Modifier
                    .wrapContentSize(),
        ) {
            val (topLeftEllipsisConstraint, bottomRightEllipsisConstraint, contentConstraint) = createRefs()

            Image(
                modifier =
                    Modifier
                        .constrainAs(topLeftEllipsisConstraint) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                        .offset(x = (-36).dp, y = (-36).dp)
                        .blur(radius = 28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(R.drawable.img_purple_ellipse),
                contentDescription = null,
            )

            Image(
                modifier =
                    Modifier
                        .constrainAs(bottomRightEllipsisConstraint) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                        .offset(x = (36).dp, y = (36).dp)
                        .blur(radius = 28.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                painter = painterResource(R.drawable.img_purple_ellipse),
                contentDescription = null,
            )

            Column(
                modifier =
                    Modifier
                        .constrainAs(contentConstraint) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .wrapContentSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .background(color = Color.Transparent),
            ) {
                Text(
                    text = stringResource(R.string.title_personalized_experience),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA896FE),
                        ),
                    // color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.msg_wallet_authentication),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAuthenticate,
                    shape = MaterialTheme.shapes.small,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDDD6FF),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.label_connect_wallet),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingProductsView(
    modifier: Modifier =
        Modifier
            .background(
                color =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.1F,
                    ),
            ),
    products: Array<AgriculturalProduct> = AgriculturalProduct.defaultItems,
    onViewAllProducts: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors().copy(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.1F,
                    ),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp,
            ),
    ) {
        Column(
            modifier =
                modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            products.forEach {
                Spacer(modifier = Modifier.height(16.dp))

                TrendingProductItemView(modifier = modifier, product = it)

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewAllProducts),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.action_view_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = RingCore.IcArrowTopRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun TrendingProductItemView(
    modifier: Modifier = Modifier.background(Color.Transparent),
    product: AgriculturalProduct,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        product.drawableRes?.let {
            Image(
                modifier = modifier.size(40.dp),
                painter = painterResource(it),
                contentDescription = null,
            )
        }

        Column(
            modifier = modifier.weight(1F),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text =
                    stringResource(
                        R.string.currency_symbol_usd,
                        String.format(Locale.getDefault(), "%.2f", product.price),
                    ),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.label_unit_prefix, product.unit),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun HeadlineBannerView(
    modifier: Modifier =
        Modifier.background(
            Color.Transparent,
        ),
    onAuthenticate: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color =
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.1F,
            ),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = modifier.padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.title_welcome_msg),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.msg_wallet_connection_perks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAuthenticate,
                shape = MaterialTheme.shapes.small,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDDD6FF),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.label_connect_wallet),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun ConnectWalletDialog(
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    AlertDialog(
        icon = {
            Icon(imageVector = RingCore.IcWallet, contentDescription = stringResource(R.string.label_connect_wallet))
        },
        title = {
            Text(
                text = "Connect wallet to continue",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )
        },
        text = {
            Column {
                Text(text = stringResource(R.string.dialog_msg_secure_transaction))

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = stringResource(R.string.dialog_msg_track_purchase))

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = stringResource(R.string.dialog_msg_unlock_features))
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
    )
}

@Preview
@Composable
private fun HeadlineBannerPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        HeadlineBannerView()
    }
}

@Preview
@Composable
private fun TrendingProductItemPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        TrendingProductItemView(
            product =
                AgriculturalProduct(
                    drawableRes = R.drawable.img_corn,
                    price = 2890.00F,
                    unit = "kg",
                ),
        )
    }
}

@Preview
@Composable
private fun TrendingProductsPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        TrendingProductsView()
    }
}

@Preview
@Composable
private fun AuthenticateWalletCardPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        AuthenticateWalletCardView()
    }
}

@Preview
@Composable
private fun TradeStatisticCardPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        TradeStatisticCardView(
            icon = RingCore.IcUpwardTrend,
            label = stringResource(R.string.label_market_cap),
            value = stringResource(R.string.currency_symbol_usd, "1.82T"),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun ModalDrawerContentPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        ModalDrawerContentView(menus = NavigationDrawerMenuItem.defaultMenus)
    }
}

@Preview
@Composable
private fun DashboardPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        DashboardView()
    }
}
