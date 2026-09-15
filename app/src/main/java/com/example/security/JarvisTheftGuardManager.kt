package com.example.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.hardware.SystemHardwareController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * THEFT GUARD (Lite):
 * Accelerometer motion detection + Charger disconnect detection.
 * Loops alarm siren, strobes flashlight, and loops TTS alert.
 */
class JarvisTheftGuardManager(
    private val context: Context,
    private val hardwareController: SystemHardwareController,
    private val scope: CoroutineScope,
    private val onAlarmTriggered: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isGuardActive: Boolean = false
        private set

    var isAlarmRunning: Boolean = false
        private set

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isFirstSensorRead = true

    private var strobeJob: Job? = null
    private var toneJob: Job? = null
    private var ringtone: Ringtone? = null

    private val chargerReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
                if (isGuardActive && !isAlarmRunning) {
                    Log.d("TheftGuard", "Charger disconnected while guard active! Triggering alarm.")
                    triggerAlarm("Charger disconnected")
                }
            }
        }
    }

    fun enableGuard(): Boolean {
        if (isGuardActive) return true
        isGuardActive = true
        isFirstSensorRead = true

        // Register Accelerometer
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Register Charger Disconnect Receiver
        try {
            val filter = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(chargerReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(chargerReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e("TheftGuard", "Error registering charger receiver", e)
        }

        return true
    }

    fun disableGuard() {
        isGuardActive = false
        stopAlarm()

        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}

        try {
            context.unregisterReceiver(chargerReceiver)
        } catch (_: Exception) {}
    }

    fun triggerAlarm(reason: String = "Motion detected") {
        if (isAlarmRunning) return
        isAlarmRunning = true
        onAlarmTriggered()

        // 1. Play loud Ringtone / Alarm siren
        playAlarmSiren()

        // 2. Flashlight strobe loop
        startFlashlightStrobe()

        // 3. Vibrate pattern
        vibrateEmergencyPattern()
    }

    fun stopAlarm() {
        isAlarmRunning = false
        strobeJob?.cancel()
        strobeJob = null

        toneJob?.cancel()
        toneJob = null

        try {
            ringtone?.stop()
        } catch (_: Exception) {}

        // Ensure torch is off
        try {
            hardwareController.setTorch(false)
        } catch (_: Exception) {}
    }

    private fun playAlarmSiren() {
        toneJob = scope.launch(Dispatchers.Default) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(context, alarmUri)?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    }
                    play()
                }
            } catch (_: Exception) {
                // Fallback to ToneGenerator
                try {
                    val tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    while (isActive && isAlarmRunning) {
                        tone.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 1200)
                        delay(1300)
                    }
                    tone.release()
                } catch (_: Exception) {}
            }
        }
    }

    private fun startFlashlightStrobe() {
        strobeJob = scope.launch(Dispatchers.Default) {
            var torchOn = false
            while (isActive && isAlarmRunning) {
                torchOn = !torchOn
                try {
                    hardwareController.setTorch(torchOn)
                } catch (_: Exception) {}
                delay(200)
            }
            try {
                hardwareController.setTorch(false)
            } catch (_: Exception) {}
        }
    }

    private fun vibrateEmergencyPattern() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 600), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400, 200, 600), -1)
            }
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isGuardActive || isAlarmRunning || event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            if (isFirstSensorRead) {
                lastX = x
                lastY = y
                lastZ = z
                isFirstSensorRead = false
                return
            }

            val dx = abs(x - lastX)
            val dy = abs(y - lastY)
            val dz = abs(z - lastZ)

            val delta = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

            // User requirement: "If Phone moved (sensor > 5) OR charger unplugged while guard ON -> Play loud alarm"
            if (delta > 5.0f) {
                Log.d("TheftGuard", "Motion detected: delta=$delta > 5.0f")
                triggerAlarm("Motion detected ($delta)")
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
