package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.service.BatteryMonitorService
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AmpraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start background battery care monitor service on launch
        BatteryMonitorService.startService(this)
        
        // Create the centralized ViewModel instance
        val viewModel = ViewModelProvider(this, AmpraViewModel.Factory(application))[AmpraViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            // Observe custom theme selection reactively (Light / Dark / AMOLED / System)
            val appTheme by viewModel.currentTheme.collectAsState()

            MyApplicationTheme(appTheme = appTheme) {
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
