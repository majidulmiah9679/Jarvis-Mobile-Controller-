package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

/**
 * True Gemini Live Mode Continuous Always-On Foreground Service.
 * Implements a permanent hands-free loop:
 * Listen -> Process with Gemini 2.5 Flash -> Speak Speech Reply -> Listen again automatically.
 * Works seamlessly in background / minimized state with PARTIAL_WAKE_LOCK and foregroundServiceType="microphone".
 */
class JarvisLiveService : Service(), TextToSpeech.OnInitListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var wakeLock: PowerManager.WakeLock? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    @Volatile
    private var isCurrentlyRecognizing = false

    @Volatile
    private var isAiProcessing = false

    @Volatile
    private var isSpeaking = false

    companion object {
        const val TAG = "JARVIS_LIVE"
        const val CHANNEL_ID = "jarvis_live_channel"
        const val NOTIFICATION_ID = 5055

        const val ACTION_START_LIVE = "ACTION_START_LIVE"
        const val ACTION_STOP_LIVE = "ACTION_STOP_LIVE"

        // State flows accessible across the entire application UI
        private val _isLiveActive = MutableStateFlow(false)
        val isLiveActive: StateFlow<Boolean> = _isLiveActive.asStateFlow()

        private val _liveTranscript = MutableStateFlow("")
        val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

        private val _liveSubtitle = MutableStateFlow("")
        val liveSubtitle: StateFlow<String> = _liveSubtitle.asStateFlow()

        private val _liveStatusText = MutableStateFlow("Live Mode Offline")
        val liveStatusText: StateFlow<String> = _liveStatusText.asStateFlow()

        private val _isListening = MutableStateFlow(false)
        val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

        private val _rmsLevel = MutableStateFlow(0f)
        val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

        // Shared conversation memory (rolling last 10 turns)
        val conversationHistory = mutableListOf<Pair<String, String>>()

        fun startLiveMode(context: Context) {
            val intent = Intent(context, JarvisLiveService::class.java).apply {
                action = ACTION_START_LIVE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopLiveMode(context: Context) {
            val intent = Intent(context, JarvisLiveService::class.java).apply {
                action = ACTION_STOP_LIVE
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        initTtsEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_LIVE
        if (action == ACTION_STOP_LIVE) {
            stopLiveService()
            return START_NOT_STICKY
        }

        _isLiveActive.value = true
        _liveStatusText.value = "JARVIS Live is Active - Always Listening... Boss"
        promoteToForeground()

        mainHandler.post {
            initSpeechRecognizer()
            startContinuousListening()
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "JarvisLive:ContinuousListeningCpuLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(12 * 60 * 60 * 1000L) // 12 hours
            }
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock acquisition notice: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            wakeLock = null
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS Live AI Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Always-on continuous hands-free speech interaction with Gemini 2.5 Flash"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun promoteToForeground() {
        val stopIntent = Intent(this, JarvisLiveService::class.java).apply {
            action = ACTION_STOP_LIVE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("JARVIS Live AI Active ⚡")
            .setContentText("JARVIS Live is Active - Always Listening... Boss")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop Live Mode", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun initTtsEngine() {
        mainHandler.post {
            try {
                tts = TextToSpeech(applicationContext, this)
            } catch (e: Exception) {
                Log.e(TAG, "TTS creation error: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            try {
                // Support Bengali and English
                val bnResult = tts?.setLanguage(Locale("bn", "BD"))
                if (bnResult == TextToSpeech.LANG_MISSING_DATA || bnResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        // IMMEDIATELY call startListening() again inside onDone()!
                        // Infinite loop: Listen -> Process -> Speak -> Listen again automatically
                        mainHandler.post {
                            if (_isLiveActive.value && !isAiProcessing) {
                                startContinuousListening()
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        mainHandler.post {
                            if (_isLiveActive.value && !isAiProcessing) {
                                startContinuousListening()
                            }
                        }
                    }
                })
                Log.d(TAG, "TTS Engine Initialized Successfully")
            } catch (e: Exception) {
                Log.e(TAG, "TTS setup error: ${e.message}")
            }
        } else {
            Log.e(TAG, "TTS Init failed with code $status")
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (speechRecognizer != null) {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext).apply {
                setRecognitionListener(createLiveRecognitionListener())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Recognizer init error: ${e.message}")
        }
    }

    private fun startContinuousListening() {
        if (!_isLiveActive.value || isAiProcessing || isSpeaking) return

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                if (isCurrentlyRecognizing) {
                    try { speechRecognizer?.cancel() } catch (_: Exception) {}
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "en-US"))
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1200L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                }

                isCurrentlyRecognizing = true
                _isListening.value = true
                _liveStatusText.value = "Listening continuously... Speak to JARVIS"
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "startListening error: ${e.message}")
                isCurrentlyRecognizing = false
                _isListening.value = false
                // Auto-retry listening after short pause
                scheduleNextListen(500)
            }
        }
    }

    private fun scheduleNextListen(delayMs: Long = 300) {
        if (!_isLiveActive.value || isAiProcessing || isSpeaking) return
        mainHandler.postDelayed({
            if (_isLiveActive.value && !isAiProcessing && !isSpeaking) {
                startContinuousListening()
            }
        }, delayMs)
    }

    private fun createLiveRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            isCurrentlyRecognizing = true
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

            // Auto-recreate recognizer if client or busy
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                initSpeechRecognizer()
            }

            // In live mode, continuous loop MUST NOT die on timeout or no match!
            if (_isLiveActive.value && !isAiProcessing && !isSpeaking) {
                scheduleNextListen(300)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            isCurrentlyRecognizing = false

            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.firstOrNull()?.trim().orEmpty()

            if (spokenText.isNotBlank()) {
                _liveTranscript.value = spokenText
                handleRecognizedSpeech(spokenText)
            } else {
                scheduleNextListen(200)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()?.trim()
            if (!partial.isNullOrBlank()) {
                _liveTranscript.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleRecognizedSpeech(spokenText: String) {
        val lower = spokenText.lowercase().trim()

        // Check STOP commands: "JARVIS stop", "Live mode off", "stop live", "বন্ধ করো"
        if (lower == "jarvis stop" || lower == "stop" || lower == "live mode off" ||
            lower == "stop live" || lower == "stop live mode" || lower.contains("বন্ধ করো") || lower.contains("লাইভ বন্ধ")
        ) {
            speakLiveReply("Yes Boss, stopping Live Mode. Going to standby.") {
                stopLiveService()
            }
            return
        }

        isAiProcessing = true
        _liveStatusText.value = "Thinking with Gemini 2.5 Flash..."

        serviceScope.launch {
            try {
                // Record into rolling conversation history (keep last 10)
                val response = processGeminiLiveRequest(spokenText)
                withContext(Dispatchers.Main) {
                    isAiProcessing = false
                    _liveSubtitle.value = response
                    speakLiveReply(response) {
                        // After speaking, continuous loop continues via tts onDone()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini Live execution error: ${e.message}")
                withContext(Dispatchers.Main) {
                    isAiProcessing = false
                    val fallbackMsg = "Yes Boss, neural channel active. I am listening."
                    _liveSubtitle.value = fallbackMsg
                    speakLiveReply(fallbackMsg) {}
                }
            }
        }
    }

    private suspend fun processGeminiLiveRequest(userInput: String): String {
        val litePrefs = getSharedPreferences("jarvis_lite_prefs", Context.MODE_PRIVATE)
        val aiKeysPrefs = getSharedPreferences("AiKeys", Context.MODE_PRIVATE)
        val ultPrefs = getSharedPreferences("com.jarvis.ultimate_preferences", Context.MODE_PRIVATE)

        val geminiKey = litePrefs.getString("JARVIS_GOOGLE_MASTER_KEY", "")?.trim().orEmpty()
            .ifEmpty { litePrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
            .ifEmpty { litePrefs.getString("gemini_key", "")?.trim().orEmpty() }
            .ifEmpty { aiKeysPrefs.getString("GEMINI_KEY", "")?.trim().orEmpty() }
            .ifEmpty { ultPrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
            .ifEmpty { runCatching { com.example.BuildConfig.GEMINI_API_KEY.trim() }.getOrDefault("") }
            .let { if (it == "MY_GEMINI_API_KEY") "" else it }

        val groqKey = litePrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty()
            .ifEmpty { aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty() }
            .ifEmpty { ultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }

        val modelName = litePrefs.getString("JARVIS_GEMINI_MODEL", "gemini-2.5-flash") ?: "gemini-2.5-flash"

        // Build rolling conversation context (last 10 turns)
        val contextBuilder = StringBuilder()
        contextBuilder.append("System: You are J.A.R.V.I.S., Tony Stark's ultra-advanced AI operating system. Respond in crisp, highly intelligent, concise voice-friendly Bengali or English. When returning JSON, provide: {\"speech_reply\": \"...\"}. Otherwise, provide clean direct text.\n\n")
        synchronized(conversationHistory) {
            for ((u, a) in conversationHistory.takeLast(10)) {
                contextBuilder.append("User: ").append(u).append("\nJARVIS: ").append(a).append("\n")
            }
        }
        contextBuilder.append("User: ").append(userInput).append("\nJARVIS:")

        // Try Gemini (2.5 Flash / modelName) first
        var aiOutput: String? = null
        if (geminiKey.isNotBlank()) {
            try {
                val reqJson = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "${contextBuilder}\n\nUser: $userInput")
                                })
                            })
                        })
                    })
                }
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = com.example.network.GeminiNetworkDispatcher.buildGeminiRequest(
                    key = com.example.network.GeminiNetworkDispatcher.sanitizeKey(geminiKey),
                    model = modelName,
                    jsonBody = reqJson.toString()
                )
                client.newCall(request).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val b = resp.body?.string().orEmpty()
                        val j = JSONObject(b)
                        aiOutput = j.optJSONArray("candidates")?.optJSONObject(0)
                            ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                            ?.optString("text")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini direct execution error: ${e.message}")
            }
        }

        // Fallback to Groq LPU if Gemini failed or no key
        if (aiOutput.isNullOrBlank() && groqKey.isNotBlank()) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val json = JSONObject().apply {
                    put("model", "llama-3.3-70b-versatile")
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", "You are J.A.R.V.I.S. Respond directly and concisely for voice speech.")
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", userInput)
                        })
                    })
                    put("max_tokens", 300)
                }
                val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val req = okhttp3.Request.Builder()
                    .url("https://api.groq.com/openai/v1/chat/completions")
                    .addHeader("Authorization", "Bearer ${com.example.network.GeminiNetworkDispatcher.sanitizeKey(groqKey)}")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                client.newCall(req).execute().use { r ->
                    if (r.isSuccessful) {
                        val body = r.body?.string().orEmpty()
                        val choices = JSONObject(body).optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            aiOutput = choices.getJSONObject(0).getJSONObject("message").optString("content")
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val rawResult = aiOutput ?: "Yes Boss, bolun. Command executed."
        val extractedReply = extractSpeechReply(rawResult)

        // Save to rolling memory
        synchronized(conversationHistory) {
            conversationHistory.add(userInput to extractedReply)
            if (conversationHistory.size > 20) {
                conversationHistory.removeAt(0)
            }
        }

        return extractedReply
    }

    /**
     * Extracts speech_reply from Gemini JSON response, or falls back to full AI text response.
     */
    private fun extractSpeechReply(raw: String): String {
        val trimmed = raw.trim()
        try {
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val obj = JSONObject(trimmed)
                if (obj.has("speech_reply")) {
                    val sp = obj.optString("speech_reply")
                    if (sp.isNotBlank()) return sp.trim()
                }
                if (obj.has("reply")) {
                    val r = obj.optString("reply")
                    if (r.isNotBlank()) return r.trim()
                }
            }
        } catch (_: Exception) {}

        try {
            val jsonBlockRegex = Regex("```(?:json)?\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
            val match = jsonBlockRegex.find(trimmed)
            if (match != null) {
                val obj = JSONObject(match.groupValues[1])
                val sp = obj.optString("speech_reply", obj.optString("reply", ""))
                if (sp.isNotBlank()) return sp.trim()
            }
        } catch (_: Exception) {}

        try {
            val firstBrace = trimmed.indexOf('{')
            val lastBrace = trimmed.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace > firstBrace) {
                val jsonCandidate = trimmed.substring(firstBrace, lastBrace + 1)
                val obj = JSONObject(jsonCandidate)
                val sp = obj.optString("speech_reply", obj.optString("reply", ""))
                if (sp.isNotBlank()) return sp.trim()
            }
        } catch (_: Exception) {}

        // Fallback to full clean text
        return cleanForSpeech(trimmed)
    }

    private fun cleanForSpeech(text: String): String {
        return text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`{1,3}(.*?)`{1,3}"), "$1")
            .replace(Regex("#{1,6}\\s*"), "")
            .replace(Regex("[-*•]\\s+"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .replace(Regex("[_~>|]"), " ")
            .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun speakLiveReply(reply: String, onFinished: (() -> Unit)? = null) {
        val cleanReply = cleanForSpeech(reply)
        if (cleanReply.isBlank()) {
            onFinished?.invoke()
            return
        }

        // CRITICAL LOG as requested: Log.d("JARVIS_TTS", "Speaking: " + reply)
        Log.d("JARVIS_TTS", "Speaking: $cleanReply")

        mainHandler.post {
            try {
                if (!isTtsReady || tts == null) {
                    // Fallback direct TextToSpeech instance
                    tts = TextToSpeech(applicationContext) { status ->
                        if (status == TextToSpeech.SUCCESS) {
                            val hasBengali = cleanReply.any { it in '\u0980'..'\u09FF' }
                            if (hasBengali) {
                                tts?.setLanguage(Locale("bn", "BD"))
                            } else {
                                tts?.setLanguage(Locale.US)
                            }
                            tts?.setSpeechRate(1.0f)
                            tts?.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply")
                        }
                    }
                    return@post
                }

                val hasBengali = cleanReply.any { it in '\u0980'..'\u09FF' }
                if (hasBengali) {
                    tts?.setLanguage(Locale("bn", "BD"))
                } else {
                    tts?.setLanguage(Locale.US)
                }
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)

                val utteranceId = "jarvis_reply_${System.currentTimeMillis()}"
                val result = tts?.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result == TextToSpeech.ERROR) {
                    tts?.setLanguage(Locale.US)
                    tts?.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, "jarvis_reply_fallback")
                }
            } catch (e: Exception) {
                Log.e(TAG, "TTS speak failed: ${e.message}")
                onFinished?.invoke()
            }
        }
    }

    private fun stopLiveService() {
        _isLiveActive.value = false
        _isListening.value = false
        _liveStatusText.value = "Live Mode Offline"

        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}

            try {
                tts?.stop()
            } catch (_: Exception) {}
        }

        releaseWakeLock()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLiveService()
        try {
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
        serviceScope.cancel()
    }
}
