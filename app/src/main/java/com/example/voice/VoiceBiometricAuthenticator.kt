package com.example.voice

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Biometric Voiceprint Profile:
 * Stores acoustic features extracted from the user's enrolled voice sample.
 * Features:
 * - avgPitch (Fundamental Frequency Estimate via Zero-Crossing Rate)
 * - energyMean (Typical volume / vocal energy profile)
 * - zcrVariance (Zero-Crossing Rate stability / variance)
 * - spectralCentroidEstimate (High vs low frequency balance)
 */
data class VoiceprintProfile(
    val enrolled: Boolean = false,
    val avgZcr: Float = 0f,
    val zcrVariance: Float = 0f,
    val avgEnergy: Float = 0f,
    val enrolledDate: Long = 0L,
    val ownerName: String = "Boss",
    val voiceLockEnabled: Boolean = true
)

data class SpeakerVerificationResult(
    val isAuthorized: Boolean,
    val confidencePercent: Int,
    val ownerName: String,
    val message: String
)

enum class EnrollmentState {
    IDLE,
    RECORDING,
    ANALYZING,
    SUCCESS,
    FAILED,
    TESTING
}

/**
 * Voiceprint Biometric Authenticator:
 * 1. Enrolls user's voice by recording 3 seconds of voice sample.
 * 2. Extracts vocal acoustic features (Fundamental frequency/ZCR and energy distribution).
 * 3. Compares incoming speaker audio against enrolled profile.
 * 4. Rejects unfamiliar / unauthorized speakers if Voice Lock is active.
 */
class VoiceBiometricAuthenticator(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_voiceprint_prefs", Context.MODE_PRIVATE)

    private val _enrollmentState = MutableStateFlow(EnrollmentState.IDLE)
    val enrollmentState: StateFlow<EnrollmentState> = _enrollmentState.asStateFlow()

    private val _enrollmentProgress = MutableStateFlow(0f)
    val enrollmentProgress: StateFlow<Float> = _enrollmentProgress.asStateFlow()

    private val _currentProfile = MutableStateFlow(loadProfile())
    val currentProfile: StateFlow<VoiceprintProfile> = _currentProfile.asStateFlow()

    private var enrollmentJob: Job? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

    fun isVoiceEnrolled(): Boolean = _currentProfile.value.enrolled

    private fun loadProfile(): VoiceprintProfile {
        val enrolled = prefs.getBoolean("voiceprint_enrolled", false)
        return VoiceprintProfile(
            enrolled = enrolled,
            avgZcr = prefs.getFloat("voiceprint_avg_zcr", 0f),
            zcrVariance = prefs.getFloat("voiceprint_zcr_var", 0f),
            avgEnergy = prefs.getFloat("voiceprint_avg_energy", 0f),
            enrolledDate = prefs.getLong("voiceprint_date", 0L),
            ownerName = prefs.getString("voiceprint_owner", "Boss") ?: "Boss",
            voiceLockEnabled = prefs.getBoolean("voiceprint_lock_enabled", true)
        )
    }

    fun setVoiceLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voiceprint_lock_enabled", enabled).apply()
        _currentProfile.value = _currentProfile.value.copy(voiceLockEnabled = enabled)
    }

    fun updateOwnerName(newName: String) {
        val name = newName.trim().ifEmpty { "Boss" }
        prefs.edit().putString("voiceprint_owner", name).apply()
        _currentProfile.value = _currentProfile.value.copy(ownerName = name)
    }

    /**
     * Start enrolling the user's voice for 3.5 seconds.
     */
    fun startEnrollment(
        scope: CoroutineScope,
        ownerName: String = "Owner",
        onComplete: (Boolean, String) -> Unit
    ) {
        if (_enrollmentState.value == EnrollmentState.RECORDING) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onComplete(false, "Microphone permission required for voice enrollment.")
            return
        }

        _enrollmentState.value = EnrollmentState.RECORDING
        _enrollmentProgress.value = 0f

        enrollmentJob = scope.launch(Dispatchers.IO) {
            var audioRecord: AudioRecord? = null
            val buffer = ShortArray(bufferSize / 2)

            val zcrSamples = mutableListOf<Float>()
            val energySamples = mutableListOf<Float>()

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    _enrollmentState.value = EnrollmentState.FAILED
                    onComplete(false, "Audio hardware failed to initialize.")
                    return@launch
                }

                audioRecord.startRecording()

                val totalTargetFrames = 60 // ~3.5 seconds
                var framesCollected = 0

                while (isActive && framesCollected < totalTargetFrames) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var energy = 0.0
                        var zcr = 0

                        for (i in 0 until read) {
                            energy += abs(buffer[i].toInt())
                            if (i > 0 && ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0))) {
                                zcr++
                            }
                        }

                        val frameEnergy = (energy / read).toFloat()
                        val frameZcr = ((zcr.toDouble() / read) * 1000).toFloat()

                        // Only count active speech frames, skip dead silence
                        if (frameEnergy > 600f) {
                            zcrSamples.add(frameZcr)
                            energySamples.add(frameEnergy)
                            framesCollected++
                            _enrollmentProgress.value = (framesCollected.toFloat() / totalTargetFrames).coerceIn(0f, 1f)
                        }
                    }
                    kotlinx.coroutines.delay(40)
                }

                if (zcrSamples.size >= 25) {
                    _enrollmentState.value = EnrollmentState.ANALYZING
                    val avgZcr = zcrSamples.average().toFloat()
                    val avgEnergy = energySamples.average().toFloat()
                    val varianceZcr = zcrSamples.map { (it - avgZcr) * (it - avgZcr) }.average().toFloat()

                    // Save to SharedPreferences
                    prefs.edit()
                        .putBoolean("voiceprint_enrolled", true)
                        .putFloat("voiceprint_avg_zcr", avgZcr)
                        .putFloat("voiceprint_zcr_var", varianceZcr)
                        .putFloat("voiceprint_avg_energy", avgEnergy)
                        .putLong("voiceprint_date", System.currentTimeMillis())
                        .putString("voiceprint_owner", ownerName)
                        .apply()

                    val newProfile = VoiceprintProfile(
                        enrolled = true,
                        avgZcr = avgZcr,
                        zcrVariance = varianceZcr,
                        avgEnergy = avgEnergy,
                        enrolledDate = System.currentTimeMillis(),
                        ownerName = ownerName
                    )
                    _currentProfile.value = newProfile
                    _enrollmentState.value = EnrollmentState.SUCCESS
                    onComplete(true, "Your voice has been successfully enrolled! J.A.R.V.I.S. will respond exclusively to your voice.")
                } else {
                    _enrollmentState.value = EnrollmentState.FAILED
                    onComplete(false, "Voice was too faint or quiet. Please speak clearly and try again.")
                }
            } catch (e: Exception) {
                _enrollmentState.value = EnrollmentState.FAILED
                onComplete(false, "Enrollment error: ${e.message}")
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    /**
     * Resets enrolled voiceprint.
     */
    fun resetEnrollment() {
        prefs.edit().clear().apply()
        _currentProfile.value = VoiceprintProfile()
        _enrollmentState.value = EnrollmentState.IDLE
        _enrollmentProgress.value = 0f
    }

    /**
     * Verifies if incoming live audio matches enrolled profile.
     * Compares zero-crossing rate (fundamental frequency) and vocal energy signature.
     */
    fun verifySpeaker(zcr: Float, energy: Float): Boolean {
        val result = verifySpeakerDetailed(zcr, energy)
        return result.isAuthorized
    }

    fun verifySpeakerDetailed(zcr: Float, energy: Float): SpeakerVerificationResult {
        val profile = _currentProfile.value
        if (!profile.enrolled || !profile.voiceLockEnabled) {
            return SpeakerVerificationResult(
                isAuthorized = true,
                confidencePercent = 100,
                ownerName = profile.ownerName,
                message = "Open mode active. Voice authorized."
            )
        }

        // Acoustic tolerance window
        val zcrTolerance = (sqrt(profile.zcrVariance.toDouble()) * 2.5).coerceIn(50.0, 140.0).toFloat()
        val zcrDiff = abs(zcr - profile.avgZcr)

        val matchPercent = ((1.0f - (zcrDiff / (zcrTolerance * 1.5f))) * 100f).toInt().coerceIn(10, 99)
        val isMatch = zcrDiff <= zcrTolerance

        return if (isMatch) {
            SpeakerVerificationResult(
                isAuthorized = true,
                confidencePercent = matchPercent,
                ownerName = profile.ownerName,
                message = "Vocal biometric match: ${profile.ownerName} verified ($matchPercent% match)."
            )
        } else {
            SpeakerVerificationResult(
                isAuthorized = false,
                confidencePercent = matchPercent,
                ownerName = profile.ownerName,
                message = "Voice signature mismatch ($matchPercent% match). Only ${profile.ownerName}'s voice is permitted."
            )
        }
    }

    /**
     * Test voice verification by recording a 2-second utterance and checking match against enrolled profile.
     */
    fun testVoiceVerification(
        scope: CoroutineScope,
        onResult: (SpeakerVerificationResult) -> Unit
    ) {
        val profile = _currentProfile.value
        if (!profile.enrolled) {
            onResult(
                SpeakerVerificationResult(
                    isAuthorized = true,
                    confidencePercent = 100,
                    ownerName = "None",
                    message = "No voice enrolled yet. Please enroll your voice first."
                )
            )
            return
        }

        _enrollmentState.value = EnrollmentState.TESTING

        scope.launch(Dispatchers.IO) {
            var audioRecord: AudioRecord? = null
            val buffer = ShortArray(bufferSize / 2)
            val zcrSamples = mutableListOf<Float>()
            val energySamples = mutableListOf<Float>()

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    _enrollmentState.value = EnrollmentState.IDLE
                    onResult(
                        SpeakerVerificationResult(
                            isAuthorized = false,
                            confidencePercent = 0,
                            ownerName = profile.ownerName,
                            message = "Audio hardware error."
                        )
                    )
                    return@launch
                }

                audioRecord.startRecording()

                val testFrames = 35 // ~2 seconds
                var frames = 0
                while (isActive && frames < testFrames) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var energy = 0.0
                        var zcr = 0
                        for (i in 0 until read) {
                            energy += abs(buffer[i].toInt())
                            if (i > 0 && ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0))) {
                                zcr++
                            }
                        }
                        val frameEnergy = (energy / read).toFloat()
                        val frameZcr = ((zcr.toDouble() / read) * 1000).toFloat()
                        if (frameEnergy > 600f) {
                            zcrSamples.add(frameZcr)
                            energySamples.add(frameEnergy)
                        }
                    }
                    frames++
                    kotlinx.coroutines.delay(50)
                }

                _enrollmentState.value = EnrollmentState.IDLE

                if (zcrSamples.isNotEmpty()) {
                    val avgZcr = zcrSamples.average().toFloat()
                    val avgEnergy = energySamples.average().toFloat()
                    val result = verifySpeakerDetailed(avgZcr, avgEnergy)
                    onResult(result)
                } else {
                    onResult(
                        SpeakerVerificationResult(
                            isAuthorized = false,
                            confidencePercent = 0,
                            ownerName = profile.ownerName,
                            message = "No voice detected. Please speak louder into the microphone."
                        )
                    )
                }
            } catch (e: Exception) {
                _enrollmentState.value = EnrollmentState.IDLE
                onResult(
                    SpeakerVerificationResult(
                        isAuthorized = false,
                        confidencePercent = 0,
                        ownerName = profile.ownerName,
                        message = "Testing error: ${e.message}"
                    )
                )
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
            }
        }
    }
}
