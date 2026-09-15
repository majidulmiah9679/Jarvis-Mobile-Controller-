package com.example.duplex

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

enum class DuplexStreamState {
    DISCONNECTED,
    LISTENING,
    AI_SPEAKING,
    BARGE_IN_INTERRUPT
}

/**
 * Real-Time Duplex Audio & Barge-In Engine for J.A.R.V.I.S.
 * Handles continuous audio monitoring, voice activity energy detection,
 * instant interruption (barge-in) when user speaks, and low-latency streaming.
 */
class DuplexAudioEngine(
    private val context: Context,
    private val apiKeyProvider: () -> String,
    private val onInterruptPlayback: () -> Unit
) {

    private val _streamState = MutableStateFlow(DuplexStreamState.DISCONNECTED)
    val streamState: StateFlow<DuplexStreamState> = _streamState.asStateFlow()

    private var recordJob: Job? = null
    private var isEngineActive = false
    private var isAiCurrentlySpeaking = false

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

    // Speech energy threshold for barge-in detection
    private val bargeInAmplitudeThreshold = 1800.0

    private val streamingClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun notifyAiSpeechStarted() {
        isAiCurrentlySpeaking = true
        _streamState.value = DuplexStreamState.AI_SPEAKING
    }

    fun notifyAiSpeechFinished() {
        isAiCurrentlySpeaking = false
        if (isEngineActive) {
            _streamState.value = DuplexStreamState.LISTENING
        }
    }

    /**
     * Starts continuous background audio loop with real-time barge-in detection.
     */
    fun startDuplexLoop(scope: CoroutineScope) {
        if (isEngineActive) return
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        isEngineActive = true
        _streamState.value = DuplexStreamState.LISTENING

        recordJob = scope.launch(Dispatchers.IO) {
            var audioRecord: AudioRecord? = null
            val audioBuffer = ShortArray(bufferSize / 2)

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

                while (isActive && isEngineActive) {
                    if (isPausedByAudioFocus) {
                        kotlinx.coroutines.delay(200)
                        continue
                    }

                    val readCount = audioRecord.read(audioBuffer, 0, audioBuffer.size)
                    if (readCount > 0) {
                        // Calculate RMS Amplitude
                        var sum = 0.0
                        for (i in 0 until readCount) {
                            sum += (audioBuffer[i] * audioBuffer[i]).toDouble()
                        }
                        val rms = sqrt(sum / readCount)

                        // BARGE-IN INTERRUPTION PROTOCOL:
                        // If AI is currently speaking and user speaks above threshold, immediately cut off speech!
                        if (isAiCurrentlySpeaking && rms > bargeInAmplitudeThreshold) {
                            _streamState.value = DuplexStreamState.BARGE_IN_INTERRUPT
                            isAiCurrentlySpeaking = false
                            onInterruptPlayback()
                            _streamState.value = DuplexStreamState.LISTENING
                        }
                    }
                }
            } catch (e: Exception) {
                // Graceful loop exit
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

    fun stopDuplexLoop() {
        isEngineActive = false
        _streamState.value = DuplexStreamState.DISCONNECTED
        recordJob?.cancel()
        recordJob = null
    }

    /**
     * Ultra-Low Latency Streaming via Gemini streamGenerateContent.
     */
    suspend fun streamGenerateContent(
        prompt: String,
        systemInstruction: String,
        onPartialChunk: (String) -> Unit
    ) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            onPartialChunk("API Key required. Please configure it in settings.")
            return@withContext
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?key=$apiKey&alt=sse"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            streamingClient.newCall(request).execute().use { response ->
                val inputStream = response.body?.byteStream() ?: return@use
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val jsonStr = currentLine.removePrefix("data: ").trim()
                        if (jsonStr == "[DONE]") break
                        try {
                            val chunkJson = JSONObject(jsonStr)
                            val candidates = chunkJson.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCand = candidates.getJSONObject(0)
                                val content = firstCand.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text")
                                    if (text.isNotBlank()) {
                                        onPartialChunk(text)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Non-json SSE lines
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onPartialChunk(" [Stream interrupted: ${e.message}]")
        }
    }
}
