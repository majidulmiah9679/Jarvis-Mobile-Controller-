package com.example.service

import android.content.Context
import kotlinx.coroutines.flow.SharedFlow

/**
 * JARVIS Background Daemon.
 * Coordinates offline wake-word detection ("Hey Jarvis") and the continuous
 * foreground autonomous service for background tasks, automation, and live listening.
 */
class JarvisBackgroundDaemon(private val context: Context) {

    val wakeWordEvents: SharedFlow<String> = JarvisForegroundService.wakeWordEvents

    fun isRunning(): Boolean = JarvisForegroundService.isServiceActive()

    fun start() {
        JarvisForegroundService.startService(context)
    }

    fun stop() {
        JarvisForegroundService.stopService(context)
    }
}
