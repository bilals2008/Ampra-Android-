package com.example.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.ChargeProfile
import java.util.Locale

class BatteryAlarmManager private constructor(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "BatteryAlarmManager"

        @Volatile
        private var INSTANCE: BatteryAlarmManager? = null

        fun getInstance(context: Context): BatteryAlarmManager {
            return INSTANCE ?: synchronized(this) {
                val instance = BatteryAlarmManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var ringtone: Ringtone? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    // Flashing state
    private var isFlashing = false
    private val handler = Handler(Looper.getMainLooper())
    private var flashRunnable: Runnable? = null
    
    // Alarm active flag
    var isAlarmActive = false
        private set

    init {
        // Initialize Vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        // Initialize TTS
        tts = TextToSpeech(context, this)

        // Initialize Camera for Flash
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        try {
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager?.getCameraCharacteristics(id)
                chars?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to inspect camera characteristics for flash", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isTtsInitialized = true
        } else {
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    /**
     * Triggers the configured alerts based on the profile
     */
    fun triggerAlerts(profile: ChargeProfile, batteryLevel: Int) {
        if (isAlarmActive) return // Avoid overlapping alarms
        isAlarmActive = true

        Log.d(TAG, "Triggering alerts for profile: ${profile.name} at $batteryLevel%")

        // 1. Audio Alarm
        if (profile.isAlarmEnabled) {
            playAlarmSound()
        }

        // 2. Vibration
        if (profile.isVibrationEnabled) {
            startVibration(profile.vibrationType)
        }

        // 3. Voice Alert
        if (profile.isVoiceEnabled) {
            speakVoiceAlert(profile.batteryLimit)
        }

        // 4. Flashlight Blinking
        if (profile.isFlashEnabled) {
            startFlashBlinking(profile.blinkIntervalMs)
        }
    }

    /**
     * Stop all active alarms/vibrations/flashlights
     */
    fun dismissAlerts() {
        if (!isAlarmActive) return
        isAlarmActive = false

        Log.d(TAG, "Dismissing active alerts")

        // Stop Audio
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        }

        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            ringtone = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }

        // Stop TTS
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }

        // Stop Vibration
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling vibration", e)
        }

        // Stop Flashlight
        stopFlashBlinking()
    }

    private fun playAlarmSound() {
        try {
            // Try to find a default alarm uri
            var alertUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            if (alertUri == null) {
                alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

            if (alertUri != null) {
                // Use MediaPlayer for looping functionality
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alertUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play default Alarm via MediaPlayer, falling back to Ringtone", e)
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ringtone = RingtoneManager.getRingtone(context, fallbackUri)
                ringtone?.play()
            } catch (e2: Exception) {
                Log.e(TAG, "Failed fallback ringtone play as well", e2)
            }
        }
    }

    private fun startVibration(type: String) {
        val pattern = when (type.lowercase(Locale.ROOT)) {
            "short" -> longArrayOf(0, 200, 200, 200)
            "medium" -> longArrayOf(0, 500, 500, 500)
            "continuous" -> longArrayOf(0, 1000, 500, 1000, 500)
            else -> longArrayOf(0, 500, 500, 500)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val repeatCount = if (type.equals("continuous", ignoreCase = true)) 0 else -1
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, repeatCount))
        } else {
            val repeatCount = if (type.equals("continuous", ignoreCase = true)) 0 else -1
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, repeatCount)
        }
    }

    private fun speakVoiceAlert(limit: Int) {
        if (!isTtsInitialized) {
            Log.w(TAG, "TTS not ready yet")
            return
        }
        val message = "Battery protection alert. Battery reached $limit percent. Please unplug the charger."
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "AmpraSpeech")
    }

    private fun startFlashBlinking(intervalMs: Int) {
        val cid = cameraId ?: return
        if (isFlashing) return
        isFlashing = true

        var torchState = false
        flashRunnable = object : Runnable {
            override fun run() {
                if (!isFlashing) return
                torchState = !torchState
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager?.setTorchMode(cid, torchState)
                    }
                } catch (e: CameraAccessException) {
                    Log.e(TAG, "Failed to set torch mode", e)
                } catch (e: SecurityException) {
                    Log.e(TAG, "Permission error setting legacy torch mode", e)
                }
                handler.postDelayed(this, intervalMs.toLong())
            }
        }
        handler.post(flashRunnable!!)
    }

    private fun stopFlashBlinking() {
        isFlashing = false
        flashRunnable?.let { handler.removeCallbacks(it) }
        flashRunnable = null
        
        // Ensure flash is turned off
        val cid = cameraId ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(cid, false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset flash off", e)
        }
    }

    fun onDestroy() {
        dismissAlerts()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Failed shutdown TTS", e)
        }
    }
}
