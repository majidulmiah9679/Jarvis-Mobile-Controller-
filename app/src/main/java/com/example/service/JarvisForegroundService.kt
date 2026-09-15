package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Autonomous Foreground Service Worker for J.A.R.V.I.S.
 * Keeps offline wake-word listener active, executes background agent workflows,
 * keeps CPU awake with PARTIAL_WAKE_LOCK during screen-off, and maintains continuous
 * 24/7 system awareness even when the app is closed or killed.
 */
class JarvisForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var wakeWordDetector: JarvisWakeWordDetector? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var backgroundAgent: JarvisBackgroundAgentExecutor? = null

    companion object {
        const val CHANNEL_ID = "jarvis_autonomous_service_channel"
        const val NOTIFICATION_ID = 4001
        const val ACTION_START = "ACTION_START_AUTONOMOUS_AGENT"
        const val ACTION_STOP = "ACTION_STOP_AUTONOMOUS_AGENT"
        const val ACTION_TRIGGER_VOICE = "ACTION_TRIGGER_VOICE"

        @Volatile
        private var isRunning = false
        fun isServiceActive(): Boolean = isRunning

        private val _wakeWordEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
        val wakeWordEvents: SharedFlow<String> = _wakeWordEvents.asSharedFlow()

        fun startService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun triggerVoiceFromNotification(context: Context) {
            val intent = Intent(context, JarvisForegroundService::class.java).apply {
                action = ACTION_TRIGGER_VOICE
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireCpuWakeLock()
        promoteToForegroundSafely("J.A.R.V.I.S. All-Mobile Live Autonomous Engine Online")

        backgroundAgent = JarvisBackgroundAgentExecutor(this) { actionLog ->
            updateNotification("J.A.R.V.I.S. // $actionLog")
        }

        wakeWordDetector = JarvisWakeWordDetector(this) { detectedWord ->
            _wakeWordEvents.tryEmit(detectedWord)
            if (!JarvisAppState.isForeground) {
                backgroundAgent?.onWakeWordTriggered(detectedWord)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopWakeWordListener()
                isRunning = false
                JarvisAppState.isAllMobileLiveModeActive = false
                releaseCpuWakeLock()
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (_: Exception) {}
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TRIGGER_VOICE -> {
                backgroundAgent?.wakeUpScreen(8000)
                backgroundAgent?.speak("J.A.R.V.I.S. online. Listening...")
                backgroundAgent?.startListeningForCommand()
                return START_STICKY
            }
            else -> {
                promoteToForegroundSafely("J.A.R.V.I.S. All-Mobile Live Autonomous Engine Online")
                isRunning = true
                JarvisAppState.isAllMobileLiveModeActive = true
                acquireCpuWakeLock()
                wakeWordDetector?.startListening(serviceScope)
                return START_STICKY
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Keep J.A.R.V.I.S. running 24/7 even when user closes / swipes app from recent apps
        if (isRunning) {
            try {
                val restartIntent = Intent(applicationContext, JarvisForegroundService::class.java).apply {
                    action = ACTION_START
                }
                val pendingIntent = PendingIntent.getService(
                    applicationContext,
                    1001,
                    restartIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                alarmManager?.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000,
                    pendingIntent
                )
            } catch (_: Exception) {}
        }
    }

    private fun acquireCpuWakeLock() {
        if (wakeLock == null) {
            try {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "JARVIS:AutonomousLiveCoreWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
            } catch (_: Exception) {}
        }
    }

    private fun releaseCpuWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun promoteToForegroundSafely(message: String) {
        val notification = buildForegroundNotification(message)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var typeFlags = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    typeFlags = typeFlags or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                }
                val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (hasMic) {
                    typeFlags = typeFlags or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
                startForeground(NOTIFICATION_ID, notification, typeFlags)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (_: Exception) {}
        } catch (_: Exception) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
    }

    private fun updateNotification(message: String) {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, buildForegroundNotification(message))
        } catch (_: Exception) {}
    }

    private fun stopWakeWordListener() {
        wakeWordDetector?.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        JarvisAppState.isAllMobileLiveModeActive = false
        stopWakeWordListener()
        releaseCpuWakeLock()
        backgroundAgent?.destroy()
        backgroundAgent = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "JARVIS All-Mobile Autonomous Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps JARVIS autonomous phone automator & offline wake-word active across all apps & screen-off"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val voiceIntent = Intent(this, JarvisForegroundService::class.java).apply {
            action = ACTION_TRIGGER_VOICE
        }
        val voicePendingIntent = PendingIntent.getService(
            this,
            2,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("J.A.R.V.I.S. // All-Mobile Live")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_btn_speak_now, "Voice Command", voicePendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open HUD", pendingIntent)
            .build()
    }
}
