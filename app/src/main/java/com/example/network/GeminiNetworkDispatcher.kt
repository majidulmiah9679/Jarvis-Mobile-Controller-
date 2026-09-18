package com.example.network

import android.content.Context
import android.util.Log
import com.example.auth.JarvisGoogleAuthManager
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Gemini Network Dispatcher for J.A.R.V.I.S.
 *
 * Fully updated for latest Google Gemini API specifications (2026):
 * - Supports modern AI Studio API keys (AQ... and AIzaSy... formats)
 * - Supports Google Cloud OAuth2 Access Tokens (ya29... format)
 * - Adaptive Dual-Auth Fallback: Automatically negotiates query-key + x-goog-api-key vs Bearer OAuth
 * - Strips quotes, whitespace, and stray Bearer prefixes from user inputs
 * - Strictly targets modern supported models (gemini-2.5-flash, gemini-2.5-pro, gemini-flash-latest, gemini-3.5-flash)
 */
object GeminiNetworkDispatcher {

    private const val TAG = "GeminiDispatcher"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Sanitizes API keys or tokens by removing quotes, whitespace, and redundant prefixes.
     */
    fun sanitizeKey(key: String): String {
        return key.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .removePrefix("Bearer ")
            .removePrefix("bearer ")
            .trim()
    }

    /**
     * Checks if the given credential is an OAuth2 Access Token (Google OAuth tokens start with ya29.).
     */
    fun isGoogleOAuthToken(key: String): Boolean {
        val clean = sanitizeKey(key)
        return clean.startsWith("ya29.")
    }

    /**
     * Compatibility alias.
     */
    fun isOAuth2Key(key: String): Boolean {
        return isGoogleOAuthToken(key)
    }

    /**
     * Builds a Gemini OkHttp Request conforming to modern Google Generative Language API standards.
     */
    fun buildGeminiRequest(
        key: String,
        model: String = "gemini-2.5-flash",
        jsonBody: String,
        forceOAuthMode: Boolean? = null
    ): Request {
        val cleanKey = sanitizeKey(key)
        val selectedModel = model.ifBlank { "gemini-2.5-flash" }
        val useOAuth = forceOAuthMode ?: isGoogleOAuthToken(cleanKey)

        val url = if (useOAuth) {
            // OAuth2 Bearer tokens MUST NOT include ?key= parameter
            "$BASE_URL/$selectedModel:generateContent"
        } else {
            // Standard AI Studio API keys (AIza... and AQ...) use query parameter + x-goog-api-key header
            "$BASE_URL/$selectedModel:generateContent?key=$cleanKey"
        }

        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        val reqBuilder = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "JARVIS-Android-AI/2026")
            .post(requestBody)

        if (useOAuth) {
            reqBuilder.addHeader("Authorization", "Bearer $cleanKey")
        } else {
            reqBuilder.addHeader("x-goog-api-key", cleanKey)
        }

        val builtRequest = reqBuilder.build()
        Log.d(TAG, "Request target: $url (useOAuth=$useOAuth)")
        return builtRequest
    }

    /**
     * Tests connection to Google Gemini API with smart adaptive dual-auth fallback.
     * Returns: Triple(success: Boolean, message: String, latencyMs: Long)
     */
    suspend fun testConnection(
        context: Context,
        key: String,
        model: String = "gemini-2.5-flash"
    ): Triple<Boolean, String, Long> = withContext(Dispatchers.IO) {
        val cleanKey = sanitizeKey(key)
        if (cleanKey.isBlank()) {
            return@withContext Triple(false, "API Key / OAuth2 Token is missing", 0L)
        }

        val targetModel = if (model.contains("2.0-flash") || model.contains("1.5-flash")) {
            "gemini-2.5-flash"
        } else {
            model.ifBlank { "gemini-2.5-flash" }
        }

        val startTime = System.currentTimeMillis()
        val testJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Respond with 'JARVIS Online - Gemini Connected' in under 8 words.")
                        })
                    })
                })
            })
        }
        val jsonString = testJson.toString()

        // Attempt 1: Standard detection based on key format
        var isOAuthMode = isGoogleOAuthToken(cleanKey)
        var request = buildGeminiRequest(cleanKey, targetModel, jsonString, forceOAuthMode = isOAuthMode)
        JarvisNetworkTracker.recordTraffic(context, jsonString.length.toLong(), 0L)

        try {
            var response = httpClient.newCall(request).execute()
            var code = response.code
            var body = response.body?.string().orEmpty()
            JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())

            // If 401 or 400 (auth format mismatch), try the opposite auth protocol once
            if ((code == 401 || code == 400) && (body.contains("OAuth2") || body.contains("API keys are not supported") || body.contains("API_KEY_INVALID") || body.contains("UNAUTHENTICATED"))) {
                Log.w(TAG, "Primary auth mode ($isOAuthMode) returned $code. Retrying with alternate mode...")
                isOAuthMode = !isOAuthMode
                val retryRequest = buildGeminiRequest(cleanKey, targetModel, jsonString, forceOAuthMode = isOAuthMode)
                response.close()
                response = httpClient.newCall(retryRequest).execute()
                code = response.code
                body = response.body?.string().orEmpty()
                JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())
            }

            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val parsedText = try {
                    val j = JSONObject(body)
                    j.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                } catch (_: Exception) {
                    "Connected (HTTP 200 OK)"
                }
                val modeTag = if (isOAuthMode) "OAuth2 Bearer 🟢" else "API Key 🟢"
                return@withContext Triple(
                    true,
                    "Google Gemini ($targetModel) Connected! $modeTag\nLatency: ${latency}ms\nResponse: ${parsedText.trim()}",
                    latency
                )
            } else {
                val errorMsg = try {
                    val j = JSONObject(body)
                    j.optJSONObject("error")?.optString("message") ?: "HTTP $code: ${body.take(100)}"
                } catch (_: Exception) {
                    "HTTP $code: ${body.take(120)}"
                }
                return@withContext Triple(false, "Gemini Error ($code): $errorMsg", latency)
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e(TAG, "Test Connection failed: ${e.message}", e)
            return@withContext Triple(false, "Network failure: ${e.message ?: "Could not reach Google servers"}", latency)
        }
    }

    /**
     * Executes prompt via Firebase AI SDK (Zero API Key / OAuth2 automated).
     */
    suspend fun executeFirebaseAi(
        context: Context? = null,
        prompt: String,
        systemInstruction: String = "",
        modelName: String = "gemini-2.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ctx = context ?: com.example.JarvisApplication.getInstance()?.applicationContext
            val app = if (ctx != null) {
                val existingApps = FirebaseApp.getApps(ctx)
                if (existingApps.isNotEmpty()) {
                    existingApps.first()
                } else {
                    try {
                        FirebaseApp.initializeApp(ctx)
                    } catch (_: Exception) {
                        null
                    }
                }
            } else {
                null
            }

            if (app == null) {
                return@withContext Result.failure(Exception("Firebase not configured (using Direct Gemini REST)"))
            }

            val targetMdl = if (modelName.contains("2.0-flash") || modelName.contains("1.5-flash")) {
                "gemini-2.5-flash"
            } else {
                modelName.ifBlank { "gemini-2.5-flash" }
            }

            val ai = FirebaseAI.getInstance(app)
            val model = ai.generativeModel(targetMdl)

            val fullPrompt = if (systemInstruction.isNotBlank()) {
                "System Instruction: $systemInstruction\n\nUser: $prompt"
            } else {
                prompt
            }

            val response = model.generateContent(fullPrompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty response from Firebase AI"))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Firebase AI unavailable (${e.message}), falling back to direct REST API")
            Result.failure(e)
        }
    }
}
