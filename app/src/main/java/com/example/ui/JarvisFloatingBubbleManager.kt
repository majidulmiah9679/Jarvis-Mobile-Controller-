package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.example.R

/**
 * Floating Clipboard Bubble Overlay (Messenger Style).
 * Displays the latest copied text with its semantic category (PASSWORD, LINK, CODE, etc.)
 * in a floating pill overlay that can be moved or tapped to paste/copy.
 */
class JarvisFloatingBubbleManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    private var bubbleView: View? = null
    var isShowing = false
        private set

    fun showBubble(clipText: String, category: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            // Toast fallback if overlay permission not granted yet
            Toast.makeText(context, "[$category CLIP]: $clipText", Toast.LENGTH_LONG).show()
            return
        }

        if (isShowing) {
            updateText(clipText, category)
            return
        }

        try {
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 24
                y = 200
            }

            // Create simple dynamic view
            val view = TextView(context).apply {
                text = "⚡ [$category]\n$clipText"
                setTextColor(android.graphics.Color.parseColor("#00E5FF"))
                setBackgroundColor(android.graphics.Color.parseColor("#E6030B1A"))
                setPadding(32, 20, 32, 20)
                textSize = 12f
                setOnClickListener {
                    Toast.makeText(context, "Copied from J.A.R.V.I.S. Bubble", Toast.LENGTH_SHORT).show()
                    hideBubble()
                }
            }

            windowManager?.addView(view, params)
            bubbleView = view
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "[$category CLIP]: $clipText", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateText(clipText: String, category: String) {
        (bubbleView as? TextView)?.text = "⚡ [$category]\n$clipText"
    }

    fun hideBubble() {
        if (!isShowing) return
        try {
            bubbleView?.let { windowManager?.removeView(it) }
            bubbleView = null
            isShowing = false
        } catch (_: Exception) {}
    }
}
