package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Advanced Accessibility UI Automator for J.A.R.V.I.S.
 * Enables autonomous node finding, clicking, text injection, gestures, and app launching across 3rd-party apps.
 */
class EnhancedAccessibilityAutomator(
    private val serviceProvider: () -> AccessibilityService?
) {

    private val service: AccessibilityService?
        get() = serviceProvider()

    val isAvailable: Boolean
        get() = service != null

    /**
     * Recursively searches for nodes matching text (case-insensitive substring).
     */
    fun findNodesByText(text: String, root: AccessibilityNodeInfo? = null): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        if (text.isBlank()) return result
        val targetRoot = root ?: try { service?.rootInActiveWindow } catch (_: Exception) { null } ?: return result

        try {
            val directMatches = targetRoot.findAccessibilityNodeInfosByText(text)
            if (!directMatches.isNullOrEmpty()) {
                result.addAll(directMatches)
            }

            // Recursive backup in case findAccessibilityNodeInfosByText misses custom views
            searchNodesRecursive(targetRoot, text.lowercase()) { node ->
                if (!result.contains(node)) {
                    result.add(node)
                }
            }
        } catch (_: Exception) {}
        return result
    }

    private fun searchNodesRecursive(node: AccessibilityNodeInfo?, query: String, onMatch: (AccessibilityNodeInfo) -> Unit) {
        if (node == null) return
        try {
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            if (text.contains(query) || desc.contains(query)) {
                onMatch(node)
            }
            val count = node.childCount
            for (i in 0 until count) {
                val child = try { node.getChild(i) } catch (_: Exception) { null }
                if (child != null) {
                    searchNodesRecursive(child, query, onMatch)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Finds nodes by full view resource ID (e.g., "com.whatsapp:id/send").
     */
    fun findNodesById(viewId: String, root: AccessibilityNodeInfo? = null): List<AccessibilityNodeInfo> {
        if (viewId.isBlank()) return emptyList()
        val targetRoot = root ?: try { service?.rootInActiveWindow } catch (_: Exception) { null } ?: return emptyList()
        return try {
            targetRoot.findAccessibilityNodeInfosByViewId(viewId) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Finds the first node matching text or description and executes a click,
     * traversing up to clickable ancestors if necessary.
     */
    fun findAndClick(targetText: String): Boolean {
        val root = try { service?.rootInActiveWindow } catch (_: Exception) { null } ?: return false
        val nodes = findNodesByText(targetText, root)
        for (node in nodes) {
            if (clickNodeOrParent(node)) {
                return true
            }
        }
        return false
    }

    /**
     * Performs click on a node. If the node itself is not clickable, climbs up parents.
     */
    fun clickNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        try {
            var current = node
            while (current != null) {
                if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                current = current.parent
            }
            // Fallback: try direct click anyway
            return node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Injects text directly into an editable text field node.
     */
    fun setNodeText(node: AccessibilityNodeInfo, text: String): Boolean {
        return try {
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            if (!success) {
                node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Finds an editable field on screen and types text into it.
     */
    fun findAndSetText(query: String, textToType: String): Boolean {
        val root = try { service?.rootInActiveWindow } catch (_: Exception) { null } ?: return false
        val nodes = findNodesByText(query, root)
        for (node in nodes) {
            if (node.isEditable) {
                return setNodeText(node, textToType)
            }
        }
        val editable = findFirstEditableNode(root)
        if (editable != null) {
            return setNodeText(editable, textToType)
        }
        return false
    }

    private fun findFirstEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        try {
            if (node.isEditable) return node
            val count = node.childCount
            for (i in 0 until count) {
                val child = try { node.getChild(i) } catch (_: Exception) { null }
                val found = findFirstEditableNode(child)
                if (found != null) return found
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Dispatches arbitrary continuous swipe gesture between two points.
     */
    fun performSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300L
    ): Boolean {
        val s = service ?: return false
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val swipePath = Path().apply {
                    moveTo(startX, startY)
                    lineTo(endX, endY)
                }
                val stroke = GestureDescription.StrokeDescription(swipePath, 0, durationMs.coerceAtLeast(50))
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                s.dispatchGesture(gesture, null, null)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Global Navigation Actions: Back, Home, Recents, Notifications, Lock Screen.
     */
    fun performGlobalBack(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) ?: false
    fun performGlobalHome(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) ?: false
    fun performGlobalRecents(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) ?: false
    fun performGlobalNotifications(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
    fun performGlobalQuickSettings(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) ?: false
    fun performGlobalPowerDialog(): Boolean = service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) ?: false
    fun performGlobalTakeScreenshot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT) ?: false
        } else {
            false
        }
    }
    fun performGlobalLockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) ?: false
        } else {
            false
        }
    }

    /**
     * App Launcher & Switcher by package name.
     */
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * App Launcher by common label / app title (e.g., "WhatsApp", "YouTube", "Spotify").
     */
    fun launchAppByLabel(context: Context, label: String): Boolean {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in packages) {
            val appLabel = pm.getApplicationLabel(app).toString()
            if (appLabel.equals(label, ignoreCase = true) || appLabel.contains(label, ignoreCase = true)) {
                return launchApp(context, app.packageName)
            }
        }
        return false
    }

    /**
     * Dumps the entire active window node hierarchy into a clean structured text snapshot.
     * This provides on-screen vision context for Multimodal LLMs!
     */
    fun dumpScreenHierarchy(root: AccessibilityNodeInfo? = service?.rootInActiveWindow): String {
        if (root == null) return "Screen context: Window content not accessible or screen is blank."
        val builder = StringBuilder()
        val pkg = root.packageName?.toString() ?: "Unknown"
        builder.appendLine("ACTIVE APP: $pkg")
        dumpNodeRecursive(root, 0, builder)
        return builder.toString()
    }

    private fun dumpNodeRecursive(node: AccessibilityNodeInfo?, depth: Int, builder: StringBuilder) {
        if (node == null || depth > 8) return
        val indent = "  ".repeat(depth)
        val text = node.text?.toString()?.take(50)
        val desc = node.contentDescription?.toString()?.take(50)
        val viewId = node.viewIdResourceName
        val isClick = node.isClickable
        val isEdit = node.isEditable

        if (!text.isNullOrBlank() || !desc.isNullOrBlank() || isClick || isEdit) {
            val label = text ?: desc ?: ""
            val role = when {
                isEdit -> "[INPUT]"
                isClick -> "[BTN]"
                else -> "[TEXT]"
            }
            val idPart = if (viewId != null) " (#$viewId)" else ""
            builder.appendLine("$indent$role $label$idPart")
        }

        for (i in 0 until node.childCount) {
            dumpNodeRecursive(node.getChild(i), depth + 1, builder)
        }
    }
}
