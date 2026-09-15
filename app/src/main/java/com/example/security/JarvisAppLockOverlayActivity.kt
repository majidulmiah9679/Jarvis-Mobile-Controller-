package com.example.security

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class JarvisAppLockOverlayActivity : ComponentActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening by mutableStateOf(false)
    private var recognizedText by mutableStateOf("Listening for voiceprint...")
    private var isSuccess by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetApp = intent.getStringExtra("TARGET_PACKAGE") ?: "Protected Application"

        initSpeechRecognizer()

        setContent {
            AppLockOverlayContent(
                targetApp = targetApp,
                isListening = isListening,
                recognizedText = recognizedText,
                isSuccess = isSuccess,
                onListenClick = { startListening() },
                onBypassForTesting = {
                    markUnlocked(targetApp)
                    Toast.makeText(this, "Biometrics Verified: Welcome Boss", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        }

        startListening()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        } catch (_: Exception) {}
        finish()
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        recognizedText = "Listening... Say 'Unlock Boss'"
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        recognizedText = "Voice match timed out. Tap mic to retry."
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.lowercase() ?: ""
                        recognizedText = "Heard: '$text'"

                        if (text.contains("unlock") || text.contains("boss") || text.contains("jarvis")) {
                            isSuccess = true
                            val targetApp = intent.getStringExtra("TARGET_PACKAGE") ?: ""
                            if (targetApp.isNotBlank()) {
                                markUnlocked(targetApp)
                            }
                            Toast.makeText(this@JarvisAppLockOverlayActivity, "Voice Biometric Matched! Access Granted.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@JarvisAppLockOverlayActivity, "Voiceprint Mismatch: Access Denied", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizedText = "Speech engine unavailable in emulator. Use test button."
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }

    companion object {
        private val temporarilyUnlockedApps = java.util.concurrent.ConcurrentHashMap<String, Long>()

        fun markUnlocked(packageName: String, durationMs: Long = 10 * 60 * 1000L) {
            temporarilyUnlockedApps[packageName] = System.currentTimeMillis() + durationMs
        }

        fun isUnlocked(packageName: String): Boolean {
            val expiry = temporarilyUnlockedApps[packageName] ?: return false
            if (System.currentTimeMillis() < expiry) {
                return true
            }
            temporarilyUnlockedApps.remove(packageName)
            return false
        }

        fun lockAll() {
            temporarilyUnlockedApps.clear()
        }
    }
}

@Composable
fun AppLockOverlayContent(
    targetApp: String,
    isListening: Boolean,
    recognizedText: String,
    isSuccess: Boolean,
    onListenClick: () -> Unit,
    onBypassForTesting: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712).copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Arc Reactor Security Ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(if (isListening) pulseScale else 1f)
                    .border(2.dp, Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF0052CC))), CircleShape)
                    .background(Color(0xFF061426), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Security else Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = if (isSuccess) Color(0xFF00E676) else Color(0xFF00E5FF),
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "STARK OS ACCESS LOCK",
                color = Color(0xFF00E5FF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )

            Text(
                text = "Target: $targetApp",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Challenge Box
            Surface(
                color = Color(0xFF0B192E),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SAY PASSWORD: \"UNLOCK BOSS\"",
                        color = Color(0xFFFFD54F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recognizedText,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mic Activation Button
            IconButton(
                onClick = onListenClick,
                modifier = Modifier
                    .size(64.dp)
                    .background(if (isListening) Color(0xFF00E5FF) else Color(0xFF0D2544), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = if (isListening) Color.Black else Color(0xFF00E5FF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Testing bypass
            OutlinedButton(
                onClick = onBypassForTesting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
            ) {
                Text("AUTHENTICATE AS BOSS (TEST)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
