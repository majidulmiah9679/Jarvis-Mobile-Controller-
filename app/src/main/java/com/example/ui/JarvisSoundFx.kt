package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Iron Man HUD Sound FX & Haptic Feedback Engine.
 * Synthesizes pure sci-fi holographic chimes and arc-reactor pulses in real-time.
 */
object JarvisSoundFx {

    private val fxScope = CoroutineScope(Dispatchers.Default)

    /**
     * Haptic feedback pulse on user interaction or voice recognition.
     */
    fun performHaptic(context: Context, durationMs: Long = 35) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    /**
     * Plays a high-tech Iron Man HUD activation chime.
     */
    fun playHudActivationSound() {
        fxScope.launch {
            synthesizeTone(frequencies = floatArrayOf(880f, 1320f, 1760f), durationMs = 120)
        }
    }

    /**
     * Plays an Arc Reactor Live Mode engagement pulse.
     */
    fun playLiveModeEngagedSound() {
        fxScope.launch {
            synthesizeTone(frequencies = floatArrayOf(440f, 660f, 880f, 1200f), durationMs = 180)
        }
    }

    /**
     * Synthesizes audio tones using Android AudioTrack.
     */
    private fun synthesizeTone(frequencies: FloatArray, durationMs: Int) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                var sample = 0.0
                for (freq in frequencies) {
                    sample += sin(2.0 * Math.PI * freq * time)
                }
                sample /= frequencies.size
                // Fade in / fade out envelope to avoid click artifacts
                val envelope = when {
                    i < numSamples * 0.15 -> i.toDouble() / (numSamples * 0.15)
                    i > numSamples * 0.85 -> (numSamples - i).toDouble() / (numSamples * 0.15)
                    else -> 1.0
                }
                buffer[i] = (sample * 18000.0 * envelope).toInt().coerceIn(-32767, 32767).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs + 30L)
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
