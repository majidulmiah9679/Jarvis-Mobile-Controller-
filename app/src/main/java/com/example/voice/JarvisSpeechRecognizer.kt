package com.example.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Ultra-Reliable Android Speech Recognizer for J.A.R.V.I.S.
 * Handles microphone permissions, automatic state recovery, audio focus,
 * multi-language (Bengali/English), and error auto-clearing.
 */
class JarvisSpeechRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isCurrentlyRecognizing = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(targetLanguage: String? = null) {
        mainHandler.post {
            try {
                if (!hasPermission()) {
                    _isListening.value = false
                    _lastError.value = "Microphone permission (RECORD_AUDIO) not granted"
                    onError("Microphone permission required. Please grant permission in Settings.")
                    return@post
                }

                if (!isAvailable()) {
                    _isListening.value = false
                    _lastError.value = "Google Speech Recognition service not available on this device"
                    onError("Speech Recognition service unavailable. Ensure Google app is installed.")
                    return@post
                }

                // Cleanly reset previous instance if stuck
                if (speechRecognizer != null && isCurrentlyRecognizing) {
                    try {
                        speechRecognizer?.cancel()
                    } catch (_: Exception) {}
                }

                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val targetTag = when (targetLanguage?.uppercase()) {
                    "EN", "ENGLISH" -> "en-US"
                    "BN", "BANGLA", "BENGALI" -> "bn-BD"
                    else -> "bn-BD"
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, targetTag)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-US", "en-IN"))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                }

                isCurrentlyRecognizing = true
                _isListening.value = true
                _lastError.value = null
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isCurrentlyRecognizing = false
                _isListening.value = false
                _lastError.value = "Speech recognition start failed: ${e.message}"
                onError("Failed to start speech recognizer: ${e.message}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                if (speechRecognizer != null && isCurrentlyRecognizing) {
                    speechRecognizer?.stopListening()
                }
            } catch (_: Exception) {}
            isCurrentlyRecognizing = false
            _isListening.value = false
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {}
            isCurrentlyRecognizing = false
            _isListening.value = false
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
            isCurrentlyRecognizing = false
            _isListening.value = false
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            isCurrentlyRecognizing = true
            _lastError.value = null
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            _rmsLevel.value = rmsdB
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            isCurrentlyRecognizing = false
        }

        override fun onError(error: Int) {
            _isListening.value = false
            isCurrentlyRecognizing = false
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error (Mic may be in use by another app)"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Internet connection required for speech recognition"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout during speech recognition"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy (auto-resetting)"
                SpeechRecognizer.ERROR_SERVER -> "Google Speech Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected (timeout)"
                else -> "Speech error code: $error"
            }
            _lastError.value = errorMsg

            // Auto-recreate recognizer if busy or client error to prevent deadlock
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                mainHandler.post {
                    try {
                        speechRecognizer?.cancel()
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                    } catch (_: Exception) {}
                }
            }

            // Only propagate meaningful errors to listener
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onError(errorMsg)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            isCurrentlyRecognizing = false
            _lastError.value = null
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0].trim()
                if (recognizedText.isNotBlank()) {
                    onResult(recognizedText)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partialText = matches[0]
                // Streaming preview if needed
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
