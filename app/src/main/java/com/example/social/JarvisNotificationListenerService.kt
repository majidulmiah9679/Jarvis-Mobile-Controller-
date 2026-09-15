package com.example.social

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.RemoteInput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class IncomingNotificationItem(
    val packageName: String,
    val sender: String,
    val text: String,
    val postTime: Long = System.currentTimeMillis(),
    val sbnKey: String,
    val appName: String = ""
)

/**
 * Notification Listener Service for J.A.R.V.I.S.
 * Intercepts incoming notifications (WhatsApp, Messenger, SMS),
 * enables voice announcement, and facilitates autonomous auto-replies.
 */
class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        private var instance: JarvisNotificationListenerService? = null

        private val _notificationEvents = MutableSharedFlow<IncomingNotificationItem>(extraBufferCapacity = 64)
        val notificationEvents: SharedFlow<IncomingNotificationItem> = _notificationEvents.asSharedFlow()

        fun getInstance(): JarvisNotificationListenerService? = instance
        fun isServiceRunning(): Boolean = instance != null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.packageName == packageName) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Resolve user-friendly application name (WhatsApp, Messenger, bKash, etc.)
        val resolvedAppName = try {
            val pm = packageManager
            val ai = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            when {
                sbn.packageName.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
                sbn.packageName.contains("orca", ignoreCase = true) -> "Messenger"
                sbn.packageName.contains("bkash", ignoreCase = true) -> "bKash"
                sbn.packageName.contains("telegram", ignoreCase = true) -> "Telegram"
                else -> sbn.packageName.substringAfterLast('.')
            }
        }

        // Brief WakeLock so device stays active if locked
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Jarvis:NotificationWake")
            wakeLock?.acquire(5000)
        } catch (_: Exception) {}

        val item = IncomingNotificationItem(
            packageName = sbn.packageName,
            sender = title,
            text = text,
            postTime = sbn.postTime,
            sbnKey = sbn.key,
            appName = resolvedAppName
        )
        _notificationEvents.tryEmit(item)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Notification dismissed
    }

    /**
     * Attempts to send an automatic reply through Notification RemoteInput actions
     * (supported by WhatsApp, Telegram, Google Messages, Signal).
     */
    fun sendDirectReply(sbnKey: String, replyText: String): Boolean {
        val activeSbns = activeNotifications ?: return false
        val target = activeSbns.find { it.key == sbnKey } ?: return false
        val notification = target.notification

        val actions = notification.actions ?: return false
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.allowFreeFormInput) {
                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putCharSequence(remoteInput.resultKey, replyText)
                    android.app.RemoteInput.addResultsToIntent(
                        arrayOf(android.app.RemoteInput.Builder(remoteInput.resultKey).build()),
                        intent,
                        bundle
                    )
                    try {
                        action.actionIntent.send(this, 0, intent)
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }
            }
        }
        return false
    }
}
