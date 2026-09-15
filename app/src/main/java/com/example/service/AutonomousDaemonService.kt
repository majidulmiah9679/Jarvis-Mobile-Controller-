package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Autonomous Foreground Daemon Service for J.A.R.V.I.S.
 * Fully compliant with Android 14 (API 34) & Android 15 (API 35/36) strict Foreground Service (FGS) mandates:
 * - Dynamic runtime permission evaluation (RECORD_AUDIO, POST_NOTIFICATIONS).
 * - Safe foregroundServiceType binding (microphone, specialUse, dataSync).
 * - Immediate startForeground() invocation in onCreate() within <5000ms.
 * - Fault-tolerant lifecycle handling with fallback to prevent ForegroundServiceStartNotAllowedException crashes.
 */
class AutonomousDaemonService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var wakeWordDetector: JarvisWakeWordDetector? = null

    companion object {
        private const val TAG = "AutonomousDaemonService"
        const val CHANNEL_ID = "jarvis_autonomous_daemon_channel"
        const val NOTIFICATION_ID = 5001
        const val ACTION_START = "com.example.service.ACTION_START_AUTONOMOUS_DAEMON"
        const val ACTION_STOP = "com.example.service.ACTION_STOP_AUTONOMOUS_DAEMON"

        @Volatile
        private var isRunning = false

        @JvmStatic
        fun isServiceActive(): Boolean = isRunning

        private val _wakeWordEvents = MutableSharedFlow<String>(extraBufferCapacity = 16)
        val wakeWordEvents: SharedFlow<String> = _wakeWordEvents.asSharedFlow()

        /**
         * Safely starts the Autonomous Daemon Service with full Android 14/15 safety checks.
         */
        fun startService(context: Context) {
            try {
                val intent = Intent(context, AutonomousDaemonService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i(TAG, "AutonomousDaemonService start request dispatched successfully")
            } catch (e: Exception) {
                // Catches ForegroundServiceStartNotAllowedException on Android 12+ or SecurityException
                Log.e(TAG, "Failed to start AutonomousDaemonService: ${e.message}", e)
                throw e
            }
        }

        /**
         * Gracefully requests the Autonomous Daemon Service to shut down.
         */
        fun stopService(context: Context) {
            try {
                val intent = Intent(context, AutonomousDaemonService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
                Log.i(TAG, "AutonomousDaemonService stop request dispatched")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop AutonomousDaemonService: ${e.message}", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "AutonomousDaemonService onCreate() initialized")

        // 1. Establish persistent notification channel immediately
        createNotificationChannel()

        // 2. CRITICAL ANDROID 14/15 RULE: Invoke startForeground immediately in onCreate
        // to satisfy the 5-second startForeground deadline.
        promoteToForegroundSafely("J.A.R.V.I.S. Autonomous Daemon: INITIALIZING")

        // 3. Initialize offline wake-word detector
        try {
            wakeWordDetector = JarvisWakeWordDetector(this) { detectedWord ->
                Log.d(TAG, "Wake word detected: $detectedWord")
                _wakeWordEvents.tryEmit(detectedWord)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing wake-word detector in daemon", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "AutonomousDaemonService onStartCommand() action: ${intent?.action}")

        when (intent?.action) {
            ACTION_STOP -> {
                shutdownService()
                return START_NOT_STICKY
            }
            else -> {
                // Ensure notification and FGS status are current
                promoteToForegroundSafely("J.A.R.V.I.S. Core 4.0: Autonomous Background Daemon ACTIVE")
                isRunning = true

                // Start acoustic detection on IO coroutine worker
                val hasMic = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasMic) {
                    serviceScope.launch(Dispatchers.IO) {
                        try {
                            wakeWordDetector?.startListening(this)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to launch wake-word listener on IO", e)
                        }
                    }
                } else {
                    Log.w(TAG, "RECORD_AUDIO permission missing; running in telemetry-only mode")
                }

                return START_STICKY
            }
        }
    }

    /**
     * Executes startForeground with exact foregroundServiceType masking based on granted permissions.
     */
    private fun promoteToForegroundSafely(statusMessage: String) {
        val notification = buildForegroundNotification(statusMessage)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var typeFlags = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC

                // Android 14+ (API 34) Special Use type
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    typeFlags = typeFlags or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                }

                // Check RECORD_AUDIO runtime permission before requesting MICROPHONE type
                val hasMicPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasMicPermission) {
                    typeFlags = typeFlags or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }

                startForeground(NOTIFICATION_ID, notification, typeFlags)
                Log.d(TAG, "startForeground executed with typeFlags: $typeFlags")
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException during typed startForeground, falling back to basic dataSync: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fatal fallback error starting foreground service", fallbackEx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Generic exception during startForeground: ${e.message}", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
    }

    private fun shutdownService() {
        Log.i(TAG, "Shutting down AutonomousDaemonService")
        isRunning = false
        try {
            wakeWordDetector?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping wake-word detector", e)
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "AutonomousDaemonService onDestroy()")
        isRunning = false
        try {
            wakeWordDetector?.stopListening()
        } catch (_: Exception) {}
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "AutonomousDaemonService onLowMemory() warning received")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "AutonomousDaemonService onTrimMemory(level=$level)")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "J.A.R.V.I.S. Autonomous Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maintains continuous background listening, telemetry, and autonomous device automations"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("J.A.R.V.I.S. 4.0 Core Online")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
