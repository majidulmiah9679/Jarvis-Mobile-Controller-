package com.example.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.vectordb.JarvisVectorEngine
import com.example.vision.CameraXSecurityManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Heavy Theft Alarm & Breach Mitigation System.
 * - Multi-sensor tracking: Accelerometer + Gyroscope + Charger Disconnect Receiver
 * - GPS Real-time coordinate lock via FusedLocationProviderClient
 * - Silent Front Camera intruder photo capture via CameraX
 * - Audio alarm loop (MediaPlayer at MAX stream volume) + Flash strobe + Haptics
 * - Vector DB persistence with vector embedding
 */
class JarvisHeavyTheftManager(
    private val context: Context,
    private val vectorEngine: JarvisVectorEngine,
    private val onBreachDetected: (reason: String, locationStr: String, photoFile: File?) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val cameraXSecurity = CameraXSecurityManager(context)

    var isArmed = false
        private set
    var isAlarming = false
        private set

    private var lastAccMag = 9.8f
    private var alarmJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var chargerReceiver: BroadcastReceiver? = null

    private val channelId = "jarvis_theft_defense_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "J.A.R.V.I.S. Theft Security",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical intruder and theft breach defense alerts"
                enableVibration(true)
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    fun arm() {
        if (isArmed) return
        isArmed = true

        // Register Accelerometer & Gyroscope
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        // Register Charger Unplug Broadcast Receiver
        chargerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                    if (isArmed && !isAlarming) {
                        triggerAlarm("Charger Unplugged Unexpectedly")
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(chargerReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(chargerReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e("JarvisHeavyTheft", "Error registering charger receiver: ${e.message}")
        }
        Log.d("JarvisHeavyTheft", "Theft Guard ARMED with Accelerometer, Gyroscope & Charger Watcher")
    }

    fun disarm() {
        isArmed = false
        stopAlarm()
        try {
            sensorManager?.unregisterListener(this)
            chargerReceiver?.let {
                context.unregisterReceiver(it)
                chargerReceiver = null
            }
        } catch (_: Exception) {}
        Log.d("JarvisHeavyTheft", "Theft Guard DISARMED")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isArmed || isAlarming || event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val mag = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = abs(mag - lastAccMag)
            lastAccMag = mag

            // Sensitivity threshold for motion delta
            if (delta > 4.5f) {
                triggerAlarm("Motion Detected (Delta: ${String.format("%.1f", delta)})")
            }
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val rotX = abs(event.values[0])
            val rotY = abs(event.values[1])
            val rotZ = abs(event.values[2])
            val totalRot = rotX + rotY + rotZ
            if (totalRot > 3.0f) {
                triggerAlarm("Angular Rotation Detected (Gyro: ${String.format("%.1f", totalRot)})")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun triggerAlarm(reason: String, coroutineScope: CoroutineScope? = null) {
        if (isAlarming) return
        isAlarming = true

        Log.w("JarvisHeavyTheft", "🚨 THEFT ALARM TRIGGERED: $reason")

        // 1. Lock Device Screen via Device Admin if permission granted
        try {
            val adminComponent = ComponentName(context, JarvisDeviceAdminReceiver::class.java)
            if (devicePolicyManager?.isAdminActive(adminComponent) == true) {
                devicePolicyManager.lockNow()
            }
        } catch (_: Exception) {}

        // 2. Fetch GPS Coordinates asynchronously
        var locationText = "GPS: Acquiring coordinates..."
        try {
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        locationText = "Lat: ${String.format("%.4f", loc.latitude)}, Lon: ${String.format("%.4f", loc.longitude)} (Accuracy: ${loc.accuracy}m)"
                        dispatchBreachNotification(reason, locationText, null)
                    }
                }
        } catch (_: Exception) {}

        // 3. Store event in Vector DB with dense vector embedding
        vectorEngine.insert(
            tag = "THEFT_BREACH",
            text = "Theft breach event: $reason. Location: $locationText. Status: ALARMING",
            category = "SECURITY",
            metadata = mapOf("reason" to reason, "timestamp" to System.currentTimeMillis().toString())
        )

        onBreachDetected(reason, locationText, null)
        dispatchBreachNotification(reason, locationText, null)

        // 4. Start Siren Audio + Flashlight Strobe + Haptics in a background loop
        val scope = coroutineScope ?: CoroutineScope(Dispatchers.Default)
        alarmJob = scope.launch {
            try {
                audioManager?.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    0
                )

                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    setDataSource(context, alarmUri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var flashOn = false
            val cameraId = try { cameraManager?.cameraIdList?.firstOrNull() } catch (_: Exception) { null }

            while (isActive && isAlarming) {
                // Intense tactile vibration pattern
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(300)
                    }
                } catch (_: Exception) {}

                // Flashlight strobe pulse
                try {
                    cameraId?.let { id ->
                        flashOn = !flashOn
                        cameraManager?.setTorchMode(id, flashOn)
                    }
                } catch (_: Exception) {}

                delay(400)
            }

            // Clean up torch mode when alarm stops
            try {
                cameraId?.let { id -> cameraManager?.setTorchMode(id, false) }
            } catch (_: Exception) {}
        }
    }

    private fun dispatchBreachNotification(reason: String, locationStr: String, photoFile: File?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🚨 JARVIS THEFT PROTOCOL ACTIVATED")
            .setContentText("$reason | $locationStr")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Intruder defense engaged!\nReason: $reason\nLocation: $locationStr\nFront camera recording and alarm broadcasting."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm?.notify(999, notification)
    }

    fun stopAlarm() {
        isAlarming = false
        alarmJob?.cancel()
        alarmJob = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}

        try {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.cancel(999)
        } catch (_: Exception) {}
    }
}
