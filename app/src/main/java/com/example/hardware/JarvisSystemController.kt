package com.example.hardware

import android.app.Activity
import android.content.Context

/**
 * JARVIS System Controller.
 * Comprehensive hardware and system toggles:
 * - Flashlight (CameraManager)
 * - Volume (AudioManager)
 * - HUD / System Brightness
 * - Hotspot & Wi-Fi Settings
 * - DND & Ringer Modes (Normal, Vibrate, Silent)
 */
class JarvisSystemController(private val context: Context) {

    private val hardwareController = SystemHardwareController(context)

    // Flashlight beam
    fun setTorch(enabled: Boolean): Boolean = hardwareController.setTorch(enabled)

    // Volume controls (0 - 100%)
    fun setMediaVolumePercent(percent: Int): Int = hardwareController.setMediaVolumePercent(percent)
    fun getMediaVolumePercent(): Int = hardwareController.getMediaVolumePercent()
    fun maxVolume(): Int = hardwareController.maxVolume()
    fun muteMedia(): Int = hardwareController.muteMedia()

    // Brightness
    fun setScreenBrightness(activity: Activity?, percent: Int): Boolean =
        hardwareController.setScreenBrightness(activity, percent)

    // Ringer modes & DND
    fun setRingerMode(mode: SystemHardwareController.RingerMode): Boolean =
        hardwareController.setRingerMode(mode)

    fun toggleSilent(silent: Boolean): Boolean {
        return if (silent) {
            hardwareController.setRingerMode(SystemHardwareController.RingerMode.SILENT)
        } else {
            hardwareController.setRingerMode(SystemHardwareController.RingerMode.NORMAL)
        }
    }

    // Connectivity
    fun openWifiSettings() = hardwareController.openWifiSettings()
    fun openHotspotSettings() = hardwareController.openHotspotSettings()
    fun openBluetoothSettings() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }
}
