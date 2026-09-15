package com.example.vision

import android.content.Context
import android.content.pm.PackageManager
import com.example.JarvisAutomationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ScreenContextData(
    val packageName: String,
    val appLabel: String,
    val rawHierarchy: String,
    val extractedButtons: List<String>,
    val extractedInputs: List<String>,
    val extractedTexts: List<String>
)

/**
 * Screen Vision Analyzer for J.A.R.V.I.S.
 * Parses active window hierarchy, buttons, text inputs, and generates
 * structural on-screen multimodal context for the Gemini model.
 */
class ScreenVisionAnalyzer(private val context: Context) {

    suspend fun captureActiveScreenContext(): ScreenContextData = withContext(Dispatchers.IO) {
        val service = JarvisAutomationService.getInstance()
        val automator = service?.automator

        if (automator == null) {
            return@withContext ScreenContextData(
                packageName = "com.jarvis.ultimate",
                appLabel = "J.A.R.V.I.S. (Self)",
                rawHierarchy = "Accessibility service standby. Screen introspection active in dashboard.",
                extractedButtons = listOf("Voice Command", "Controller", "Settings"),
                extractedInputs = listOf("Command Input Field"),
                extractedTexts = listOf("System Ready")
            )
        }

        val raw = automator.dumpScreenHierarchy()
        val activePkg = service.rootInActiveWindow?.packageName?.toString() ?: "Unknown"

        val pm = context.packageManager
        val appLabel = try {
            val info = pm.getApplicationInfo(activePkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            activePkg
        }

        val buttons = mutableListOf<String>()
        val inputs = mutableListOf<String>()
        val texts = mutableListOf<String>()

        raw.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[BTN]") -> buttons.add(trimmed.removePrefix("[BTN]").trim())
                trimmed.startsWith("[INPUT]") -> inputs.add(trimmed.removePrefix("[INPUT]").trim())
                trimmed.startsWith("[TEXT]") -> texts.add(trimmed.removePrefix("[TEXT]").trim())
            }
        }

        ScreenContextData(
            packageName = activePkg,
            appLabel = appLabel,
            rawHierarchy = raw,
            extractedButtons = buttons.take(15),
            extractedInputs = inputs.take(10),
            extractedTexts = texts.take(20)
        )
    }

    fun buildScreenPrompt(userQuestion: String, screenData: ScreenContextData): String {
        return """
            The user is looking at their Android screen in the app '${screenData.appLabel}' (${screenData.packageName}).
            
            CURRENT ON-SCREEN ELEMENTS:
            - Interactive Buttons: ${if (screenData.extractedButtons.isNotEmpty()) screenData.extractedButtons.joinToString(", ") else "None detected"}
            - Editable Input Fields: ${if (screenData.extractedInputs.isNotEmpty()) screenData.extractedInputs.joinToString(", ") else "None detected"}
            - Visible Text Labels: ${screenData.extractedTexts.take(10).joinToString(" | ")}
            
            USER QUESTION ABOUT SCREEN:
            "$userQuestion"
            
            Answer concisely and tell the user what they see, or provide the exact step/button they should press.
        """.trimIndent()
    }
}
