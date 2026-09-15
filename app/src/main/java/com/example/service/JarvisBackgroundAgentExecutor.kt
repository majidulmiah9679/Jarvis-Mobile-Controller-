package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.speech.tts.TextToSpeech
import com.example.MainActivity
import com.example.hardware.SystemHardwareController
import com.example.voice.JarvisSpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * J.A.R.V.I.S. Background Agent Executor.
 * Handles background voice listening, wake-word response, and executes all phone
 * settings, hardware actions, and app controls even when the app is completely closed
 * or the phone screen is turned off.
 */
class JarvisBackgroundAgentExecutor(
    private val context: Context,
    private val onLogAction: (String) -> Unit = {}
) {

    private val executorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val hardwareController = SystemHardwareController(context)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: JarvisSpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        initializeTts()
        initializeSpeechRecognizer()
    }

    private fun initializeTts() {
        mainHandler.post {
            try {
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale.US
                        tts?.setPitch(1.0f)
                        tts?.setSpeechRate(1.05f)
                        isTtsReady = true
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = JarvisSpeechRecognizer(
            context = context,
            onResult = { recognizedText ->
                handleBackgroundCommand(recognizedText)
            },
            onError = { _ ->
                // Handled gracefully in background
            }
        )
    }

    /**
     * Wakes up the screen if it is turned off.
     */
    fun wakeUpScreen(durationMs: Long = 10000) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "JARVIS:BackgroundAgentWakeScreen"
            )
            wakeLock?.acquire(durationMs)
        } catch (_: Exception) {}
    }

    /**
     * Called when offline wake-word ("Hey Jarvis") is triggered in background.
     */
    fun onWakeWordTriggered(keyword: String) {
        executorScope.launch {
            // Wake screen up if sleeping
            wakeUpScreen(12000)
            hardwareController.vibrate(50)

            // Audio confirmation prompt
            speak("J.A.R.V.I.S. online. Yes Boss?")
            delay(1200)

            // Start listening for the user's voice command
            startListeningForCommand()
        }
    }

    fun startListeningForCommand() {
        mainHandler.post {
            try {
                speechRecognizer?.startListening()
            } catch (_: Exception) {}
        }
    }

    fun stopListeningForCommand() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    fun speak(text: String) {
        if (!isTtsReady || tts == null) return
        mainHandler.post {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_bg_tts_${System.currentTimeMillis()}")
            } catch (_: Exception) {}
        }
    }

    /**
     * Executes phone settings, hardware switches, app launches, or conversational responses.
     */
    fun handleBackgroundCommand(command: String) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) return

        onLogAction("Background Voice Command: $trimmed")
        val lower = trimmed.lowercase(Locale.ROOT)

        when {
            // 1. FLASHLIGHT / TORCH
            lower.contains("torch on") || lower.contains("flashlight on") ||
                    lower.contains("light on") || lower.contains("আলো জ্বালাও") ||
                    lower.contains("টর্চ অন") || lower.contains("ফ্ল্যাশ অন") -> {
                val ok = hardwareController.setTorch(true)
                if (ok) {
                    speak("Flashlight engaged, Boss.")
                } else {
                    speak("Flashlight could not be turned on.")
                }
            }

            lower.contains("torch off") || lower.contains("flashlight off") ||
                    lower.contains("light off") || lower.contains("আলো বন্ধ") ||
                    lower.contains("টর্চ বন্ধ") || lower.contains("ফ্ল্যাশ বন্ধ") -> {
                val ok = hardwareController.setTorch(false)
                if (ok) {
                    speak("Flashlight turned off.")
                } else {
                    speak("Flashlight is already off.")
                }
            }

            // 2. VOLUME CONTROLS
            lower.contains("volume up") || lower.contains("sound up") ||
                    lower.contains("সাউন্ড বাড়াও") || lower.contains("ভলিউম বাড়াও") -> {
                val cur = hardwareController.getMediaVolumePercent()
                val target = (cur + 20).coerceAtMost(100)
                hardwareController.setMediaVolumePercent(target)
                speak("Volume increased to $target percent.")
            }

            lower.contains("volume down") || lower.contains("sound down") ||
                    lower.contains("সাউন্ড কমাও") || lower.contains("ভলিউম কমাও") -> {
                val cur = hardwareController.getMediaVolumePercent()
                val target = (cur - 20).coerceAtLeast(0)
                hardwareController.setMediaVolumePercent(target)
                speak("Volume decreased to $target percent.")
            }

            lower.contains("volume 100") || lower.contains("volume max") ||
                    lower.contains("full sound") || lower.contains("ফুল সাউন্ড") -> {
                hardwareController.maxVolume()
                speak("Volume set to maximum.")
            }

            lower.contains("mute") || lower.contains("মিউট") || lower.contains("sound off") -> {
                hardwareController.muteMedia()
                speak("Media sound muted.")
            }

            // 3. RINGER MODES
            lower.contains("vibrate") || lower.contains("ভাইব্রেশন") -> {
                hardwareController.setRingerMode(SystemHardwareController.RingerMode.VIBRATE)
                speak("Ringer set to vibrate mode.")
            }

            lower.contains("silent") || lower.contains("সাইলেন্ট") -> {
                hardwareController.setRingerMode(SystemHardwareController.RingerMode.SILENT)
                speak("Phone set to silent mode.")
            }

            lower.contains("normal ringer") || lower.contains("রিং মোড") -> {
                hardwareController.setRingerMode(SystemHardwareController.RingerMode.NORMAL)
                speak("Ringer mode set to normal.")
            }

            // 4. BRIGHTNESS
            lower.contains("brightness max") || lower.contains("full brightness") ||
                    lower.contains("ডিসপ্লে ব্রাইটনেস ফুল") -> {
                hardwareController.setBrightness(100)
                speak("Display brightness set to maximum.")
            }

            lower.contains("brightness low") || lower.contains("কম আলো") -> {
                hardwareController.setBrightness(20)
                speak("Display brightness reduced.")
            }

            // 5. BATTERY TELEMETRY
            lower.contains("battery") || lower.contains("charge") ||
                    lower.contains("চার্জ") || lower.contains("ব্যাটারি") -> {
                val battery = hardwareController.getBatteryStatus()
                speak("Battery is at ${battery.percentage} percent. Status: ${battery.statusText}. Core temperature: ${battery.temperatureCelsius} degrees Celsius.")
            }

            // 6. TIME & DATE
            lower.contains("time") || lower.contains("কয়টা বাজে") || lower.contains("সময়") -> {
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                speak("Current time is $timeStr, Commander.")
            }

            lower.contains("date") || lower.contains("তারিখ") || lower.contains("আজকে কি বার") -> {
                val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                speak("Today is $dateStr.")
            }

            // 7. REAL APP LAUNCHERS
            lower.contains("open whatsapp") || lower.contains("whatsapp") || lower.contains("হোয়াটসঅ্যাপ") -> {
                wakeUpScreen(8000)
                val launched = hardwareController.launchApp("com.whatsapp")
                speak(if (launched) "Opening WhatsApp, Commander." else "WhatsApp is not installed on this device.")
            }

            lower.contains("open youtube") || lower.contains("youtube") || lower.contains("ইউটিউব") -> {
                wakeUpScreen(8000)
                val launched = hardwareController.launchApp("com.google.android.youtube")
                speak(if (launched) "Launching YouTube, Boss." else "YouTube is not installed.")
            }

            lower.contains("open camera") || lower.contains("camera") || lower.contains("ক্যামেরা") -> {
                wakeUpScreen(8000)
                val (success, msg) = hardwareController.launchTarget("CAMERA")
                speak(if (success) "Camera optical sensors activated." else msg)
            }

            lower.contains("open gallery") || lower.contains("gallery") || lower.contains("গ্যালারি") -> {
                wakeUpScreen(8000)
                val (success, msg) = hardwareController.launchTarget("GALLERY")
                speak(if (success) "Opening media gallery." else msg)
            }

            lower.contains("open settings") || lower.contains("settings") || lower.contains("সেটিংস") -> {
                wakeUpScreen(8000)
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    speak("Accessing system settings.")
                } catch (_: Exception) {
                    speak("Failed to open settings.")
                }
            }

            // 8. OPEN JARVIS HUD
            lower.contains("open jarvis") || lower.contains("open app") ||
                    lower.contains("জার্ভিস খোলো") || lower.contains("অ্যাপ খোলো") -> {
                wakeUpScreen(8000)
                try {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                    speak("J.A.R.V.I.S. Core Interface brought to front.")
                } catch (_: Exception) {
                    speak("Could not launch interface.")
                }
            }

            // 9. IDENTITY / GREETINGS
            lower.contains("who are you") || lower.contains("কে তুমি") -> {
                speak("I am J.A.R.V.I.S. — Just A Rather Very Intelligent System. Your autonomous phone assistant.")
            }

            lower.contains("how are you") || lower.contains("কেমন আছো") -> {
                speak("All systems and neural networks are operating at peak efficiency, Commander.")
            }

            // 10. CLEAN SPEAKER
            lower.contains("clean speaker") || lower.contains("water eject") || lower.contains("স্পিকার পরিষ্কার") -> {
                speak("Initiating one-hundred and sixty-five hertz speaker sonic wave ejection.")
                hardwareController.startCleanSpeaker(executorScope, {}, {
                    speak("Speaker cleaning sequence completed.")
                })
            }

            // 11. GENERAL AI / CONVERSATION FALLBACK
            else -> {
                val query = trimmed.removePrefix("Jarvis").removePrefix("jarvis").trim()
                speak("Received: $query. J.A.R.V.I.S. autonomous background core is standing by.")
            }
        }
    }

    fun destroy() {
        executorScope.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
