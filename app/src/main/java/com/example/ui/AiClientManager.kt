package com.example.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Universal Multi-Engine AI Client Manager for J.A.R.V.I.S.
 * Supports: Google Gemini, Groq (LPU), OpenRouter, DeepSeek, HuggingFace.
 * Provides real-time automated handshake, connection validation, and seamless fallback.
 */
object AiClientManager {

    private const val TAG = "AiClientManager"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Checks if any supported AI engine key has been saved in "AiKeys", "jarvis_lite_prefs", or BuildConfig.
     */
    fun hasAnyApiKey(context: Context): Boolean {
        val aiKeysPrefs = context.getSharedPreferences("AiKeys", Context.MODE_PRIVATE)
        val defaultPrefs = context.getSharedPreferences("jarvis_lite_prefs", Context.MODE_PRIVATE)

        val groq = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }
        val openRouter = aiKeysPrefs.getString("OPENROUTER_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_OPENROUTER_KEY", "")?.trim().orEmpty() }
        val gemini = aiKeysPrefs.getString("GEMINI_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
            .ifEmpty { defaultPrefs.getString("gemini_key", "")?.trim().orEmpty() }
            .ifEmpty { defaultPrefs.getString("JARVIS_GOOGLE_MASTER_KEY", "")?.trim().orEmpty() }
            .ifEmpty { runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("") }
        val deepseek = aiKeysPrefs.getString("DEEPSEEK_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_DEEPSEEK_KEY", "")?.trim().orEmpty() }
        val hf = aiKeysPrefs.getString("HUGGINGFACE_KEY", "")?.trim().orEmpty()
            .ifEmpty { defaultPrefs.getString("JARVIS_HF_KEY", "")?.trim().orEmpty() }

        return groq.isNotBlank() || openRouter.isNotBlank() ||
                (gemini.isNotBlank() && gemini != "MY_GEMINI_API_KEY") ||
                deepseek.isNotBlank() || hf.isNotBlank()
    }

    /**
     * Executes real AI request using the active selected engine with automatic cascading fallback.
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

            // Extract all candidate keys
            val groqKey = aiKeysPrefs.getString("GROQ_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_GROQ_KEY", "")?.trim().orEmpty() }
            val openRouterKey = aiKeysPrefs.getString("OPENROUTER_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_OPENROUTER_KEY", "")?.trim().orEmpty() }
            val geminiKey = aiKeysPrefs.getString("GEMINI_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_GEMINI_KEY", "")?.trim().orEmpty() }
                .ifEmpty { defaultPrefs.getString("gemini_key", "")?.trim().orEmpty() }
                .ifEmpty { defaultPrefs.getString("JARVIS_GOOGLE_MASTER_KEY", "")?.trim().orEmpty() }
                .ifEmpty { runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("") }
            val deepseekKey = aiKeysPrefs.getString("DEEPSEEK_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_DEEPSEEK_KEY", "")?.trim().orEmpty() }
            val hfKey = aiKeysPrefs.getString("HUGGINGFACE_KEY", "")?.trim().orEmpty()
                .ifEmpty { defaultPrefs.getString("JARVIS_HF_KEY", "")?.trim().orEmpty() }

            val activeBrain = defaultPrefs.getString("JARVIS_ACTIVE_BRAIN", "GEMINI") ?: "GEMINI"
            val systemInstruction = "You are J.A.R.V.I.S., the ultimate personal AI assistant. Reply concisely in 1-2 natural sentences with confidence, ready for action."

            var generatedResponse: String? = null
            var lastError = "No configured AI engine key responded"

            // Build priority order starting with active brain
            val enginePriority = mutableListOf<String>()
            when (activeBrain.uppercase()) {
                "GROQ" -> enginePriority.addAll(listOf("GROQ", "GEMINI", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
                "OPENROUTER" -> enginePriority.addAll(listOf("OPENROUTER", "GROQ", "GEMINI", "DEEPSEEK", "HUGGINGFACE"))
                "DEEPSEEK" -> enginePriority.addAll(listOf("DEEPSEEK", "GEMINI", "GROQ", "OPENROUTER", "HUGGINGFACE"))
                "HUGGINGFACE" -> enginePriority.addAll(listOf("HUGGINGFACE", "GEMINI", "GROQ", "OPENROUTER", "DEEPSEEK"))
                else -> enginePriority.addAll(listOf("GEMINI", "GROQ", "OPENROUTER", "DEEPSEEK", "HUGGINGFACE"))
            }

            for (engine in enginePriority) {
                if (generatedResponse != null) break

                when (engine) {
                    "GEMINI" -> {
                        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
                            val targetModel = defaultPrefs.getString("JARVIS_GEMINI_MODEL", "gemini-2.5-flash") ?: "gemini-2.5-flash"
                            val candidateModels = listOf(targetModel, "gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash", "gemini-flash-latest").distinct()

                            for (m in candidateModels) {
                                try {
                                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$m:generateContent?key=$geminiKey"
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
                                    val req = Request.Builder()
                                        .url(url)
                                        .post(reqJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                        .build()

                                    httpClient.newCall(req).execute().use { resp ->
                                        val body = resp.body?.string().orEmpty()
                                        if (resp.isSuccessful) {
                                            val j = JSONObject(body)
                                            val text = j.optJSONArray("candidates")?.optJSONObject(0)
                                                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                                                ?.optString("text")
                                            if (!text.isNullOrBlank()) {
                                                generatedResponse = text.trim()
                                                Log.d(TAG, "Gemini ($m) responded successfully")
                                                return@use
                                            }
                                        } else {
                                            lastError = "Google Gemini ($m) HTTP ${resp.code}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    lastError = "Google Gemini: ${e.message}"
                                }
                                if (generatedResponse != null) break
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
                                val req = Request.Builder()
                                    .url("https://api.groq.com/openai/v1/chat/completions")
                                    .addHeader("Authorization", "Bearer $groqKey")
                                    .post(groqJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    if (resp.isSuccessful) {
                                        val j = JSONObject(body)
                                        val text = j.optJSONArray("choices")?.optJSONObject(0)
                                            ?.optJSONObject("message")?.optString("content")
                                        if (!text.isNullOrBlank()) {
                                            generatedResponse = text.trim()
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
                                val req = Request.Builder()
                                    .url("https://openrouter.ai/api/v1/chat/completions")
                                    .addHeader("Authorization", "Bearer $openRouterKey")
                                    .addHeader("HTTP-Referer", "https://ai.studio")
                                    .addHeader("X-Title", "JARVIS")
                                    .post(orJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    if (resp.isSuccessful) {
                                        val j = JSONObject(body)
                                        val text = j.optJSONArray("choices")?.optJSONObject(0)
                                            ?.optJSONObject("message")?.optString("content")
                                        if (!text.isNullOrBlank()) {
                                            generatedResponse = text.trim()
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
                                val req = Request.Builder()
                                    .url("https://api.deepseek.com/chat/completions")
                                    .addHeader("Authorization", "Bearer $deepseekKey")
                                    .post(dsJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    if (resp.isSuccessful) {
                                        val j = JSONObject(body)
                                        val text = j.optJSONArray("choices")?.optJSONObject(0)
                                            ?.optJSONObject("message")?.optString("content")
                                        if (!text.isNullOrBlank()) {
                                            generatedResponse = text.trim()
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
                                val req = Request.Builder()
                                    .url("https://api-inference.huggingface.co/models/$hfModel")
                                    .addHeader("Authorization", "Bearer $hfKey")
                                    .post(hfJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                                    .build()

                                httpClient.newCall(req).execute().use { resp ->
                                    val body = resp.body?.string().orEmpty()
                                    if (resp.isSuccessful) {
                                        val arr = JSONArray(body)
                                        if (arr.length() > 0) {
                                            val text = arr.getJSONObject(0).optString("generated_text")
                                            if (!text.isNullOrBlank()) {
                                                generatedResponse = text.trim()
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
}
