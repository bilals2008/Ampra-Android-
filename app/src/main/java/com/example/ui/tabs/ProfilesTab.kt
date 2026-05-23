package com.example.ui.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChargeProfile
import com.example.ui.viewmodel.AmpraViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfilesTab(
    viewModel: AmpraViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val isSpacious by viewModel.uiDensitySpacious.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Dialog state variables
    var newProfileName by remember { mutableStateOf("") }
    var newBatteryLimit by remember { mutableStateOf(80f) }
    var newAlarmEnabled by remember { mutableStateOf(true) }
    var newNotificationEnabled by remember { mutableStateOf(true) }
    var newVoiceEnabled by remember { mutableStateOf(false) }
    var newVibrationEnabled by remember { mutableStateOf(true) }
    var newFlashEnabled by remember { mutableStateOf(false) }

    val padding = if (isSpacious) 20.dp else 12.dp
    val spacing = if (isSpacious) 16.dp else 10.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Tab Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Charging Profiles",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Apply or build custom battery protect limits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Add Profile FAB
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("add_profile_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add alert profile", modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Create your first charging profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("profiles_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    val isActive = activeProfile?.id == profile.id
                    ProfileCard(
                        profile = profile,
                        isActive = isActive,
                        onActivate = { viewModel.setActiveProfile(profile) },
                        onDelete = { viewModel.deleteProfile(profile) }
                    )
                }
            }
        }
    }

    // Profile creation dialog sheet
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "New Charging Profile",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title input
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name (e.g. Work mode)") },
                        placeholder = { Text("Custom Protective Alert") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_profile_name_input")
                    )

                    // Target Cap slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Battery Limit Target:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text("${newBatteryLimit.roundToInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = newBatteryLimit,
                            onValueChange = { newBatteryLimit = it },
                            valueRange = 50f..100f,
                            modifier = Modifier.testTag("dialog_profile_battery_slider")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Text(
                        text = "Trigger Alert Behaviors:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Alarm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            Text("Play Alarm Melody", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = newAlarmEnabled,
                            onCheckedChange = { newAlarmEnabled = it },
                            modifier = Modifier.testTag("dialog_profile_alarm_checkbox")
                        )
                    }

                    // Notification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Push Status Notification", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = newNotificationEnabled,
                            onCheckedChange = { newNotificationEnabled = it },
                            modifier = Modifier.testTag("dialog_profile_notif_checkbox")
                        )
                    }

                    // Voice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Text("Speak Voice Message (TTS)", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = newVoiceEnabled,
                            onCheckedChange = { newVoiceEnabled = it },
                            modifier = Modifier.testTag("dialog_profile_voice_checkbox")
                        )
                    }

                    // Vibration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFF9800))
                            Text("Haptic Buzz vibration", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = newVibrationEnabled,
                            onCheckedChange = { newVibrationEnabled = it },
                            modifier = Modifier.testTag("dialog_profile_vibe_checkbox")
                        )
                    }

                    // Flashlight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.FlashlightOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFE91E63))
                            Text("Blink Camera Flashlight", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = newFlashEnabled,
                            onCheckedChange = { newFlashEnabled = it },
                            modifier = Modifier.testTag("dialog_profile_flash_checkbox")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = newProfileName.ifBlank { "Custom protective limit" }
                        viewModel.addNewProfile(
                            name = finalName,
                            batteryLimit = newBatteryLimit.roundToInt(),
                            isAlarmEnabled = newAlarmEnabled,
                            isNotificationEnabled = newNotificationEnabled,
                            isVoiceEnabled = newVoiceEnabled,
                            isVibrationEnabled = newVibrationEnabled,
                            vibrationType = "Medium",
                            isFlashEnabled = newFlashEnabled,
                            blinkIntervalMs = 500,
                            isRepeatEnabled = false
                        )
                        // Reset
                        newProfileName = ""
                        newBatteryLimit = 80f
                        newAlarmEnabled = true
                        newNotificationEnabled = true
                        newVoiceEnabled = false
                        newVibrationEnabled = true
                        newFlashEnabled = false
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("dialog_submit_button")
                ) {
                    Text("CREATE PROFILE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProfileCard(
    profile: ChargeProfile,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isActive) 3.dp else 1.dp, RoundedCornerShape(16.dp))
            .clickable { onActivate() }
            .testTag("profile_card_item_${profile.name.replace(" ", "_").lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title + Cap
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (profile.isPreset) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = "PRESET",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Display list of enabled alert behaviors conceptually
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconChipIndicator(icon = Icons.Default.VolumeUp, enabled = profile.isAlarmEnabled, color = MaterialTheme.colorScheme.error)
                        IconChipIndicator(icon = Icons.Default.Notifications, enabled = profile.isNotificationEnabled, color = MaterialTheme.colorScheme.primary)
                        IconChipIndicator(icon = Icons.Default.RecordVoiceOver, enabled = profile.isVoiceEnabled, color = MaterialTheme.colorScheme.tertiary)
                        IconChipIndicator(icon = Icons.Default.Vibration, enabled = profile.isVibrationEnabled, color = Color(0xFFFF9800))
                        IconChipIndicator(icon = Icons.Default.FlashlightOn, enabled = profile.isFlashEnabled, color = Color(0xFFE91E63))
                    }
                }

                Text(
                    text = "${profile.batteryLimit}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active/Inactive Indicator Box
                if (isActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                        Text(
                            text = "ACTIVE PROTECTION",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                } else {
                    TextButton(
                        onClick = onActivate,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("ACTIVATE PROFILE", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Delete custom profiles
                if (!profile.isPreset) {
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.size(36.dp).testTag("delete_profile_button")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete custom profile", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IconChipIndicator(
    icon: ImageVector,
    enabled: Boolean,
    color: Color
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
        modifier = Modifier.size(16.dp)
    )
}
