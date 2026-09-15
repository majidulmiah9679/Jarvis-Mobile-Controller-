package com.example.service

/**
 * Global J.A.R.V.I.S. Application State Tracker.
 * Tracks foreground/background visibility and All-Mobile Live Autonomous mode state
 * across the entire device lifecycle.
 */
object JarvisAppState {
    @Volatile
    var isForeground: Boolean = false

    @Volatile
    var isAllMobileLiveModeActive: Boolean = false
}
