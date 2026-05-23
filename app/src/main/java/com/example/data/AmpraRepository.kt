package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AmpraRepository(
    private val profileDao: ChargeProfileDao,
    private val sessionDao: ChargeSessionDao,
    private val repositoryScope: CoroutineScope
) {
    val allProfiles: Flow<List<ChargeProfile>> = profileDao.getAllProfiles()
    val activeProfile: Flow<ChargeProfile?> = profileDao.getActiveProfileFlow()

    val allSessions: Flow<List<ChargeSession>> = sessionDao.getAllSessions()

    init {
        repositoryScope.launch(Dispatchers.IO) {
            checkAndPrepopulateProfiles()
        }
    }

    private suspend fun checkAndPrepopulateProfiles() {
        val count = profileDao.getProfileCount()
        if (count == 0) {
            val defaultProfiles = listOf(
                ChargeProfile(
                    name = "Battery Health Mode",
                    batteryLimit = 80,
                    isAlarmEnabled = true,
                    isNotificationEnabled = true,
                    isVoiceEnabled = false,
                    isVibrationEnabled = true,
                    vibrationType = "Medium",
                    isFlashEnabled = false,
                    blinkIntervalMs = 500,
                    isRepeatEnabled = false,
                    repeatIntervalMinutes = 5,
                    isPreset = true,
                    isActive = true // Default active profile
                ),
                ChargeProfile(
                    name = "Sleep Mode",
                    batteryLimit = 80,
                    isAlarmEnabled = false,
                    isNotificationEnabled = true,
                    isVoiceEnabled = false,
                    isVibrationEnabled = true,
                    vibrationType = "Short",
                    isFlashEnabled = false,
                    blinkIntervalMs = 500,
                    isRepeatEnabled = false,
                    repeatIntervalMinutes = 10,
                    isPreset = true,
                    isActive = false
                ),
                ChargeProfile(
                    name = "Office Mode",
                    batteryLimit = 85,
                    isAlarmEnabled = false,
                    isNotificationEnabled = true,
                    isVoiceEnabled = false,
                    isVibrationEnabled = false,
                    vibrationType = "Short",
                    isFlashEnabled = false,
                    blinkIntervalMs = 1000,
                    isRepeatEnabled = false,
                    repeatIntervalMinutes = 5,
                    isPreset = true,
                    isActive = false
                ),
                ChargeProfile(
                    name = "Gaming Mode",
                    batteryLimit = 90,
                    isAlarmEnabled = true,
                    isNotificationEnabled = true,
                    isVoiceEnabled = true,
                    isVibrationEnabled = true,
                    vibrationType = "Continuous",
                    isFlashEnabled = true,
                    blinkIntervalMs = 300,
                    isRepeatEnabled = true,
                    repeatIntervalMinutes = 2,
                    isPreset = true,
                    isActive = false
                )
            )

            for (profile in defaultProfiles) {
                profileDao.insertProfile(profile)
            }
        }
    }

    suspend fun insertProfile(profile: ChargeProfile): Long = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: ChargeProfile) = withContext(Dispatchers.IO) {
        profileDao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: ChargeProfile) = withContext(Dispatchers.IO) {
        profileDao.deleteProfile(profile)
    }

    suspend fun setActiveProfile(profileId: Int) = withContext(Dispatchers.IO) {
        profileDao.setActiveProfile(profileId)
    }

    suspend fun getActiveProfileOnce(): ChargeProfile? = withContext(Dispatchers.IO) {
        profileDao.getActiveProfileOnce()
    }

    suspend fun insertSession(session: ChargeSession) = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(session: ChargeSession) = withContext(Dispatchers.IO) {
        sessionDao.deleteSession(session)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        sessionDao.clearAllHistory()
    }
}
