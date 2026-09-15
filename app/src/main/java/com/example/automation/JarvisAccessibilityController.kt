package com.example.automation

import android.content.Context
import com.example.JarvisAutomationService
import com.example.duplex.DuplexAudioEngine
import kotlinx.coroutines.CoroutineScope

/**
 * JARVIS Full Accessibility Controller.
 * Provides complete phone control, gesture dispatching, UI tree indexing,
 * automated clicking/scrolling, and connects to acoustic duplex audio
 * with real-time barge-in interruption.
 */
class JarvisAccessibilityController(
    private val context: Context,
    private val apiKeyProvider: () -> String,
    private val onInterruptPlayback: () -> Unit
) {
    val automator = EnhancedAccessibilityAutomator { JarvisAutomationService.getInstance() }
    val duplexEngine = DuplexAudioEngine(
        context = context,
        apiKeyProvider = apiKeyProvider,
        onInterruptPlayback = onInterruptPlayback
    )

    fun isServiceEnabled(): Boolean = JarvisAutomationService.isServiceRunning()

    fun clickNodeByText(text: String): Boolean = automator.findAndClick(text)
    fun clickNodeById(viewId: String): Boolean {
        val nodes = automator.findNodesById(viewId)
        return nodes.firstOrNull()?.let { automator.clickNodeOrParent(it) } ?: false
    }
    fun inputText(text: String): Boolean = automator.findAndSetText("", text)
    fun scrollForward(): Boolean = automator.performSwipe(500f, 1500f, 500f, 300f)
    fun scrollBackward(): Boolean = automator.performSwipe(500f, 300f, 500f, 1500f)
    fun performBack(): Boolean = automator.performGlobalBack()
    fun performHome(): Boolean = automator.performGlobalHome()
    fun performRecents(): Boolean = automator.performGlobalRecents()
    fun openNotifications(): Boolean = automator.performGlobalNotifications()
    fun launchAppByLabel(label: String): Boolean = automator.launchAppByLabel(context, label)
    fun launchApp(packageName: String): Boolean = automator.launchApp(context, packageName)

    fun startDuplexVoice(scope: CoroutineScope) {
        duplexEngine.startDuplexLoop(scope)
    }

    fun stopDuplexVoice() {
        duplexEngine.stopDuplexLoop()
    }
}
