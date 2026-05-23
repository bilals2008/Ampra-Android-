package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AmpraRepository
import com.example.data.ChargeProfile
import com.example.data.ChargeSession
import com.example.ui.theme.AppTheme
import com.example.service.BatteryInfoState
import com.example.service.BatteryStatusHub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AmpraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AmpraRepository
    val currentBatteryState: StateFlow<BatteryInfoState>

    // Profiles & History from DB
    val allProfiles: StateFlow<List<ChargeProfile>>
    val activeProfile: StateFlow<ChargeProfile?>
    val allSessions: StateFlow<List<ChargeSession>>

    // General persistent configurations
    private val _currentTheme = MutableStateFlow(AppTheme.DARK)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(true)
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _uiDensitySpacious = MutableStateFlow(true)
    val uiDensitySpacious: StateFlow<Boolean> = _uiDensitySpacious.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AmpraRepository(
            database.chargeProfileDao(),
            database.chargeSessionDao(),
            viewModelScope
        )

        val hub = BatteryStatusHub.getInstance(application)
        hub.init(repository, viewModelScope)
        currentBatteryState = hub.batteryState

        allProfiles = repository.allProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeProfile = repository.activeProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allSessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Read saved setting values from local prefs or initialize
        val prefs = application.getSharedPreferences("ampra_settings", Application.MODE_PRIVATE)
        val savedThemeString = prefs.getString("theme", AppTheme.DARK.name) ?: AppTheme.DARK.name
        _currentTheme.value = AppTheme.valueOf(savedThemeString)
        _animationsEnabled.value = prefs.getBoolean("animations_enabled", true)
        _uiDensitySpacious.value = prefs.getBoolean("ui_density_spacious", true)
    }

    // Theme setter
    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        val prefs = getApplication<Application>().getSharedPreferences("ampra_settings", Application.MODE_PRIVATE)
        prefs.edit().putString("theme", theme.name).apply()
    }

    // Animations setter
    fun setAnimationsEnabled(enabled: Boolean) {
        _animationsEnabled.value = enabled
        val prefs = getApplication<Application>().getSharedPreferences("ampra_settings", Application.MODE_PRIVATE)
        prefs.edit().putBoolean("animations_enabled", enabled).apply()
    }

    // UI density setter
    fun setUiDensitySpacious(spacious: Boolean) {
        _uiDensitySpacious.value = spacious
        val prefs = getApplication<Application>().getSharedPreferences("ampra_settings", Application.MODE_PRIVATE)
        prefs.edit().putBoolean("ui_density_spacious", spacious).apply()
    }

    // Profile options
    fun setActiveProfile(profile: ChargeProfile) {
        viewModelScope.launch {
            repository.setActiveProfile(profile.id)
        }
    }

    fun addNewProfile(
        name: String,
        batteryLimit: Int,
        isAlarmEnabled: Boolean,
        isNotificationEnabled: Boolean,
        isVoiceEnabled: Boolean,
        isVibrationEnabled: Boolean,
        vibrationType: String,
        isFlashEnabled: Boolean,
        blinkIntervalMs: Int,
        isRepeatEnabled: Boolean
    ) {
        viewModelScope.launch {
            val newProfile = ChargeProfile(
                name = name,
                batteryLimit = batteryLimit,
                isAlarmEnabled = isAlarmEnabled,
                isNotificationEnabled = isNotificationEnabled,
                isVoiceEnabled = isVoiceEnabled,
                isVibrationEnabled = isVibrationEnabled,
                vibrationType = vibrationType,
                isFlashEnabled = isFlashEnabled,
                blinkIntervalMs = blinkIntervalMs,
                isRepeatEnabled = isRepeatEnabled,
                repeatIntervalMinutes = 5,
                isPreset = false,
                isActive = false
            )
            repository.insertProfile(newProfile)
        }
    }

    fun updateActiveProfileDetails(
        batteryLimit: Int? = null,
        isAlarmEnabled: Boolean? = null,
        isNotificationEnabled: Boolean? = null,
        isVoiceEnabled: Boolean? = null,
        isVibrationEnabled: Boolean? = null,
        vibrationType: String? = null,
        isFlashEnabled: Boolean? = null,
        blinkIntervalMs: Int? = null,
        isRepeatEnabled: Boolean? = null
    ) {
        val currentActive = activeProfile.value ?: return
        viewModelScope.launch {
            val updated = currentActive.copy(
                batteryLimit = batteryLimit ?: currentActive.batteryLimit,
                isAlarmEnabled = isAlarmEnabled ?: currentActive.isAlarmEnabled,
                isNotificationEnabled = isNotificationEnabled ?: currentActive.isNotificationEnabled,
                isVoiceEnabled = isVoiceEnabled ?: currentActive.isVoiceEnabled,
                isVibrationEnabled = isVibrationEnabled ?: currentActive.isVibrationEnabled,
                vibrationType = vibrationType ?: currentActive.vibrationType,
                isFlashEnabled = isFlashEnabled ?: currentActive.isFlashEnabled,
                blinkIntervalMs = blinkIntervalMs ?: currentActive.blinkIntervalMs,
                isRepeatEnabled = isRepeatEnabled ?: currentActive.isRepeatEnabled
            )
            repository.updateProfile(updated)
        }
    }

    fun deleteProfile(profile: ChargeProfile) {
        if (profile.isPreset) return // Don't let users delete core presets
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    // Dismiss trigger
    fun dismissCurrentAlarms() {
        BatteryStatusHub.getInstance(getApplication()).dismissActiveAlerts()
    }

    // Delete history
    fun removeSession(session: ChargeSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Simulation functions
    fun toggleSimulationMode(enabled: Boolean) {
        BatteryStatusHub.getInstance(getApplication()).setSimulationMode(enabled)
    }

    fun setSimulatedBattery(
        level: Int,
        isCharging: Boolean,
        chargerType: String,
        temperature: Float,
        speed: Float
    ) {
        BatteryStatusHub.getInstance(getApplication()).updateSimulationValues(
            level = level,
            isCharging = isCharging,
            chargerType = chargerType,
            temperature = temperature,
            speed = speed
        )
    }

    // Factory helper
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AmpraViewModel::class.java)) {
                return AmpraViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
