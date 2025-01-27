package com.example.fideicomisoapproverring.guests.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.fideicomisoapproverring.dashboard.R
import com.example.fideicomisoapproverring.guests.model.AgriculturalProduct
import com.example.fideicomisoapproverring.guests.model.DrawerMenu
import com.example.fideicomisoapproverring.theme.icons.RingCore
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcUpwardTrend
import com.example.fideicomisoapproverring.theme.ui.theme.RingCoreTheme
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    onMenuClick: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.wrapContentSize(),
                drawerShape = MaterialTheme.shapes.small,
            ) {
                ModalDrawerContentView(
                    menus = DrawerMenu.defaultMenus,
                    onMenuClick = onMenuClick
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundView()
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.1F
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
                                    contentDescription = null
                                )
                            }
                        },
                        title = { Text(text = stringResource(R.string.title_greeting_prefix, stringResource(R.string.title_guest))) }
                    )
                },
                containerColor = Color.Transparent
            ) { contentPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .verticalScroll(state = rememberScrollState())
                ) {


                }
            }
        }
    }
}

@Composable
fun ModalDrawerContentView(
    menus: Array<DrawerMenu>,
    onMenuClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .clip(shape = MaterialTheme.shapes.large)
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
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.1F
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .background(color = Color.Transparent)
        ) {
            Row(
                modifier = Modifier.background(color = Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null)

                Spacer(Modifier.width(8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AuthenticateWalletCardView(
    modifier: Modifier = Modifier,
    onAuthenticate: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.26F
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .wrapContentSize()
        ) {
            val (topLeftEllipsisConstraint, bottomRightEllipsisConstraint, contentConstraint) = createRefs()

            Image(
                modifier = Modifier
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
                modifier = Modifier
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
                modifier = Modifier
                    .constrainAs(contentConstraint) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .wrapContentSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .background(color = Color.Transparent)
            ) {
                Text(
                    text = stringResource(R.string.title_personalized_experience),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA896FE)
                    ),
                    // color = MaterialTheme.colorScheme.primary

                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.msg_wallet_authentication),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAuthenticate,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.label_connect_wallet),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

    }
}

@Composable
fun TrendingProductsView(
    modifier: Modifier = Modifier,
    products: Array<AgriculturalProduct> = AgriculturalProduct.defaultItems
) {
    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.1F
            ),
            shape = MaterialTheme.shapes.medium
        ).padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium)
    ) {
        products.forEach {
            Spacer(modifier = Modifier.height(16.dp))

            TrendingProductItemView(modifier = modifier, product = it)

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun TrendingProductItemView(
    modifier: Modifier = Modifier,
    product: AgriculturalProduct,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
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
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.currency_symbol_usd, String.format(Locale.getDefault(), "%.2f", product.price)),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.label_unit_prefix, product.unit),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
private fun TrendingProductItemPreview() {
    RingCoreTheme(
        darkTheme = true,
    ) {
        TrendingProductItemView(
            product = AgriculturalProduct(drawableRes = R.drawable.img_corn, price = 2890.00F, unit = "kg"),
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
        darkTheme = true
    ) {
        AuthenticateWalletCardView()
    }
}

@Preview
@Composable
private fun TradeStatisticCardPreview() {
    RingCoreTheme(
        darkTheme = true
    ) {
        TradeStatisticCardView(
            icon = RingCore.IcUpwardTrend,
            label = stringResource(R.string.label_market_cap),
            value = stringResource(R.string.currency_symbol_usd, "1.82T")
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun ModalDrawerContentPreview() {
    RingCoreTheme(
        darkTheme = true
    ) {
        ModalDrawerContentView(menus = DrawerMenu.defaultMenus)
    }

}

@Preview
@Composable
private fun DashboardPreview() {
    RingCoreTheme(
        darkTheme = true
    ) {
        DashboardView()
    }
}