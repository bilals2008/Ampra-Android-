package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.tabs.*
import com.example.ui.viewmodel.AmpraViewModel

sealed class TabScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : TabScreen("home", "Home", Icons.Default.Home)
    object Automation : TabScreen("automation", "Automation", Icons.Default.Tune)
    object Profiles : TabScreen("profiles", "Profiles", Icons.Default.Dashboard)
    object History : TabScreen("history", "History", Icons.Default.History)
    object Settings : TabScreen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    viewModel: AmpraViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf<TabScreen>(TabScreen.Home) }

    val tabs = listOf(
        TabScreen.Home,
        TabScreen.Automation,
        TabScreen.Profiles,
        TabScreen.History,
        TabScreen.Settings
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_nav_bar")
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentTab.route == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High fidelity switching with crossfade animation transitions under 300ms, conforming to guidelines
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(250),
                label = "tab_crossfade"
            ) { targetScreen ->
                when (targetScreen) {
                    TabScreen.Home -> HomeTab(viewModel = viewModel)
                    TabScreen.Automation -> AutomationTab(viewModel = viewModel)
                    TabScreen.Profiles -> ProfilesTab(viewModel = viewModel)
                    TabScreen.History -> HistoryTab(viewModel = viewModel)
                    TabScreen.Settings -> SettingsTab(viewModel = viewModel)
                }
            }
        }
    }
}
