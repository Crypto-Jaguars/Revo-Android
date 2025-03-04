package com.example.fideicomisoapproverring.theme.ui

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fideicomisoapproverring.theme.R
import com.example.fideicomisoapproverring.theme.ui.theme.ThemeManager
import com.example.fideicomisoapproverring.theme.ui.theme.rememberThemeManager
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    themeManager: ThemeManager,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val followSystem by themeManager.followSystem.collectAsState(initial = true)
    val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)
    val useDynamicColors by themeManager.useDynamicColors.collectAsState(initial = true)
    val useAutoTheme by themeManager.useAutoTheme.collectAsState(initial = false)
    val autoDarkStart by themeManager.autoDarkStart.collectAsState(initial = 20)
    val autoDarkEnd by themeManager.autoDarkEnd.collectAsState(initial = 6)

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.label_theme_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.label_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Follow System Switch
            ListItem(
                headlineContent = { Text(stringResource(R.string.label_follow_system)) },
                supportingContent = { Text(stringResource(R.string.label_follow_system_description)) },
                trailingContent = {
                    Switch(
                        checked = followSystem,
                        onCheckedChange = { checked ->
                            scope.launch {
                                themeManager.setFollowSystem(checked)
                            }
                        },
                        thumbContent = {
                            if (followSystem) {
                                Icon(
                                    imageVector = Icons.Default.PhonelinkSetup,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                }
            )

            if (!followSystem) {
                // Auto Theme Switch
                ListItem(
                    headlineContent = { Text(stringResource(R.string.label_auto_theme)) },
                    supportingContent = { Text(stringResource(R.string.label_auto_theme_description)) },
                    trailingContent = {
                        Switch(
                            checked = useAutoTheme,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    themeManager.setUseAutoTheme(checked)
                                }
                            },
                            thumbContent = {
                                if (useAutoTheme) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                )

                if (useAutoTheme) {
                    // Dark theme hours settings
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.label_dark_hours)) },
                        supportingContent = {
                            Text(
                                "${LocalTime.of(autoDarkStart, 0).format(DateTimeFormatter.ofPattern("HH:mm"))} - " +
                                "${LocalTime.of(autoDarkEnd, 0).format(DateTimeFormatter.ofPattern("HH:mm"))}"
                            )
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { showStartTimePicker = true }) {
                                    Icon(Icons.Default.EditCalendar, contentDescription = "Set start time")
                                }
                                IconButton(onClick = { showEndTimePicker = true }) {
                                    Icon(Icons.Default.EditCalendar, contentDescription = "Set end time")
                                }
                            }
                        }
                    )
                } else {
                    // Manual Dark Mode Switch
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.label_dark_mode)) },
                        supportingContent = { Text(stringResource(R.string.label_dark_mode_description)) },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        themeManager.setDarkMode(checked)
                                    }
                                },
                                thumbContent = {
                                    if (isDarkMode) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                }
                            )
                        }
                    )
                }
            }

            // Dynamic Colors Switch (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.label_dynamic_colors)) },
                    supportingContent = { Text(stringResource(R.string.label_dynamic_colors_description)) },
                    trailingContent = {
                        Switch(
                            checked = useDynamicColors,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    themeManager.setUseDynamicColors(checked)
                                }
                            },
                            thumbContent = {
                                if (useDynamicColors) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour ->
                scope.launch {
                    themeManager.setAutoDarkHours(hour, autoDarkEnd)
                }
                showStartTimePicker = false
            },
            initialHour = autoDarkStart
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour ->
                scope.launch {
                    themeManager.setAutoDarkHours(autoDarkStart, hour)
                }
                showEndTimePicker = false
            },
            initialHour = autoDarkEnd
        )
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    initialHour: Int
) {
    var selectedHour by remember { mutableStateOf(initialHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hour: $selectedHour:00")
                Slider(
                    value = selectedHour.toFloat(),
                    onValueChange = { selectedHour = it.toInt() },
                    valueRange = 0f..23f,
                    steps = 23
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 