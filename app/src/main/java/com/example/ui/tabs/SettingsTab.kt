package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.viewmodel.AmpraViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsTab(
    viewModel: AmpraViewModel,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val animationsEnabled by viewModel.animationsEnabled.collectAsState()
    val isSpacious by viewModel.uiDensitySpacious.collectAsState()

    var showOptimizationAlert by remember { mutableStateOf(false) }
    var bootEnabled by remember { mutableStateOf(true) }

    val padding = if (isSpacious) 20.dp else 12.dp
    val spacing = if (isSpacious) 16.dp else 10.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Tab Header Title
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Fine tune system behavior and interface styling",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Card 1: Appearance Customization
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Visual Customization", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // 1. Theme selection grid
                Text(
                    text = "App Color Theme",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black
                )

                // Grid mapping themes chunked in groups of 4 to fit perfectly responsive on any screen
                val themes: List<Triple<AppTheme, String, ImageVector>> = listOf(
                    Triple(AppTheme.LIGHT, "Light", Icons.Default.LightMode),
                    Triple(AppTheme.DARK, "Dark", Icons.Default.DarkMode),
                    Triple(AppTheme.AMOLED, "AMOLED", Icons.Default.BrightnessLow),
                    Triple(AppTheme.SYSTEM, "System", Icons.Default.SettingsSuggest),
                    Triple(AppTheme.CYBERPUNK, "Cyber", Icons.Default.FlashOn),
                    Triple(AppTheme.SOLAR_AMBER, "Solar", Icons.Default.WbSunny),
                    Triple(AppTheme.FOREST_MOSS, "Forest", Icons.Default.Eco),
                    Triple(AppTheme.RETRO_LAVENDER, "Lavender", Icons.Default.Brush)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themes.chunked(4).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowThemes.forEach { (themeEnum: AppTheme, name: String, icon: ImageVector) ->
                                val isSelected = currentTheme == themeEnum
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setTheme(themeEnum) }
                                        .testTag("theme_btn_${name.lowercase()}"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = name,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            if (rowThemes.size < 4) {
                                repeat(4 - rowThemes.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }


                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 2. Animation Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active UI Pulsing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Smooth gradient animations and pulse indicators", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = animationsEnabled,
                        onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                        modifier = Modifier.testTag("toggle_animations")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 3. UI Density Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                        Text("Interface Density", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Adjust spacing across screen sections", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair(false, "Compact"),
                            Pair(true, "Spacious")
                        ).forEach { (isSpac, label) ->
                            val isSelected = isSpacious == isSpac
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setUiDensitySpacious(isSpac) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("density_chip_$label")
                            )
                        }
                    }
                }
            }
        }

        // Card 2: System Performance & Operations
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.IntegrationInstructions,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Services & Performance", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge)
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // 1. Auto start on boot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Start on Device Boot", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Enable service protection instantly as soon as mobile switches on.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = bootEnabled,
                        onCheckedChange = { bootEnabled = it }
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 2. Battery Optimizations Review Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Battery Restriction Exemption", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Android deep sleep restricts background alerts. Review configuration requirement.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { showOptimizationAlert = true },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Review optimization settings")
                    }
                }
            }
        }

        // Section Title: Premium locked options
        Text(
            text = "⚡ Advanced Intelligence (Premium Coming Soon)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFB300),
            modifier = Modifier.padding(top = 8.dp)
        )

        // List mapping Premium Features Visually Locked
        val premiumFeatures = listOf(
            Pair("Predictive Charge Analysis", "AI estimates remaining battery health cycle duration and suggests charging patterns."),
            Pair("Multiple Device Sync", "Connect smartwatch, tablet, and pc to receive battery alert alarms anywhere."),
            Pair("Custom Audio Voice Packs", "Download or load personalized audio and speech warning alarms."),
            Pair("Dynamic Screen Widgets", "Aesthetic grid widgets and responsive lockscreen notifications."),
            Pair("Smart Charging Scheduling", "Silence warning alarms automatically during dynamic scheduled Sleep calendars."),
            Pair("Smart Plug Integration", "Automatically triggers smart plugs like Tuya/Shelly to cut physical power when cap is achieved!")
        )

        LazyHorizontalRow(premiumFeatures)

        // Trademark info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Fully Offline • No Cloud Sync Required • No Ads",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ampra Assistant v1.2.0 • Made with Jetpack Compose",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }

    // Battery Optimization instructions Alert
    if (showOptimizationAlert) {
        AlertDialog(
            onDismissRequest = { showOptimizationAlert = false },
            title = { Text("Exempt Ampra from Sleep Mode", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "To guarantee that alarms and alarms loop immediately when charging achieves targets, you must prevent Android from putting Ampra to sleep:\n\n" +
                            "1. Go to dynamic system Settings.\n" +
                            "2. Select Apps ➔ Ampra.\n" +
                            "3. Select Battery Use.\n" +
                            "4. Adjust settings to 'UNRESTRICTED' optimization.\n\n" +
                            "This permits the background services to run robustly without delay.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = { showOptimizationAlert = false }) {
                    Text("I UNDERSTAND")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LazyHorizontalRow(features: List<Pair<String, String>>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        features.forEach { (title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFF3E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Coming soon feature is locked",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFF8E1)
                            ) {
                                Text(
                                    text = "SOON",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF8F00),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
