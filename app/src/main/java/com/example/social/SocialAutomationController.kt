package com.example.social

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import com.example.JarvisAutomationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

/**
 * Autonomous Social & Messaging Controller for J.A.R.V.I.S.
 * Automates WhatsApp, WhatsApp Business, Instagram, Facebook, and incoming calls.
 */
class SocialAutomationController(private val context: Context) {

    /**
     * Autonomous WhatsApp Message Dispatch:
     * 1. Opens target chat via WhatsApp deep-link.
     * 2. Waits for window to load.
     * 3. Uses Accessibility to locate the send button or input node and auto-clicks Send.
     */
    fun sendWhatsAppAutonomous(
        phoneNumber: String,
        message: String,
        isBusiness: Boolean = false,
        onStatus: (String) -> Unit
    ) {
        val cleanPhone = phoneNumber.filter { it.isDigit() || it == '+' }
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (e: Exception) {
            message
        }

        val pkg = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"
        val uri = if (cleanPhone.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
        } else {
            Uri.parse("whatsapp://send?text=$encodedMessage")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(pkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
            onStatus("Targeting $pkg: Chat opened with prefilled message.")

            // Coroutine to find and click the send button autonomously
            CoroutineScope(Dispatchers.IO).launch {
                delay(1200) // Allow WhatsApp UI to render
                val service = JarvisAutomationService.getInstance()
                if (service != null) {
                    val automator = service.automator
                    // Try WhatsApp send button resource IDs and labels
                    var clicked = false
                    val sendIds = listOf(
                        "$pkg:id/send",
                        "$pkg:id/fab",
                        "com.whatsapp:id/send"
                    )

                    for (id in sendIds) {
                        val nodes = automator.findNodesById(id)
                        if (nodes.isNotEmpty()) {
                            clicked = automator.clickNodeOrParent(nodes[0])
                            if (clicked) break
                        }
                    }

                    if (!clicked) {
                        // Fallback to text / content description "Send" / "পাঠান"
                        clicked = automator.findAndClick("Send") || automator.findAndClick("পাঠান")
                    }

                    if (clicked) {
                        onStatus("Autonomous Auto-Send executed successfully in WhatsApp!")
                    } else {
                        onStatus("Message ready in input field. Tap Send or allow Accessibility permission.")
                    }
                }
            }
        } catch (e: Exception) {
            onStatus("WhatsApp application ($pkg) not found on device.")
        }
    }

    /**
     * Instagram Story / Post Automation Navigation.
     */
    fun openInstagramStoryComposer(mediaUri: Uri? = null, onStatus: (String) -> Unit) {
        val pkg = "com.instagram.android"
        try {
            val intent = if (mediaUri != null) {
                Intent("com.instagram.share.ADD_TO_STORY").apply {
                    setDataAndType(mediaUri, "image/*")
                    setPackage(pkg)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (intent != null) {
                context.startActivity(intent)
                onStatus("Instagram Story composer opened successfully.")
            } else {
                onStatus("Instagram is not installed.")
            }
        } catch (e: Exception) {
            onStatus("Could not launch Instagram: ${e.message}")
        }
    }

    /**
     * Facebook Post Composer Automation.
     */
    fun openFacebookComposer(text: String, onStatus: (String) -> Unit) {
        val pkg = "com.facebook.katana"
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("Facebook post draft initialized.")
        } catch (e: Exception) {
            onStatus("Facebook app not found or could not be opened.")
        }
    }

    /**
     * Voice-driven / Autonomous Incoming Call Handling.
     */
    fun answerIncomingCall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val tm = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                if (tm != null) {
                    tm.acceptRingingCall()
                    return true
                }
            } catch (e: SecurityException) {
                // Permission not granted, fallback to accessibility
            }
        }

        // Accessibility Fallback: click "Answer", "Accept", "উত্তোলন"
        val service = JarvisAutomationService.getInstance()
        if (service != null) {
            return service.automator.findAndClick("Answer") ||
                    service.automator.findAndClick("Accept") ||
                    service.automator.findAndClick("Receive")
        }
        return false
    }

    fun rejectIncomingCall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val tm = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                if (tm != null) {
                    return tm.endCall()
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }

        // Accessibility Fallback: click "Decline", "Reject", "Dismiss"
        val service = JarvisAutomationService.getInstance()
        if (service != null) {
            return service.automator.findAndClick("Decline") ||
                    service.automator.findAndClick("Reject") ||
                    service.automator.findAndClick("Dismiss")
        }
        return false
    }
}
