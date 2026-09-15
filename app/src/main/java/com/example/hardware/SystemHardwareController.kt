package com.example.hardware

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.bluetooth.BluetoothAdapter
import android.net.wifi.WifiManager
import android.os.PowerManager
import com.example.JarvisAutomationService
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class BatteryStatusData(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val statusText: String = "Normal",
    val temperatureCelsius: Float = 25.0f,
    val voltageMv: Int = 4000
)

/**
 * Hardware Controller for J.A.R.V.I.S.
 * Upgraded to Full Mobile Control with Real Android APIs:
 * - Flashlight Beam (CameraManager torch mode)
 * - Audio Output Level (AudioManager stream music + slider & haptics)
 * - Ringer & DND Mode (Normal, Vibrate, Silent)
 * - Display Luminance & Screen Keep Awake (WindowManager & Flags)
 * - Real-time Battery Status (BatteryManager)
 * - Clean Speaker 165Hz (AudioTrack 165Hz sine wave oscillator for water ejection)
 * - Vibration Test & Diagnostics
 * - Copy Device Info Telemetry (RAM, Battery, Network, OS, Model)
 * - Real App Launchers (Camera, Gallery, Browser, Dialer, Files)
 */
class SystemHardwareController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    private var torchCameraId: String? = null

    init {
        findTorchCameraId()
    }

    private fun findTorchCameraId() {
        try {
            val cm = cameraManager ?: return
            for (id in cm.cameraIdList) {
                val characteristics = cm.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    torchCameraId = id
                    return
                }
            }
            for (id in cm.cameraIdList) {
                val characteristics = cm.getCameraCharacteristics(id)
                if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                    torchCameraId = id
                    return
                }
            }
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 1. FLASHLIGHT BEAM (Real CameraManager Torch)
    // -------------------------------------------------------------
    fun hasTorchSupport(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun setTorch(enabled: Boolean): Boolean {
        if (!hasTorchSupport()) {
            return false
        }
        val cm = cameraManager ?: return false
        val id = torchCameraId ?: cm.cameraIdList.firstOrNull() ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cm.setTorchMode(id, enabled)
                vibrate(30)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // 2. AUDIO OUTPUT LEVEL & HAPTIC FEEDBACK
    // -------------------------------------------------------------
    fun setMediaVolumePercent(percent: Int): Int {
        val am = audioManager ?: return 0
        val clamped = percent.coerceIn(0, 100)
        val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (maxVolume * (clamped / 100f)).toInt()
        am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
        vibrate(25)
        return getMediaVolumePercent()
    }

    fun getMediaVolumePercent(): Int {
        val am = audioManager ?: return 0
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        return ((current.toFloat() / max) * 100).toInt()
    }

    fun maxVolume(): Int = setMediaVolumePercent(100)
    fun muteMedia(): Int = setMediaVolumePercent(0)

    // -------------------------------------------------------------
    // VIBRATION & HAPTICS
    // -------------------------------------------------------------
    fun vibrate(durationMs: Long = 100) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun testVibrationPattern() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 150, 100, 200, 100, 450)
                val amplitudes = intArrayOf(0, 255, 0, 200, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 200, 100, 450), -1)
            }
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 3. RINGER & DND MODES
    // -------------------------------------------------------------
    enum class RingerMode { NORMAL, VIBRATE, SILENT, DND }

    fun getCurrentRingerMode(): RingerMode {
        val am = audioManager ?: return RingerMode.NORMAL
        return when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> RingerMode.NORMAL
        }
    }

    fun setRingerMode(mode: RingerMode): Boolean {
        val am = audioManager ?: return false
        val nm = notificationManager ?: return false

        return try {
            when (mode) {
                RingerMode.NORMAL -> {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nm.isNotificationPolicyAccessGranted) {
                        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                    }
                    vibrate(40)
                    true
                }
                RingerMode.VIBRATE -> {
                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    vibrate(200) // 200ms per requirement
                    true
                }
                RingerMode.SILENT -> {
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    vibrate(30)
                    true
                }
                RingerMode.DND -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (nm.isNotificationPolicyAccessGranted) {
                            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                            true
                        } else {
                            openDndSettings()
                            false
                        }
                    } else {
                        am.ringerMode = AudioManager.RINGER_MODE_SILENT
                        true
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // 4. DISPLAY LUMINANCE & SCREEN KEEP AWAKE
    // -------------------------------------------------------------
    fun setScreenBrightness(activity: Activity?, percent: Int): Boolean {
        val clamped = percent.coerceIn(0, 100)
        if (activity != null) {
            try {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = (clamped / 100f).coerceIn(0.01f, 1.0f)
                activity.window.attributes = layoutParams
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    fun setBrightness(percent: Int): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(context)) {
                val value = ((percent.coerceIn(0, 100) / 100f) * 255).toInt()
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
                true
            } else {
                openDisplaySettings()
                true
            }
        } catch (_: Exception) {
            openDisplaySettings()
            true
        }
    }

    fun openDisplaySettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun setScreenKeepAwake(activity: Activity?, keepAwake: Boolean) {
        activity?.runOnUiThread {
            try {
                if (keepAwake) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            } catch (_: Exception) {}
        }
    }

    // -------------------------------------------------------------
    // 5. BATTERY STATUS (Real Android BatteryManager)
    // -------------------------------------------------------------
    fun getBatteryStatus(): BatteryStatusData {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)
            if (intent != null) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val pluggedText = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Charging"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charging"
                    else -> if (isCharging) "Charging" else "Discharging"
                }
                val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)
                BatteryStatusData(
                    percentage = pct,
                    isCharging = isCharging,
                    statusText = pluggedText,
                    temperatureCelsius = tempTenths / 10.0f,
                    voltageMv = voltage
                )
            } else {
                BatteryStatusData()
            }
        } catch (e: Exception) {
            BatteryStatusData()
        }
    }

    // -------------------------------------------------------------
    // 6. CLEAN SPEAKER 165Hz (AudioTrack 165Hz Sine Wave for 30s)
    // -------------------------------------------------------------
    private var cleanSpeakerJob: Job? = null
    private var cleanAudioTrack: AudioTrack? = null

    fun isSpeakerCleaningActive(): Boolean = cleanSpeakerJob?.isActive == true

    fun startCleanSpeaker(
        scope: CoroutineScope,
        onTick: (secondsLeft: Int) -> Unit,
        onComplete: () -> Unit
    ) {
        stopCleanSpeaker()

        cleanSpeakerJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val frequency = 165.0
            val numSamples = sampleRate
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                buffer[i] = (sin(angle) * (Short.MAX_VALUE * 0.95)).toInt().toShort()
            }

            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                cleanAudioTrack = audioTrack
                audioTrack.play()

                val totalDurationSeconds = 30
                var secondsRemaining = totalDurationSeconds

                val playJob = launch {
                    while (isActive) {
                        audioTrack.write(buffer, 0, buffer.size)
                    }
                }

                while (isActive && secondsRemaining > 0) {
                    launch(Dispatchers.Main) {
                        onTick(secondsRemaining)
                    }
                    delay(1000)
                    secondsRemaining--
                }

                playJob.cancel()
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
                cleanAudioTrack = null

                launch(Dispatchers.Main) {
                    onTick(0)
                    onComplete()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    fun stopCleanSpeaker() {
        try {
            cleanSpeakerJob?.cancel()
            cleanSpeakerJob = null
            cleanAudioTrack?.stop()
            cleanAudioTrack?.release()
            cleanAudioTrack = null
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 7. COPY DEVICE INFO TELEMETRY
    // -------------------------------------------------------------
    fun getFormattedDeviceInfo(): String {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024 * 1024 * 1024.0)
        val freeRamGb = memInfo.availMem / (1024 * 1024 * 1024.0)

        val battery = getBatteryStatus()

        val netCaps = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        val networkType = when {
            netCaps == null -> "OFFLINE"
            netCaps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WI-FI (SECURED)"
            netCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR (5G/LTE)"
            else -> "ACTIVE NETWORK"
        }

        return """
            === J.A.R.V.I.S. DEVICE TELEMETRY ===
            DEVICE: ${Build.MANUFACTURER.uppercase()} ${Build.MODEL}
            OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            RAM: ${String.format("%.2f", freeRamGb)} GB free / ${String.format("%.2f", totalRamGb)} GB total
            BATTERY: ${battery.percentage}% [${battery.statusText}] (${battery.temperatureCelsius}°C)
            NETWORK: $networkType
            HUD SECURITY: ENCRYPTED // ONLINE
        """.trimIndent()
    }

    fun copyDeviceInfoToClipboard(): Boolean {
        return try {
            val text = getFormattedDeviceInfo()
            val clip = ClipData.newPlainText("JARVIS Device Telemetry", text)
            clipboardManager?.setPrimaryClip(clip)
            vibrate(50)
            true
        } catch (_: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // 8. REAL APP LAUNCHERS
    // -------------------------------------------------------------
    fun launchTarget(target: String): Pair<Boolean, String> {
        return try {
            vibrate(30)
            when (target.uppercase()) {
                "CAMERA" -> {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        Pair(true, "Camera opened")
                    } else {
                        val fallback = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallback)
                        Pair(true, "Camera opened")
                    }
                }
                "GALLERY" -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        type = "image/*"
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        Pair(true, "Gallery opened")
                    } else {
                        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(pickIntent)
                        Pair(true, "Gallery opened")
                    }
                }
                "BROWSER" -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Pair(true, "Browser opened")
                }
                "DIALER" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Pair(true, "Dialer opened")
                }
                "FILES" -> {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Pair(true, "File Manager opened")
                }
                else -> Pair(false, "Unknown target $target")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Failed to open $target")
        }
    }

    fun launchApp(appName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(appName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                launchTarget(appName).first
            }
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // SETTINGS INTENTS
    // -------------------------------------------------------------
    fun openWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openDndSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openWifiSettings() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openHotspotSettings() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.android.settings", "com.android.settings.TetherSettings")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    // -------------------------------------------------------------
    // 9. WIRELESS & CONNECTIVITY HARDWARE CONTROL
    // -------------------------------------------------------------
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            BluetoothAdapter.getDefaultAdapter()
        } catch (_: Exception) {
            null
        }
    }
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun isWifiActive(): Boolean {
        return wifiManager?.isWifiEnabled == true
    }

    @Suppress("DEPRECATION")
    fun toggleWifi(enable: Boolean? = null): Pair<Boolean, String> {
        val wm = wifiManager ?: return Pair(false, "Wi-Fi hardware unavailable")
        val target = enable ?: !wm.isWifiEnabled
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wm.isWifiEnabled = target
                vibrate(40)
                Pair(true, if (target) "Wi-Fi activated" else "Wi-Fi deactivated")
            } else {
                // Android 10+ requires user panel or settings
                try {
                    val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(panelIntent)
                    vibrate(40)
                    Pair(true, "Opened Wi-Fi connectivity panel")
                } catch (_: Exception) {
                    openWifiSettings()
                    vibrate(40)
                    Pair(true, "Opened Wi-Fi Settings")
                }
            }
        } catch (e: Exception) {
            openWifiSettings()
            Pair(true, "Opened Wi-Fi Settings")
        }
    }

    fun isBluetoothActive(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (_: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    fun toggleBluetooth(enable: Boolean? = null): Pair<Boolean, String> {
        val adapter = bluetoothAdapter ?: return Pair(false, "Bluetooth hardware not detected")
        return try {
            val target = enable ?: !adapter.isEnabled
            if (target) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    adapter.enable()
                    vibrate(40)
                    Pair(true, "Bluetooth enabled")
                } else {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    vibrate(40)
                    Pair(true, "Requested Bluetooth enable")
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    adapter.disable()
                    vibrate(40)
                    Pair(true, "Bluetooth disabled")
                } else {
                    openBluetoothSettings()
                    vibrate(40)
                    Pair(true, "Opened Bluetooth Settings")
                }
            }
        } catch (e: Exception) {
            openBluetoothSettings()
            Pair(true, "Opened Bluetooth Settings")
        }
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openMobileDataSettings() {
        try {
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            vibrate(40)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (_: Exception) {
                openWirelessSettings()
            }
        }
    }

    fun openWirelessSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            vibrate(40)
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 10. DISPLAY SLEEP, WAKE & NOTIFICATION CONTROLS
    // -------------------------------------------------------------
    fun turnScreenOff(): Boolean {
        vibrate(50)
        val service = JarvisAutomationService.getInstance()
        return if (service != null) {
            service.automator.performGlobalLockScreen()
        } else {
            false
        }
    }

    @Suppress("DEPRECATION")
    fun turnScreenOn(): Boolean {
        return try {
            val pm = powerManager ?: return false
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "jarvis:screenturnon"
            )
            wakeLock.acquire(3000)
            vibrate(50)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openNotificationDrawer(): Boolean {
        vibrate(30)
        val service = JarvisAutomationService.getInstance()
        return service?.automator?.performGlobalNotifications() ?: false
    }

    fun openQuickSettings(): Boolean {
        vibrate(30)
        val service = JarvisAutomationService.getInstance()
        return service?.automator?.performGlobalQuickSettings() ?: false
    }

    fun takeScreenshot(): Boolean {
        vibrate(40)
        val service = JarvisAutomationService.getInstance()
        return service?.automator?.performGlobalTakeScreenshot() ?: false
    }
}
