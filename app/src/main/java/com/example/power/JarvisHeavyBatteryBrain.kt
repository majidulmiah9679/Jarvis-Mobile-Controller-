package com.example.power

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hardware.BatteryStatusData
import com.example.vectordb.JarvisVectorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Heavy Autonomous Battery Saver Brain.
 * - Records battery telemetry vectors into Vector DB to model discharge patterns.
 * - ML Module Shedding: selectively throttles heavy modules (CameraX vision -> Wake-word rate -> Brightness).
 * - ElevenLabs-style neural phrasing speech synthesis and notification alerts.
 */
class JarvisHeavyBatteryBrain(
    private val context: Context,
    private val vectorEngine: JarvisVectorEngine,
    private val onElevenLabsSpeechRequested: (String) -> Unit,
    private val onModuleSheddingTriggered: (shedLevel: Int, description: String) -> Unit
) {

    private val channelId = "jarvis_battery_brain_channel"
    private var lastCheckedPercentage = -1
    private var lastCheckTime = System.currentTimeMillis()
    private var isPowerSaveActive = false

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "J.A.R.V.I.S. Power Core Management",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Autonomous battery prediction and power-saving triggers"
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    /**
     * Ingest live battery telemetry, update vector model, and trigger predictions.
     */
    fun ingestBatteryTelemetry(battery: BatteryStatusData, scope: CoroutineScope) {
        val now = System.currentTimeMillis()
        val deltaPct = if (lastCheckedPercentage > 0) lastCheckedPercentage - battery.percentage else 0
        val deltaTimeMin = ((now - lastCheckTime) / 60000).coerceAtLeast(1)

        // Estimated burn rate in % per hour
        val burnRatePerHour = if (deltaPct > 0) (deltaPct.toFloat() / deltaTimeMin) * 60f else 4.5f
        val predictedMinutesRemaining = if (burnRatePerHour > 0f) {
            ((battery.percentage.toFloat() / burnRatePerHour) * 60).toInt()
        } else {
            battery.percentage * 15
        }

        // Store vector record of battery telemetry on percentage change or throttled 2-min window
        if (deltaPct != 0 || (now - lastCheckTime >= 120_000L)) {
            val telemetryText = "Battery: ${battery.percentage}%, Charging: ${battery.isCharging}, Temp: ${battery.temperatureCelsius}C, Burn: ${String.format("%.1f", burnRatePerHour)}%/h, Predicted: ${predictedMinutesRemaining}m"
            vectorEngine.insert(
                tag = "BATTERY_TELEMETRY",
                text = telemetryText,
                category = "POWER",
                metadata = mapOf(
                    "percentage" to battery.percentage.toString(),
                    "predictedMinutes" to predictedMinutesRemaining.toString()
                )
            )
            lastCheckTime = now
        }

        lastCheckedPercentage = battery.percentage

        // Check <15% threshold for autonomous mitigation
        if (battery.percentage < 15 && !battery.isCharging) {
            if (!isPowerSaveActive) {
                isPowerSaveActive = true
                executeHeavyPowerMitigation(battery.percentage, predictedMinutesRemaining)
            }
        } else if (battery.percentage >= 20 || battery.isCharging) {
            isPowerSaveActive = false
        }
    }

    private fun executeHeavyPowerMitigation(percentage: Int, minutesLeft: Int) {
        Log.w("HeavyBatteryBrain", "⚡ Critical battery ($percentage%) - Executing heavy mitigation")

        // 1. ElevenLabs-style neural phrasing
        val speech = "Warning Boss. Power Core depleted to $percentage percent. Approximately $minutesLeft minutes of operational autonomy remaining. Engaging Iron Man power conservation protocols."
        onElevenLabsSpeechRequested(speech)

        // 2. ML Module Shedding:
        // Stage 1: Shut down continuous CameraX vision
        // Stage 2: Reduce wake-word sampling rate
        // Stage 3: Reduce display luminance to 20%
        onModuleSheddingTriggered(1, "CAMERA_VISION_OFF")
        onModuleSheddingTriggered(2, "WAKE_WORD_THROTTLED")
        onModuleSheddingTriggered(3, "BRIGHTNESS_TO_20")

        // 3. Dispatch Persistent Notification with Battery Saver Settings PendingIntent
        val settingsIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            888,
            settingsIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
            .setContentTitle("⚡ POWER CORE CRITICAL: $percentage%")
            .setContentText("Heavy modules shed. Estimated $minutesLeft min remaining.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("J.A.R.V.I.S. Autonomous Power Management active:\n- Continuous Vision paused\n- Display luminance reduced to 20%\n- Estimated autonomy: $minutesLeft minutes.\nTap to open system battery settings."))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm?.notify(888, notification)
    }

    fun reset() {
        isPowerSaveActive = false
    }
}
