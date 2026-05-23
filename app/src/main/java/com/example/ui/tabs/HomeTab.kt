package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.BatteryInfoState
import com.example.ui.theme.BatteryGreenEnd
import com.example.ui.theme.BatteryGreenStart
import com.example.ui.theme.ElectricGradientEnd
import com.example.ui.theme.ElectricGradientStart
import com.example.ui.viewmodel.AmpraViewModel
import kotlin.math.roundToInt

@Composable
fun HomeTab(
    viewModel: AmpraViewModel,
    modifier: Modifier = Modifier
) {
    val batteryState by viewModel.currentBatteryState.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val isSpacious by viewModel.uiDensitySpacious.collectAsState()
    val isAnimationEnabled by viewModel.animationsEnabled.collectAsState()

    var showSimulationPanel by remember { mutableStateOf(false) }

    // Densities & Pacing
    val padding = if (isSpacious) 20.dp else 12.dp
    val spacing = if (isSpacious) 16.dp else 10.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Name Callout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AMPRA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = if (batteryState.isCharging) "Powering up device..." else "Ready • Unplugged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Real-time Power Status Indicator Tag
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (batteryState.isCharging) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.shadow(4.dp, CircleShape)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (batteryState.isCharging) Icons.Default.Bolt else Icons.Default.BatteryStd,
                        contentDescription = "Power status icon",
                        tint = if (batteryState.isCharging) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = batteryState.chargerType,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (batteryState.isCharging) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Active Alarm Warning Banner (Dismiss action)
        AnimatedVisibility(
            visible = batteryState.isAlarmActive,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(vertical = 4.dp)
                    .testTag("alarm_active_banner")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgent Alarms",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Smart Battery Alert Triggered!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Your battery reached ${batteryState.level}%, matching the limit set in profile '${batteryState.currentActiveProfile?.name ?: "Custom"}'. Speak alerts, alarm loop, flashlight blinking, or vibration may be playing.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.dismissCurrentAlarms() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_alarm_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop alarm")
                            Text("DISMISS CHARGING ALARM", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Central Battery Dial Layout
        BatteryDial(
            level = batteryState.level,
            isCharging = batteryState.isCharging,
            targetLimit = activeProfile?.batteryLimit ?: 80,
            isAnimationEnabled = isAnimationEnabled,
            modifier = Modifier.size(230.dp)
        )

        // Quick Active Profile Details Row
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().clickable { /* Quick profiles toggle link */ }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Protection Mode",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Active Protection Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = activeProfile?.name ?: "Default Charging Alert",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${activeProfile?.batteryLimit ?: 80}% Cap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Telemetry Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            MetricCard(
                title = "Battery Temp",
                value = "${batteryState.temperatureCelsius.roundToInt()} °C",
                icon = Icons.Default.Thermostat,
                color = when {
                    batteryState.temperatureCelsius > 42f -> MaterialTheme.colorScheme.error
                    batteryState.temperatureCelsius > 36f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Charging Speed",
                value = if (batteryState.isCharging) "+${batteryState.chargingSpeedPercentPerHour.roundToInt()}%/hr" else "Stable 0%/hr",
                icon = Icons.Default.Speed,
                color = if (batteryState.isCharging) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            MetricCard(
                title = "Target Limit",
                value = "${activeProfile?.batteryLimit ?: 80}%",
                icon = Icons.Default.RadioButtonChecked,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "State of Health",
                value = "98% (Premium)",
                icon = Icons.Default.HealthAndSafety,
                color = MaterialTheme.colorScheme.secondary,
                isPremiumBadge = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Developer Simulator Box Toggle Drawer (Show UI interactive controls for AI Studio/Browser Emulation)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("simulation_toggle_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (batteryState.isSimulation) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (batteryState.isSimulation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header of Drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSimulationPanel = !showSimulationPanel }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Simulation controls toggle icon",
                            tint = if (batteryState.isSimulation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                text = "Telemetry & Alert Simulator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (batteryState.isSimulation) "Active (Simulated Battery)" else "Disabled (Reading device status)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (batteryState.isSimulation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showSimulationPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown"
                    )
                }

                // Inner simulation panel
                AnimatedVisibility(
                    visible = showSimulationPanel,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // 1. Simulation Activate Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Simulation Mode",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Simulate state changes in browser streaming",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = batteryState.isSimulation,
                                onCheckedChange = { viewModel.toggleSimulationMode(it) },
                                modifier = Modifier.testTag("simulation_switch")
                            )
                        }

                        // Disabled warning overlap if not simulated
                        if (!batteryState.isSimulation) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "⚠️ Turn ON Simulation above to mock charger plug ins, change temperature, or drag charging levels above limit.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // 2. Simulated Battery Slider Control
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Simulated Battery Level: ${batteryState.level}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Limit Target: ${activeProfile?.batteryLimit ?: 80}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = batteryState.level.toFloat(),
                                    onValueChange = {
                                        viewModel.setSimulatedBattery(
                                            level = it.roundToInt(),
                                            isCharging = batteryState.isCharging,
                                            chargerType = batteryState.chargerType,
                                            temperature = batteryState.temperatureCelsius,
                                            speed = batteryState.chargingSpeedPercentPerHour
                                        )
                                    },
                                    valueRange = 1f..100f,
                                    modifier = Modifier.testTag("sim_battery_slider")
                                )
                            }

                            // 3. Simulated Charging Connection state
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Is Charger Logged In:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = batteryState.isCharging,
                                        onClick = {
                                            val t = if (batteryState.isCharging) "Disconnected" else "AC"
                                            viewModel.setSimulatedBattery(
                                                level = batteryState.level,
                                                isCharging = !batteryState.isCharging,
                                                chargerType = t,
                                                temperature = batteryState.temperatureCelsius,
                                                speed = batteryState.chargingSpeedPercentPerHour
                                            )
                                        },
                                        label = { Text("Charging Active") }
                                    )
                                }
                            }

                            // 4. Charger Type Selection Row
                            if (batteryState.isCharging) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Charger Plug Connected:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("AC", "USB", "Wireless").forEach { type ->
                                            val selected = batteryState.chargerType == type
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        viewModel.setSimulatedBattery(
                                                            level = batteryState.level,
                                                            isCharging = batteryState.isCharging,
                                                            chargerType = type,
                                                            temperature = batteryState.temperatureCelsius,
                                                            speed = if (type == "AC") 38f else if (type == "USB") 12f else 22f
                                                        )
                                                    }
                                                    .padding(1.dp)
                                            ) {
                                                Text(
                                                    text = type,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Temperature control slider
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Simulated Temp: ${batteryState.temperatureCelsius.roundToInt()} °C", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = batteryState.temperatureCelsius,
                                    onValueChange = {
                                        viewModel.setSimulatedBattery(
                                            level = batteryState.level,
                                            isCharging = batteryState.isCharging,
                                            chargerType = batteryState.chargerType,
                                            temperature = it,
                                            speed = batteryState.chargingSpeedPercentPerHour
                                        )
                                    },
                                    valueRange = 22f..52f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryDial(
    level: Int,
    isCharging: Boolean,
    targetLimit: Int,
    isAnimationEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Dynamic brush animations for pulse
    val primaryColor = MaterialTheme.colorScheme.primary
    val ringColor = if (level >= targetLimit) MaterialTheme.colorScheme.secondary else primaryColor

    val transition = rememberInfiniteTransition(label = "Ring pulse transition")
    val pulseScale by if (isCharging && isAnimationEnabled) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = ringColor,
                spotColor = ringColor
            )
            .drawBehind {
                val scale = pulseScale
                if (scale > 1f) {
                    drawCircle(
                        color = ringColor.copy(alpha = 0.05f),
                        radius = size.minDimension / 1.7f * scale
                    )
                }
            }
    ) {
        // Double Progress Rings
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            
            // 1. Underlay track
            drawArc(
                color = ringColor.copy(alpha = 0.09f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Main battery sweep
            val mainSweep = (level / 100f) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        ringColor.copy(alpha = 0.4f),
                        ringColor,
                        ringColor.copy(alpha = 0.8f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = mainSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Mark the target limit cap exactly
            val targetAngle = -90f + ((targetLimit / 100f) * 360f)
            drawArc(
                color = Color.White.copy(alpha = 0.8f),
                startAngle = targetAngle - 1f,
                sweepAngle = 2f,
                useCenter = false,
                style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Butt)
            )
        }

        // Inside Dial Details
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery Status Indicators",
                tint = ringColor,
                modifier = Modifier.size(36.dp)
            )
            
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "$level",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
                )
            }

            Text(
                text = "🔋 Target ${targetLimit}%",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ringColor,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isPremiumBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isPremiumBadge) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "SOON",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
