package com.example.alarms

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.ui.ReminderPrompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AlarmRingtonePlayer {

    private val _isRinging = MutableStateFlow(false)
    val isRinging: StateFlow<Boolean> = _isRinging.asStateFlow()

    private val _ringingPrompt = MutableStateFlow<ReminderPrompt?>(null)
    val ringingPrompt: StateFlow<ReminderPrompt?> = _ringingPrompt.asStateFlow()

    private val _previewingTone = MutableStateFlow<String?>(null)
    val previewingTone: StateFlow<String?> = _previewingTone.asStateFlow()

    private var activeJob: Job? = null
    private var previewJob: Job? = null
    private var toneGen: ToneGenerator? = null
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    val ringtoneOptions = listOf(
        "Continuous Phone Ring" to "Classic phone ringing like an incoming call",
        "Loud Alarm Clock" to "High-volume alarm siren / buzzer",
        "Digital Rapid Beeps" to "Urgent rapid alert beeps",
        "Melodic Wake Call" to "Pleasant melodious wake chime",
        "Radar Siren" to "High-priority warning radar beep",
        "System Default" to "System default alarm / ringtone"
    )

    val AVAILABLE_RINGTONES: List<String> = ringtoneOptions.map { it.first }

    fun playPreview(context: Context, soundName: String) {
        previewTone(context, soundName)
    }

    private fun getVibrator(context: Context): Vibrator {
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
        return vibrator!!
    }

    fun startRinging(context: Context, soundName: String, prompt: ReminderPrompt?) {
        stopRinging()
        _isRinging.value = true
        _ringingPrompt.value = prompt

        // Start repeating vibration
        try {
            val vib = getVibrator(context)
            val pattern = longArrayOf(0, 1000, 800, 1000, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            // Ignore vibration error
        }

        // Start continuous audio loop
        activeJob = CoroutineScope(Dispatchers.Default).launch {
            runSoundLoop(context, soundName)
        }
    }

    private suspend fun runSoundLoop(context: Context, soundName: String) {
        val streamType = if (soundName.contains("Phone", ignoreCase = true)) {
            AudioManager.STREAM_RING
        } else {
            AudioManager.STREAM_ALARM
        }

        // Try System Ringtone first if requested
        if (soundName == "System Default" || soundName.contains("Phone", ignoreCase = true)) {
            try {
                val uri = if (soundName.contains("Phone", ignoreCase = true)) {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                } else {
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                }
                val r = RingtoneManager.getRingtone(context.applicationContext, uri)
                if (r != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        r.isLooping = true
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        r.audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    }
                    ringtone = r
                    r.play()

                    while (kotlinx.coroutines.currentCoroutineContext().isActive && _isRinging.value) {
                        if (!r.isPlaying) {
                            r.play()
                        }
                        delay(2000)
                    }
                    return
                }
            } catch (e: Exception) {
                // Fallback to ToneGenerator below
            }
        }

        // ToneGenerator loop as bulletproof continuous audio generator
        try {
            toneGen = ToneGenerator(streamType, 100)
            while (kotlinx.coroutines.currentCoroutineContext().isActive && _isRinging.value) {
                when (soundName) {
                    "Continuous Phone Ring" -> {
                        // Ring ring cadence: 800ms tone, 400ms pause, 800ms tone, 2000ms silence
                        toneGen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                        delay(1200)
                        toneGen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                        delay(2400)
                    }
                    "Loud Alarm Clock" -> {
                        // Pulsing rapid alarm buzzer
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 700)
                        delay(900)
                    }
                    "Digital Rapid Beeps" -> {
                        // High urgency triple beep
                        repeat(3) {
                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
                            delay(220)
                        }
                        delay(600)
                    }
                    "Melodic Wake Call" -> {
                        toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                        delay(300)
                        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                        delay(400)
                        delay(1000)
                    }
                    "Radar Siren" -> {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 600)
                        delay(750)
                    }
                    else -> {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 800)
                        delay(1200)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore tone gen errors
        }
    }

    fun stopRinging() {
        _isRinging.value = false
        _ringingPrompt.value = null
        activeJob?.cancel()
        activeJob = null

        try {
            ringtone?.stop()
            ringtone = null
        } catch (e: Exception) {}

        try {
            toneGen?.stopTone()
            toneGen?.release()
            toneGen = null
        } catch (e: Exception) {}

        try {
            vibrator?.cancel()
        } catch (e: Exception) {}
    }

    fun previewTone(context: Context, soundName: String) {
        stopPreview()
        _previewingTone.value = soundName

        previewJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                when (soundName) {
                    "Continuous Phone Ring" -> {
                        tg.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                        delay(1000)
                        tg.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                        delay(1200)
                    }
                    "Loud Alarm Clock" -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 600)
                        delay(800)
                        tg.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 600)
                        delay(800)
                    }
                    "Digital Rapid Beeps" -> {
                        repeat(4) {
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                            delay(200)
                        }
                    }
                    "Melodic Wake Call" -> {
                        tg.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                        delay(300)
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                        delay(600)
                    }
                    "Radar Siren" -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                        delay(650)
                        tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                        delay(650)
                    }
                    else -> {
                        tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 600)
                        delay(800)
                    }
                }
                tg.stopTone()
                tg.release()
            } catch (e: Exception) {
                // Tone preview fallback
            } finally {
                _previewingTone.value = null
            }
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        _previewingTone.value = null
    }
}
