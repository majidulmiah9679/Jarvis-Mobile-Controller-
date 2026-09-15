package com.example

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.HashSet

class JarvisAutomationService : AccessibilityService() {

    val automator: com.example.automation.EnhancedAccessibilityAutomator by lazy {
        com.example.automation.EnhancedAccessibilityAutomator { this }
    }

    companion object {
        @Volatile
        private var instance: JarvisAutomationService? = null

        @JvmStatic
        fun getInstance(): JarvisAutomationService? {
            return instance
        }

        fun getAutomator(): com.example.automation.EnhancedAccessibilityAutomator? {
            return instance?.automator
        }

        fun isServiceRunning(): Boolean {
            return instance != null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance == this) {
            instance = null
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName == null) return
        val currentPackage = event.packageName.toString()

        try {
            // [HEAVY APP LOCKER] Intercept opening of protected applications with real overlay
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (currentPackage != packageName && !com.example.security.JarvisAppLockOverlayActivity.isUnlocked(currentPackage)) {
                    val encHelper = com.example.security.EncryptedPrefsHelper(this)
                    val lockedApps = encHelper.getLockedApps()
                    if (lockedApps.contains(currentPackage)) {
                        val overlayIntent = android.content.Intent(this, com.example.security.JarvisAppLockOverlayActivity::class.java).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("TARGET_PACKAGE", currentPackage)
                        }
                        startActivity(overlayIntent)
                    }
                }
            }

            val prefs: SharedPreferences = getSharedPreferences("JarvisSettings", Context.MODE_PRIVATE)
            val selectedApps: Set<String> = prefs.getStringSet("selected_apps", HashSet<String>()) ?: HashSet<String>()

            // Inspect selected apps
            if (selectedApps.contains(currentPackage)) {
                val rootNode: AccessibilityNodeInfo? = try {
                    rootInActiveWindow
                } catch (_: Exception) {
                    null
                }
                rootNode?.recycle()
            }
        } catch (e: Exception) {
            // Safely guard against window transition and lifecycle anomalies
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        // Handled cleanly when user disables service in settings
    }

    // স্ক্রিনের যেকোনো কোঅর্ডিনেটে অটো ট্যাপ করার মেথড
    fun autoClick(x: Float, y: Float): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val clickPath = Path()
                clickPath.moveTo(x, y)
                val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, 50)
                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(clickStroke)
                dispatchGesture(gestureBuilder.build(), null, null)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // স্ক্রল ডাউন অটোমেশন মেথড
    fun autoScrollDown(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val scrollPath = Path()
                scrollPath.moveTo(500f, 1500f)
                scrollPath.lineTo(500f, 500f)
                val scrollStroke = GestureDescription.StrokeDescription(scrollPath, 0, 300)
                val gestureBuilder = GestureDescription.Builder()
                gestureBuilder.addStroke(scrollStroke)
                dispatchGesture(gestureBuilder.build(), null, null)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // নোড টেক্সট বা আইডি দিয়ে ক্লিক
    fun clickByText(text: String): Int {
        var clickedCount = 0
        try {
            val rootNode: AccessibilityNodeInfo? = try {
                rootInActiveWindow
            } catch (_: Exception) {
                null
            }
            if (rootNode != null) {
                val nodes = rootNode.findAccessibilityNodeInfosByText(text)
                if (nodes != null) {
                    for (node in nodes) {
                        try {
                            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                clickedCount++
                            }
                        } catch (_: Exception) {}
                    }
                }
                try {
                    rootNode.recycle()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            // Guarded
        }
        return clickedCount
    }

    // Extended UI Automation Helpers
    fun swipeHorizontal(fromLeftToRight: Boolean): Boolean {
        return if (fromLeftToRight) {
            automator.performSwipe(150f, 1000f, 900f, 1000f, 300L)
        } else {
            automator.performSwipe(900f, 1000f, 150f, 1000f, 300L)
        }
    }

    fun inputTextToField(targetQuery: String, text: String): Boolean {
        return automator.findAndSetText(targetQuery, text)
    }

    fun pressBack(): Boolean = automator.performGlobalBack()
    fun pressHome(): Boolean = automator.performGlobalHome()
    fun pressRecents(): Boolean = automator.performGlobalRecents()
}
