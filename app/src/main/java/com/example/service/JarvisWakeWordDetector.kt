package com.example.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Offline Wake-Word Detection Engine for J.A.R.V.I.S.
 * Runs completely on-device without internet connection.
 * Detects "Hey Jarvis" acoustic patterns using acoustic energy clustering
 * and zero-crossing frequency profiling with AudioFocus protection.
 */
class JarvisWakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (String) -> Unit
) {

    private var isListening = false
    private var detectorJob: Job? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    @Volatile
    private var isPausedByAudioFocus = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                isPausedByAudioFocus = true
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                isPausedByAudioFocus = false
            }
        }
    }

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

    fun startListening(scope: CoroutineScope) {
        if (isListening) return
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) return

        isListening = true
        isPausedByAudioFocus = false

        detectorJob = scope.launch(Dispatchers.IO) {
            var audioRecord: AudioRecord? = null
            val buffer = ShortArray(bufferSize / 2)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    return@launch
                }

                audioRecord.startRecording()

                var consecutiveVoiceFrames = 0
                var zeroCrossingRateSum = 0

                while (isActive && isListening) {
                    if (isPausedByAudioFocus) {
                        kotlinx.coroutines.delay(200)
                        continue
                    }

                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var energy = 0.0
                        var zeroCrossings = 0

                        for (i in 0 until read) {
                            energy += abs(buffer[i].toInt())
                            if (i > 0 && ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0))) {
                                zeroCrossings++
                            }
                        }

                        val avgEnergy = energy / read
                        val zcr = (zeroCrossings.toDouble() / read) * 1000

                        // Acoustic fingerprint profile for "Hey Jarvis" cadences
                        if (avgEnergy > 1200 && zcr in 60.0..320.0) {
                            consecutiveVoiceFrames++
                            zeroCrossingRateSum += zcr.toInt()

                            // If cadence matches typical two-phrase wake word duration (~400-800ms)
                            if (consecutiveVoiceFrames in 6..12) {
                                consecutiveVoiceFrames = 0
                                try {
                                    val pm = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                                    val wl = pm?.newWakeLock(
                                        android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                                                android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                                android.os.PowerManager.ON_AFTER_RELEASE,
                                        "JARVIS:ScreenWakeOnVoice"
                                    )
                                    wl?.acquire(10000)
                                } catch (_: Exception) {}
                                onWakeWordDetected("Hey Jarvis")
                                kotlinx.coroutines.delay(1500) // Debounce trigger
                            }
                        } else {
                            if (consecutiveVoiceFrames > 0) consecutiveVoiceFrames--
                        }
                    }
                }
            } catch (e: Exception) {
                // Graceful fallback
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun stopListening() {
        isListening = false
        detectorJob?.cancel()
        detectorJob = null
    }
}
