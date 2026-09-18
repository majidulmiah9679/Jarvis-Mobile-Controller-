package com.example.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
import com.example.auth.JarvisGoogleAuthManager
import com.example.network.JarvisNetworkTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Dual-Engine AI Client Manager for J.A.R.V.I.S. (2026 Edition).
 *
 * Supported Engines:
 * 1. Google Gemini (Cloud / Account Integration - OAuth 2.0 / No Key Needed)
 * 2. Google Gemini (API Key - Supports both AQ... and AIzaSy... with x-goog-api-key header)
 * 3. Groq Cloud LPU (LLaMA 3.3 70B, LLaMA 3.1 8B, Gemma 2 9B)
 *
 * Features:
 * - Real HTTP network traffic tracking (No 0B bug)
 * - 429 Rate limit retry with exponential backoff
 * - J.A.R.V.I.S. "Yes Boss" conversational tone & structured automation JSON parsing
 */
object AiClientManager {

    private const val TAG = "AiClientManager"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Checks if any supported AI engine key has been saved or if Google Account is linked.
     */
    fun hasAnyApiKey(context: Context): Boolean {
        if (JarvisGoogleAuthManager.isSignedIn(context)) return true

        val aiKeysPrefs = context.getSharedPreferences("AiKeys", Context.MODE_PRIVATE)
        val defaultPrefs = context.getSharedPreferences("jarvis_lite_prefs", Context.MODE_PRIVATE)

        val gemini = aiKeysPrefs.getString("GEMINI_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
            .ifEmpty { defaultPrefs.getString("gemini_key", "")?.trim().orEmpty() }
            .ifEmpty { defaultPrefs.getString("JARVIS_GOOGLE_MASTER_KEY", "")?.trim().orEmpty() }
            .ifEmpty { runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("") }

        val groq = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }

        return (gemini.isNotBlank() && gemini != "MY_GEMINI_API_KEY") ||
                groq.isNotBlank()
    }

    /**
     * Executes real AI request using the active selected engine with cascading fallback and retry.
     * Guaranteed to callback on Main Thread.
     */
    fun askAiAuto(
        context: Context,
        prompt: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val aiKeysPrefs = context.getSharedPreferences("AiKeys", Context.MODE_PRIVATE)
            val defaultPrefs = context.getSharedPreferences("jarvis_lite_prefs", Context.MODE_PRIVATE)

            val geminiKey = aiKeysPrefs.getString("GEMINI_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
                .ifEmpty { defaultPrefs.getString("gemini_key", "")?.trim().orEmpty() }
                .ifEmpty { defaultPrefs.getString("JARVIS_GOOGLE_MASTER_KEY", "")?.trim().orEmpty() }
                .ifEmpty { runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("") }

            val groqKey = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }

            val activeBrain = defaultPrefs.getString("JARVIS_ACTIVE_BRAIN", "GEMINI_CLOUD") ?: "GEMINI_CLOUD"
            val systemInstruction = "You are J.A.R.V.I.S., the ultimate personal AI assistant. Always start your response with 'Yes Boss,'. Address the user as Boss. Reply concisely in 1-2 natural sentences with confidence, ready for action."

            var generatedResponse: String? = null
            var lastError = "No configured AI engine key responded"

            // Build priority order based on active selection
            val enginePriority = mutableListOf<String>()
            when (activeBrain.uppercase()) {
                "GEMINI_CLOUD" -> enginePriority.addAll(listOf("GEMINI_CLOUD", "GEMINI", "GROQ"))
                "GEMINI" -> enginePriority.addAll(listOf("GEMINI", "GEMINI_CLOUD", "GROQ"))
                "GROQ" -> enginePriority.addAll(listOf("GROQ", "GEMINI_CLOUD", "GEMINI"))
                else -> enginePriority.addAll(listOf("GEMINI_CLOUD", "GEMINI", "GROQ"))
            }

            for (engine in enginePriority) {
                if (generatedResponse != null) break

                when (engine) {
                    "GEMINI_CLOUD", "GEMINI" -> {
                        // 1. If GEMINI_CLOUD or user logged in, prioritize Firebase AI SDK (Zero 401 Error)
                        if (engine == "GEMINI_CLOUD" || com.example.auth.JarvisGoogleAuthManager.isGoogleCloudLoggedIn(context)) {
                            val fbResult = com.example.network.GeminiNetworkDispatcher.executeFirebaseAi(
                                context = context,
                                prompt = prompt,
                                systemInstruction = systemInstruction,
                                modelName = "gemini-2.5-flash"
                            )
                            if (fbResult.isSuccess) {
                                val text = fbResult.getOrNull()
                                if (!text.isNullOrBlank()) {
                                    generatedResponse = formatJarvisReply(text)
                                    Log.d(TAG, "Gemini Cloud (Firebase AI) responded successfully")
                                    break
                                }
                            }
                        }

                        val cleanGeminiKey = com.example.network.GeminiNetworkDispatcher.sanitizeKey(geminiKey)
                        val effectiveKey = if (cleanGeminiKey.isNotBlank() && cleanGeminiKey != "MY_GEMINI_API_KEY") {
                            cleanGeminiKey
                        } else {
                            runCatching { com.example.network.GeminiNetworkDispatcher.sanitizeKey(BuildConfig.GEMINI_API_KEY) }.getOrDefault("")
                        }

                        val targetModel = defaultPrefs.getString("JARVIS_GEMINI_MODEL", "gemini-2.5-flash") ?: "gemini-2.5-flash"
                        val candidateModels = listOf(targetModel, "gemini-2.5-flash", "gemini-2.5-pro", "gemini-flash-latest", "gemini-3.5-flash")
                            .map { if (it.contains("2.0-flash") || it.contains("1.5-flash")) "gemini-2.5-flash" else it }
                            .distinct()

                        for (m in candidateModels) {
                            var attempt = 0
                            val maxAttempts = 2
                            while (attempt < maxAttempts && generatedResponse == null) {
                                attempt++
                                try {
                                    val reqJson = JSONObject().apply {
                                        put("contents", JSONArray().apply {
                                            put(JSONObject().apply {
                                                put("role", "user")
                                                put("parts", JSONArray().apply {
                                                    put(JSONObject().apply {
                                                        put("text", "System: $systemInstruction\n\nUser: $prompt")
                                                    })
                                                })
                                            })
                                        })
                                    }
                                    val reqBodyStr = reqJson.toString()

                                    // Build request conforming strictly to Google Generative Language API
                                    val request = com.example.network.GeminiNetworkDispatcher.buildGeminiRequest(
                                        key = effectiveKey,
                                        model = m,
                                        jsonBody = reqBodyStr
                                    )
                                    JarvisNetworkTracker.recordTraffic(context, reqBodyStr.length.toLong(), 0L)

                                    httpClient.newCall(request).execute().use { resp ->
                                        var code = resp.code
                                        var body = resp.body?.string().orEmpty()
                                        JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())

                                        if (resp.isSuccessful) {
                                            val j = JSONObject(body)
                                            val text = j.optJSONArray("candidates")?.optJSONObject(0)
                                                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                                                ?.optString("text")
                                            if (!text.isNullOrBlank()) {
                                                generatedResponse = formatJarvisReply(text)
                                                Log.d(TAG, "Gemini ($m) responded successfully")
                                                return@use
                                            }
                                        } else if (code == 429) {
                                            Log.w(TAG, "Gemini 429 rate limit hit on $m, backing off...")
                                            delay(1500L * attempt)
                                        } else if ((code == 401 || code == 400) && (body.contains("OAuth2") || body.contains("API keys are not supported") || body.contains("API_KEY_INVALID"))) {
                                            // Auth mode retry
                                            val isOAuth = com.example.network.GeminiNetworkDispatcher.isGoogleOAuthToken(effectiveKey)
                                            val altReq = com.example.network.GeminiNetworkDispatcher.buildGeminiRequest(
                                                key = effectiveKey,
                                                model = m,
                                                jsonBody = reqBodyStr,
                                                forceOAuthMode = !isOAuth
                                            )
                                            httpClient.newCall(altReq).execute().use { altResp ->
                                                val altBody = altResp.body?.string().orEmpty()
                                                if (altResp.isSuccessful) {
                                                    val j = JSONObject(altBody)
                                                    val text = j.optJSONArray("candidates")?.optJSONObject(0)
                                                        ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                                                        ?.optString("text")
                                                    if (!text.isNullOrBlank()) {
                                                        generatedResponse = formatJarvisReply(text)
                                                        return@use
                                                    }
                                                }
                                            }
                                        } else {
                                            lastError = "Google Gemini ($m) HTTP $code: ${body.take(120)}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    lastError = "Google Gemini: ${e.message}"
                                }
                            }
                            if (generatedResponse != null) break
                        }
                    }

                    "GROQ" -> {
                        val cleanGroqKey = com.example.network.GeminiNetworkDispatcher.sanitizeKey(groqKey)
                        if (cleanGroqKey.isNotBlank()) {
                            try {
                                val groqModel = defaultPrefs.getString("JARVIS_GROQ_MODEL", "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile"
                                val groqJson = JSONObject().apply {
                                    put("model", groqModel)
                                    put("messages", JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("role", "system")
                                            put("content", systemInstruction)
                                        })
                                        put(JSONObject().apply {
                                            put("role", "user")
                                            put("content", prompt)
                                        })
                                    })
                                    put("temperature", 0.7)
                                    put("max_tokens", 512)
                                }
                                val reqStr = groqJson.toString()
                                val req = Request.Builder()
                                    .url("https://api.groq.com/openai/v1/chat/completions")
                                    .addHeader("Authorization", "Bearer $cleanGroqKey")
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("User-Agent", "JARVIS-Android/2026")
                                    .post(reqStr.toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                JarvisNetworkTracker.recordTraffic(context, reqStr.length.toLong(), 0L)
                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())
                                    if (resp.isSuccessful) {
                                        val j = JSONObject(body)
                                        val text = j.optJSONArray("choices")?.optJSONObject(0)
                                            ?.optJSONObject("message")?.optString("content")
                                        if (!text.isNullOrBlank()) {
                                            generatedResponse = formatJarvisReply(text)
                                            Log.d(TAG, "Groq ($groqModel) responded successfully")
                                        }
                                    } else {
                                        val errDetail = try {
                                            JSONObject(body).optJSONObject("error")?.optString("message") ?: body.take(100)
                                        } catch (_: Exception) { body.take(100) }
                                        lastError = "Groq ($groqModel) HTTP ${resp.code}: $errDetail"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "Groq: ${e.message}"
                            }
                        }
                    }
                }
            }

            if (!generatedResponse.isNullOrBlank()) {
                mainHandler.post {
                    onSuccess(generatedResponse!!)
                }
            } else {
                mainHandler.post {
                    onError(lastError)
                }
            }
        }
    }

    /**
     * Ensures all J.A.R.V.I.S. voice and text output addresses the user as "Boss" and starts with "Yes Boss,".
     */
    private fun formatJarvisReply(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.startsWith("Yes Boss,", ignoreCase = true) ||
            trimmed.startsWith("হাঁ বস,", ignoreCase = true) ||
            trimmed.startsWith("জি বস,", ignoreCase = true)) {
            return trimmed
        }
        return "Yes Boss, $trimmed"
    }
}
