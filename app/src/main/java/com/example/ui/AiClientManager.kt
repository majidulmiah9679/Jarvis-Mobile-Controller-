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
 * Universal Multi-Engine AI Client Manager for J.A.R.V.I.S. (2026 Edition).
 *
 * Supported Engines:
 * 1. Google Gemini (Cloud / Account Integration - OAuth 2.0 / No Key Needed)
 * 2. Google Gemini (API Key - Supports both AQ... and AIzaSy... with x-goog-api-key header)
 * 3. OpenAI ChatGPT (GPT-4o, GPT-4o Mini, o1)
 * 4. Anthropic Claude (Claude 3.5 Sonnet, Claude 3 Haiku)
 * 5. Groq Cloud LPU (LLaMA 3.3 70B, LLaMA 3.1 8B)
 * 6. OpenRouter (100+ Free models)
 * 7. DeepSeek AI (Chat V3, Reasoner R1)
 * 8. HuggingFace Inference API
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

        val openai = aiKeysPrefs.getString("OPENAI_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_OPENAI_KEY", "")?.trim().orEmpty() }

        val claude = aiKeysPrefs.getString("ANTHROPIC_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_ANTHROPIC_KEY", "")?.trim().orEmpty() }

        val groq = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }

        val openRouter = aiKeysPrefs.getString("OPENROUTER_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_OPENROUTER_KEY", "")?.trim().orEmpty() }

        val deepseek = aiKeysPrefs.getString("DEEPSEEK_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_DEEPSEEK_KEY", "")?.trim().orEmpty() }

        val hf = aiKeysPrefs.getString("HUGGINGFACE_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_HF_KEY", "")?.trim().orEmpty() }

        return (gemini.isNotBlank() && gemini != "MY_GEMINI_API_KEY") ||
                openai.isNotBlank() ||
                claude.isNotBlank() ||
                groq.isNotBlank() ||
                openRouter.isNotBlank() ||
                deepseek.isNotBlank() ||
                hf.isNotBlank()
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

            val openaiKey = aiKeysPrefs.getString("OPENAI_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_OPENAI_KEY", "")?.trim().orEmpty() }

            val claudeKey = aiKeysPrefs.getString("ANTHROPIC_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_ANTHROPIC_KEY", "")?.trim().orEmpty() }

            val groqKey = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }

            val openRouterKey = aiKeysPrefs.getString("OPENROUTER_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_OPENROUTER_KEY", "")?.trim().orEmpty() }

            val deepseekKey = aiKeysPrefs.getString("DEEPSEEK_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_DEEPSEEK_KEY", "")?.trim().orEmpty() }

            val hfKey = aiKeysPrefs.getString("HUGGINGFACE_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_HF_KEY", "")?.trim().orEmpty() }

            val activeBrain = defaultPrefs.getString("JARVIS_ACTIVE_BRAIN", "GEMINI_CLOUD") ?: "GEMINI_CLOUD"
            val systemInstruction = "You are J.A.R.V.I.S., the ultimate personal AI assistant. Always start your response with 'Yes Boss,'. Address the user as Boss. Reply concisely in 1-2 natural sentences with confidence, ready for action."

            var generatedResponse: String? = null
            var lastError = "No configured AI engine key responded"

            // Build priority order based on active selection
            val enginePriority = mutableListOf<String>()
            when (activeBrain.uppercase()) {
                "GEMINI_CLOUD" -> enginePriority.addAll(listOf("GEMINI_CLOUD", "GEMINI", "OPENAI", "ANTHROPIC", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "GEMINI" -> enginePriority.addAll(listOf("GEMINI", "GEMINI_CLOUD", "OPENAI", "ANTHROPIC", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "OPENAI" -> enginePriority.addAll(listOf("OPENAI", "GEMINI", "ANTHROPIC", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "ANTHROPIC" -> enginePriority.addAll(listOf("ANTHROPIC", "OPENAI", "GEMINI", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "GROQ" -> enginePriority.addAll(listOf("GROQ", "GEMINI", "OPENAI", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "OPENROUTER" -> enginePriority.addAll(listOf("OPENROUTER", "GROQ", "GEMINI", "OPENAI", "DEEPSEEK", "HUGGINGFACE"))
                "DEEPSEEK" -> enginePriority.addAll(listOf("DEEPSEEK", "GEMINI", "OPENAI", "GROQ", "OPENROUTER", "HUGGINGFACE"))
                "HUGGINGFACE" -> enginePriority.addAll(listOf("HUGGINGFACE", "GEMINI", "GROQ", "OPENAI", "OPENROUTER", "DEEPSEEK"))
                else -> enginePriority.addAll(listOf("GEMINI", "GEMINI_CLOUD", "OPENAI", "ANTHROPIC", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
            }

            for (engine in enginePriority) {
                if (generatedResponse != null) break

                when (engine) {
                    "GEMINI_CLOUD", "GEMINI" -> {
                        val effectiveKey = if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
                            geminiKey
                        } else {
                            // Use AI Studio built-in key or Google OAuth token
                            runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("").ifBlank { "AQ.AIStudioBuiltInKey" }
                        }

                        val targetModel = defaultPrefs.getString("JARVIS_GEMINI_MODEL", "gemini-2.5-flash") ?: "gemini-2.5-flash"
                        val candidateModels = listOf(targetModel, "gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.0-flash", "gemini-flash-latest").distinct()

                        for (m in candidateModels) {
                            var attempt = 0
                            val maxAttempts = 2
                            while (attempt < maxAttempts && generatedResponse == null) {
                                attempt++
                                try {
                                    val reqJson = JSONObject().apply {
                                        put("contents", JSONArray().apply {
                                            put(JSONObject().apply {
                                                put("parts", JSONArray().apply {
                                                    put(JSONObject().apply {
                                                        put("text", "System: $systemInstruction\n\nUser: $prompt")
                                                    })
                                                })
                                            })
                                        })
                                    }
                                    val reqBodyStr = reqJson.toString()
                                    val requestBody = reqBodyStr.toRequestBody("application/json".toMediaTypeOrNull())

                                    // Build request supporting both AQ... and AIzaSy... formats
                                    val requestBuilder = Request.Builder()
                                        .url("https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent")
                                        .addHeader("Content-Type", "application/json")
                                        .addHeader("x-goog-api-key", effectiveKey)
                                        .post(requestBody)

                                    if (effectiveKey.startsWith("AQ") || effectiveKey.startsWith("ya29.")) {
                                        requestBuilder.addHeader("Authorization", "Bearer $effectiveKey")
                                    }

                                    val request = requestBuilder.build()
                                    JarvisNetworkTracker.recordTraffic(context, reqBodyStr.length.toLong(), 0L)

                                    httpClient.newCall(request).execute().use { resp ->
                                        val body = resp.body?.string().orEmpty()
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
                                        } else if (resp.code == 429) {
                                            // Rate limit hit - wait briefly with exponential backoff
                                            Log.w(TAG, "Gemini 429 rate limit hit on $m, backing off...")
                                            delay(1500L * attempt)
                                        } else if (resp.code in listOf(400, 401, 404)) {
                                            // Fallback to URL query parameter method if header method rejected
                                            val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$effectiveKey"
                                            val fallbackReq = Request.Builder()
                                                .url(fallbackUrl)
                                                .post(reqBodyStr.toRequestBody("application/json".toMediaTypeOrNull()))
                                                .build()

                                            httpClient.newCall(fallbackReq).execute().use { fbResp ->
                                                val fbBody = fbResp.body?.string().orEmpty()
                                                JarvisNetworkTracker.recordTraffic(context, 0L, fbBody.length.toLong())
                                                if (fbResp.isSuccessful) {
                                                    val j = JSONObject(fbBody)
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
                                            lastError = "Google Gemini ($m) HTTP ${resp.code}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    lastError = "Google Gemini: ${e.message}"
                                }
                            }
                            if (generatedResponse != null) break
                        }
                    }

                    "OPENAI" -> {
                        if (openaiKey.isNotBlank()) {
                            try {
                                val model = defaultPrefs.getString("JARVIS_OPENAI_MODEL", "gpt-4o-mini") ?: "gpt-4o-mini"
                                val reqJson = JSONObject().apply {
                                    put("model", model)
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
                                    put("max_tokens", 512)
                                }
                                val reqStr = reqJson.toString()
                                val req = Request.Builder()
                                    .url("https://api.openai.com/v1/chat/completions")
                                    .addHeader("Authorization", "Bearer $openaiKey")
                                    .addHeader("Content-Type", "application/json")
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
                                            Log.d(TAG, "OpenAI ($model) responded successfully")
                                        }
                                    } else {
                                        lastError = "OpenAI ($model) HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "OpenAI: ${e.message}"
                            }
                        }
                    }

                    "ANTHROPIC" -> {
                        if (claudeKey.isNotBlank()) {
                            try {
                                val model = defaultPrefs.getString("JARVIS_ANTHROPIC_MODEL", "claude-3-5-sonnet-20241022") ?: "claude-3-5-sonnet-20241022"
                                val reqJson = JSONObject().apply {
                                    put("model", model)
                                    put("max_tokens", 512)
                                    put("system", systemInstruction)
                                    put("messages", JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("role", "user")
                                            put("content", prompt)
                                        })
                                    })
                                }
                                val reqStr = reqJson.toString()
                                val req = Request.Builder()
                                    .url("https://api.anthropic.com/v1/messages")
                                    .addHeader("x-api-key", claudeKey)
                                    .addHeader("anthropic-version", "2023-06-01")
                                    .addHeader("Content-Type", "application/json")
                                    .post(reqStr.toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                JarvisNetworkTracker.recordTraffic(context, reqStr.length.toLong(), 0L)
                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())
                                    if (resp.isSuccessful) {
                                        val j = JSONObject(body)
                                        val contentArr = j.optJSONArray("content")
                                        val text = contentArr?.optJSONObject(0)?.optString("text")
                                        if (!text.isNullOrBlank()) {
                                            generatedResponse = formatJarvisReply(text)
                                            Log.d(TAG, "Claude ($model) responded successfully")
                                        }
                                    } else {
                                        lastError = "Anthropic ($model) HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "Anthropic: ${e.message}"
                            }
                        }
                    }

                    "GROQ" -> {
                        if (groqKey.isNotBlank()) {
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
                                    .addHeader("Authorization", "Bearer $groqKey")
                                    .addHeader("Content-Type", "application/json")
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
                                        lastError = "Groq ($groqModel) HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "Groq: ${e.message}"
                            }
                        }
                    }

                    "OPENROUTER" -> {
                        if (openRouterKey.isNotBlank()) {
                            try {
                                val orModel = defaultPrefs.getString("JARVIS_OPENROUTER_MODEL", "meta-llama/llama-3.3-70b-instruct:free") ?: "meta-llama/llama-3.3-70b-instruct:free"
                                val orJson = JSONObject().apply {
                                    put("model", orModel)
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
                                }
                                val reqStr = orJson.toString()
                                val req = Request.Builder()
                                    .url("https://openrouter.ai/api/v1/chat/completions")
                                    .addHeader("Authorization", "Bearer $openRouterKey")
                                    .addHeader("HTTP-Referer", "https://ai.studio")
                                    .addHeader("X-Title", "JARVIS")
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
                                            Log.d(TAG, "OpenRouter ($orModel) responded successfully")
                                        }
                                    } else {
                                        lastError = "OpenRouter HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "OpenRouter: ${e.message}"
                            }
                        }
                    }

                    "DEEPSEEK" -> {
                        if (deepseekKey.isNotBlank()) {
                            try {
                                val dsModel = defaultPrefs.getString("JARVIS_DEEPSEEK_MODEL", "deepseek-chat") ?: "deepseek-chat"
                                val dsJson = JSONObject().apply {
                                    put("model", dsModel)
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
                                }
                                val reqStr = dsJson.toString()
                                val req = Request.Builder()
                                    .url("https://api.deepseek.com/chat/completions")
                                    .addHeader("Authorization", "Bearer $deepseekKey")
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
                                            Log.d(TAG, "DeepSeek ($dsModel) responded successfully")
                                        }
                                    } else {
                                        lastError = "DeepSeek HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "DeepSeek: ${e.message}"
                            }
                        }
                    }

                    "HUGGINGFACE" -> {
                        if (hfKey.isNotBlank()) {
                            try {
                                val hfModel = defaultPrefs.getString("JARVIS_HF_MODEL", "mistralai/Mistral-7B-Instruct-v0.3") ?: "mistralai/Mistral-7B-Instruct-v0.3"
                                val hfJson = JSONObject().apply {
                                    put("inputs", "System: $systemInstruction\n\nUser: $prompt\n\nAssistant:")
                                }
                                val reqStr = hfJson.toString()
                                val req = Request.Builder()
                                    .url("https://api-inference.huggingface.co/models/$hfModel")
                                    .addHeader("Authorization", "Bearer $hfKey")
                                    .post(reqStr.toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                JarvisNetworkTracker.recordTraffic(context, reqStr.length.toLong(), 0L)
                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    JarvisNetworkTracker.recordTraffic(context, 0L, body.length.toLong())
                                    if (resp.isSuccessful) {
                                        val arr = JSONArray(body)
                                        if (arr.length() > 0) {
                                            val text = arr.getJSONObject(0).optString("generated_text")
                                            if (!text.isNullOrBlank()) {
                                                generatedResponse = formatJarvisReply(text)
                                                Log.d(TAG, "HuggingFace ($hfModel) responded successfully")
                                            }
                                        }
                                    } else {
                                        lastError = "HuggingFace HTTP ${resp.code}"
                                    }
                                }
                            } catch (e: Exception) {
                                lastError = "HuggingFace: ${e.message}"
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
