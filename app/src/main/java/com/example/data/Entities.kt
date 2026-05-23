package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "charge_profiles")
data class ChargeProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val batteryLimit: Int, // 1 to 100
    val isAlarmEnabled: Boolean,
    val isNotificationEnabled: Boolean,
    val isVoiceEnabled: Boolean,
    val isVibrationEnabled: Boolean,
    val vibrationType: String, // "Short", "Medium", "Continuous"
    val isFlashEnabled: Boolean,
    val blinkIntervalMs: Int,
    val isRepeatEnabled: Boolean,
    val repeatIntervalMinutes: Int,
    val isPreset: Boolean = false,
    val isActive: Boolean = false
)

@Entity(tableName = "charge_sessions")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val startBatteryLevel: Int,
    val endBatteryLevel: Int,
    val chargerType: String, // "AC", "USB", "Wireless", "Disconnected"
    val maxTemperature: Float,
    val averageChargingSpeed: Float, // % per hour
    val limitReached: Boolean,
    val targetLimitSet: Int,
    val profileName: String
)

@Dao
interface ChargeProfileDao {
    @Query("SELECT * FROM charge_profiles ORDER BY isPreset DESC, name ASC")
    fun getAllProfiles(): Flow<List<ChargeProfile>>

    @Query("SELECT * FROM charge_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfileFlow(): Flow<ChargeProfile?>

    @Query("SELECT * FROM charge_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): ChargeProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChargeProfile): Long

    @Update
    suspend fun updateProfile(profile: ChargeProfile)

    @Delete
    suspend fun deleteProfile(profile: ChargeProfile)

    @Query("UPDATE charge_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Transaction
    suspend fun setActiveProfile(profileId: Int) {
        deactivateAllProfiles()
        QuerySetActiveHelper(profileId)
    }

    @Query("UPDATE charge_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun QuerySetActiveHelper(profileId: Int)

    @Query("SELECT COUNT(*) FROM charge_profiles")
    suspend fun getProfileCount(): Int
}

@Dao
interface ChargeSessionDao {
    @Query("SELECT * FROM charge_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargeSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargeSession)

    @Delete
    suspend fun deleteSession(session: ChargeSession)

    @Query("DELETE FROM charge_sessions")
    suspend fun clearAllHistory()
}
