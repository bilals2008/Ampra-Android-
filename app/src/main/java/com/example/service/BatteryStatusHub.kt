package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.example.data.AmpraRepository
import com.example.data.ChargeProfile
import com.example.data.ChargeSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BatteryInfoState(
    val level: Int = 50,
    val isCharging: Boolean = false,
    val chargerType: String = "Disconnected", // "AC", "USB", "Wireless", "Disconnected"
    val temperatureCelsius: Float = 28.5f,
    val chargingSpeedPercentPerHour: Float = 15.0f,
    val isSimulation: Boolean = false,
    val currentActiveProfile: ChargeProfile? = null,
    val isAlarmActive: Boolean = false
)

class BatteryStatusHub private constructor(private val context: Context) {

    companion object {
        private const val TAG = "BatteryStatusHub"

        @Volatile
        private var INSTANCE: BatteryStatusHub? = null

        fun getInstance(context: Context): BatteryStatusHub {
            return INSTANCE ?: synchronized(this) {
                val instance = BatteryStatusHub(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _batteryState = MutableStateFlow(BatteryInfoState())
    val batteryState: StateFlow<BatteryInfoState> = _batteryState.asStateFlow()

    private var repository: AmpraRepository? = null
    private var scope: CoroutineScope? = null
    private var receiverRegistered = false

    private val alarmManager = BatteryAlarmManager.getInstance(context)

    // Keep track of charging session start state
    private var sessionStartTime: Long = 0
    private var sessionStartBatteryLevel: Int = 0
    private var sessionMaxTemp: Float = 0f

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (_batteryState.value.isSimulation) {
                // If currently in simulation mode, ignore real hardware updates
                return
            }
            updateFromBatteryIntent(intent)
        }
    }

    fun init(repository: AmpraRepository, scope: CoroutineScope) {
        this.repository = repository
        this.scope = scope

        // Start collecting active profile
        scope.launch {
            repository.activeProfile.collect { profile ->
                _batteryState.value = _batteryState.value.copy(currentActiveProfile = profile)
                checkBatteryLimits()
            }
        }

        registerRealReceiver()
    }

    private fun registerRealReceiver() {
        if (receiverRegistered) return
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = context.registerReceiver(batteryReceiver, filter)
            stickyIntent?.let { updateFromBatteryIntent(it) }
            receiverRegistered = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register battery broadcast receiver", e)
        }
    }

    private fun updateFromBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 50

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Disconnected"
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempCelsius = tempTenths / 10.0f

        processBatteryChange(pct, isCharging, plugType, tempCelsius)
    }

    private fun processBatteryChange(level: Int, isCharging: Boolean, chargerType: String, tempCelsius: Float) {
        val oldState = _batteryState.value
        
        // Dynamic speed calculation placeholder (%/hr)
        var estimatedSpeed = 18f
        if (isCharging) {
            estimatedSpeed = when (chargerType) {
                "AC" -> 35f
                "USB" -> 12f
                "Wireless" -> 20f
                else -> 15f
            }
        } else {
            estimatedSpeed = 0f
        }

        // Handle Session tracking
        if (isCharging && !oldState.isCharging) {
            // Started charging session
            sessionStartTime = System.currentTimeMillis()
            sessionStartBatteryLevel = level
            sessionMaxTemp = tempCelsius
        } else if (!isCharging && oldState.isCharging) {
            // Discharged/unplugged session ended
            if (sessionStartTime > 0) {
                val endTime = System.currentTimeMillis()
                val finalStartPct = sessionStartBatteryLevel
                val finalEndPct = level
                val maxTemp = maxOf(sessionMaxTemp, tempCelsius)
                val durationSec = (endTime - sessionStartTime) / 1000f
                var speed = 0f
                if (durationSec > 10) {
                    val pctDiff = finalEndPct - finalStartPct
                    if (pctDiff > 0) {
                        speed = (pctDiff / (durationSec / 3600f))
                    }
                }
                
                val currentProfile = oldState.currentActiveProfile
                val targetLimit = currentProfile?.batteryLimit ?: 80
                val reachedLimit = finalEndPct >= targetLimit

                val finalSpeed = if (speed > 0f) speed else (when (oldState.chargerType) {
                    "AC" -> 38f
                    "USB" -> 10f
                    "Wireless" -> 18f
                    else -> 12f
                })

                val session = ChargeSession(
                    startTime = sessionStartTime,
                    endTime = endTime,
                    startBatteryLevel = finalStartPct,
                    endBatteryLevel = finalEndPct,
                    chargerType = oldState.chargerType,
                    maxTemperature = maxTemp,
                    averageChargingSpeed = finalSpeed,
                    limitReached = reachedLimit,
                    targetLimitSet = targetLimit,
                    profileName = currentProfile?.name ?: "Custom Alert"
                )

                scope?.launch {
                    repository?.insertSession(session)
                }

                sessionStartTime = 0
            }
        }

        if (isCharging && tempCelsius > sessionMaxTemp) {
            sessionMaxTemp = tempCelsius
        }

        // Update active profile triggers if necessary
        _batteryState.value = oldState.copy(
            level = level,
            isCharging = isCharging,
            chargerType = chargerType,
            temperatureCelsius = tempCelsius,
            chargingSpeedPercentPerHour = estimatedSpeed,
            isAlarmActive = alarmManager.isAlarmActive
        )

        checkBatteryLimits()
    }

    /**
     * Simulation triggers
     */
    fun setSimulationMode(enabled: Boolean) {
        val oldState = _batteryState.value
        if (oldState.isSimulation == enabled) return

        if (!enabled) {
            // Re-read real info
            _batteryState.value = oldState.copy(isSimulation = false)
            registerRealReceiver()
        } else {
            _batteryState.value = oldState.copy(isSimulation = true)
        }
    }

    fun updateSimulationValues(
        level: Int,
        isCharging: Boolean,
        chargerType: String,
        temperature: Float,
        speed: Float
    ) {
        if (!_batteryState.value.isSimulation) return
        processBatteryChange(level, isCharging, chargerType, temperature)
    }

    private fun checkBatteryLimits() {
        val state = _batteryState.value
        val profile = state.currentActiveProfile ?: return

        if (state.isCharging && state.level >= profile.batteryLimit) {
            // Trigger alerts!
            if (!alarmManager.isAlarmActive) {
                alarmManager.triggerAlerts(profile, state.level)
                _batteryState.value = _batteryState.value.copy(isAlarmActive = true)
            }
        } else {
            // If charging is unplugged, or falls below target, should we dismiss?
            // Usually, unplugging charger should dismiss the alert automatically, satisfying convenience
            if (!state.isCharging && alarmManager.isAlarmActive) {
                dismissActiveAlerts()
            }
        }
    }

    fun dismissActiveAlerts() {
        alarmManager.dismissAlerts()
        _batteryState.value = _batteryState.value.copy(isAlarmActive = false)
    }
}
