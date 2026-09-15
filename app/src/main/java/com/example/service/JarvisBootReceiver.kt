package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Auto-starts J.A.R.V.I.S. All-Mobile Live Autonomous Service on device boot
 * if the user previously engaged All-Mobile Live Mode.
 */
class JarvisBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = context.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)
            val isLiveModeEnabled = prefs.getBoolean("all_mobile_live_mode", false)
            if (isLiveModeEnabled) {
                JarvisForegroundService.startService(context)
            }
        }
    }
}
