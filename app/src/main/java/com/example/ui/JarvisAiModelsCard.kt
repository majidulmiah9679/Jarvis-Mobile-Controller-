package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonCyan = Color(0xFF00E5FF)
private val ElectricBlue = Color(0xFF0078FF)
private val NeonGreen = Color(0xFF00FF66)
private val AmberGold = Color(0xFFFFCC00)
private val CoralRed = Color(0xFFFF4444)
private val SheetDarkBg = Color(0xFF080F1D)
private val CardDarkBg = Color(0xFF0D1829)

/**
 * Simple, elegant Side Settings Sheet for AI Brain (Gemini & Groq).
 * Opens quickly from Home Screen side button or HUD banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSimpleAiSideSettingsSheet(
    viewModel: JarvisViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetDarkBg,
        tonalElevation = 8.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.5f))
            )
        }
    ) {
        SimpleAiSettingsContent(
            viewModel = viewModel,
            isDark = true,
            isSideSheet = true,
            onClose = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Settings Subpage version (used when navigated from Settings screen).
 */
@Composable
fun JarvisGeminiSettingsSubpage(
    viewModel: JarvisViewModel,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    SimpleAiSettingsContent(
        viewModel = viewModel,
        isDark = isDark,
        isSideSheet = false,
        onClose = null,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

/**
 * Core simple AI configuration content for Google Gemini & Groq.
 */
@Composable
fun SimpleAiSettingsContent(
    viewModel: JarvisViewModel,
    isDark: Boolean = true,
    isSideSheet: Boolean = false,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // Local states
    var selectedActiveEngine by remember { mutableStateOf(viewModel.activeBrain.ifBlank { "GEMINI" }.uppercase()) }
    
    // Gemini states
    var geminiKeyInput by remember { mutableStateOf(viewModel.geminiKey.ifBlank { viewModel.apiKey }) }
    var geminiKeyVisible by remember { mutableStateOf(false) }
    var selectedGeminiModel by remember { mutableStateOf(viewModel.geminiModel.ifBlank { "gemini-2.5-flash" }) }
    var isTestingGemini by remember { mutableStateOf(false) }
    var geminiTestResult by remember { mutableStateOf<String?>(null) }
    var geminiTestSuccess by remember { mutableStateOf<Boolean?>(null) }

    // Groq states
    var groqKeyInput by remember { mutableStateOf(viewModel.groqKey) }
    var groqKeyVisible by remember { mutableStateOf(false) }
    var selectedGroqModel by remember { mutableStateOf(viewModel.groqModel.ifBlank { "llama-3.3-70b-versatile" }) }
    var isTestingGroq by remember { mutableStateOf(false) }
    var groqTestResult by remember { mutableStateOf<String?>(null) }
    var groqTestSuccess by remember { mutableStateOf<Boolean?>(null) }

    val bgColor = if (isDark) CardDarkBg else Color(0xFFF0F4F8)
    val textColor = if (isDark) Color.White else Color(0xFF101828)
    val subTextColor = if (isDark) Color(0xFF90A4AE) else Color(0xFF475467)

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open browser: $url", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .testTag("simple_ai_settings_content")
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI BRAIN SETTINGS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = textColor
                    )
                }
                Text(
                    text = "Google Gemini & Groq LPU (Simple Dual-Engine)",
                    fontSize = 12.sp,
                    color = subTextColor
                )
            }
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = subTextColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ACTIVE BRAIN SELECTOR BAR
        Text(
            text = "CURRENT ACTIVE ENGINE:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Gemini Toggle Pill
            val isGeminiActive = selectedActiveEngine == "GEMINI" || selectedActiveEngine == "GEMINI_CLOUD"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isGeminiActive) Color(0xFF0A2E20) else bgColor,
                border = BorderStroke(1.5.dp, if (isGeminiActive) NeonGreen else Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selectedActiveEngine = "GEMINI"
                        viewModel.activateEngine("GEMINI")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isGeminiActive) NeonGreen else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Gemini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGeminiActive) NeonGreen else subTextColor
                    )
                    if (isGeminiActive) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Groq Toggle Pill
            val isGroqActive = selectedActiveEngine == "GROQ"
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isGroqActive) Color(0xFF261D08) else bgColor,
                border = BorderStroke(1.5.dp, if (isGroqActive) AmberGold else Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        selectedActiveEngine = "GROQ"
                        viewModel.activateEngine("GROQ")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = if (isGroqActive) AmberGold else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Groq LPU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGroqActive) AmberGold else subTextColor
                    )
                    if (isGroqActive) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. GOOGLE GEMINI SECTION
        // ==========================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(1.dp, if (selectedActiveEngine == "GEMINI" || selectedActiveEngine == "GEMINI_CLOUD") NeonGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌟", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Google Gemini AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    val isGeminiConfigured = geminiKeyInput.isNotBlank()
                    Text(
                        text = if (isGeminiConfigured) "● READY" else "○ NO KEY",
                        color = if (isGeminiConfigured) NeonGreen else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Key Input Field
                OutlinedTextField(
                    value = geminiKeyInput,
                    onValueChange = { geminiKeyInput = com.example.network.GeminiNetworkDispatcher.sanitizeKey(it) },
                    label = { Text("Gemini Key / Token (AQ... / AIza... / ya29...)") },
                    placeholder = { Text("AQ..., AIzaSy..., or ya29... token") },
                    visualTransformation = if (geminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (geminiKeyInput.isNotEmpty()) {
                                IconButton(onClick = { geminiKeyInput = "" }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = subTextColor, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = { geminiKeyVisible = !geminiKeyVisible }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    if (geminiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = subTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val text = clipboard.getText()?.text.orEmpty()
                                    val sanitized = com.example.network.GeminiNetworkDispatcher.sanitizeKey(text)
                                    if (sanitized.isNotEmpty()) {
                                        geminiKeyInput = sanitized
                                        Toast.makeText(context, "Pasted Gemini Credential!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Model Selector Chips
                Text(
                    text = "Select Gemini Model (2026 Latest):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                val geminiModels = listOf(
                    "gemini-2.5-flash" to "2.5 Flash ⭐",
                    "gemini-2.5-pro" to "2.5 Pro 🧠",
                    "gemini-flash-latest" to "Flash Auto ⚡",
                    "gemini-3.5-flash" to "3.5 Flash 🚀"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    geminiModels.forEach { (modelId, label) ->
                        val isSelected = selectedGeminiModel == modelId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f))
                                .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedGeminiModel = modelId
                                    viewModel.selectModelForEngine("GEMINI", modelId)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else subTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons (Test & Get Free Key)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (geminiKeyInput.isBlank()) {
                                Toast.makeText(context, "Enter a Gemini key first", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            isTestingGemini = true
                            geminiTestResult = null
                            viewModel.testEngineConnection("GEMINI", geminiKeyInput) { ok, msg, latency ->
                                isTestingGemini = false
                                geminiTestSuccess = ok
                                geminiTestResult = if (ok) "🟢 Connected! Ping: ${latency}ms" else "🔴 $msg"
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        if (isTestingGemini) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { openUrl("https://aistudio.google.com/apikey") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Free Key ↗", fontSize = 11.sp)
                    }
                }

                // Test result feedback
                if (geminiTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = geminiTestResult.orEmpty(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (geminiTestSuccess == true) NeonGreen else CoralRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 2. GROQ CLOUD LPU SECTION
        // ==========================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(1.dp, if (selectedActiveEngine == "GROQ") AmberGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Groq Cloud (LPU)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    val isGroqConfigured = groqKeyInput.isNotBlank()
                    Text(
                        text = if (isGroqConfigured) "● READY" else "○ NO KEY",
                        color = if (isGroqConfigured) AmberGold else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Key Input Field
                OutlinedTextField(
                    value = groqKeyInput,
                    onValueChange = { groqKeyInput = com.example.network.GeminiNetworkDispatcher.sanitizeKey(it) },
                    label = { Text("Groq API Key (gsk_...)") },
                    placeholder = { Text("Paste gsk_... key") },
                    visualTransformation = if (groqKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (groqKeyInput.isNotEmpty()) {
                                IconButton(onClick = { groqKeyInput = "" }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = subTextColor, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = { groqKeyVisible = !groqKeyVisible }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    if (groqKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = subTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val text = clipboard.getText()?.text.orEmpty()
                                    val sanitized = com.example.network.GeminiNetworkDispatcher.sanitizeKey(text)
                                    if (sanitized.isNotEmpty()) {
                                        groqKeyInput = sanitized
                                        Toast.makeText(context, "Pasted Groq Key!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = AmberGold, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Model Selector Chips
                Text(
                    text = "Select Groq Model (800+ T/s):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                val groqModels = listOf(
                    "llama-3.3-70b-versatile" to "LLaMA 3.3 ⭐",
                    "llama-3.1-8b-instant" to "LLaMA 3.1 ⚡",
                    "deepseek-r1-distill-llama-70b" to "DeepSeek R1 🧠",
                    "gemma2-9b-it" to "Gemma 2 🌐"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groqModels.forEach { (modelId, label) ->
                        val isSelected = selectedGroqModel == modelId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AmberGold.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f))
                                .border(1.dp, if (isSelected) AmberGold else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedGroqModel = modelId
                                    viewModel.selectModelForEngine("GROQ", modelId)
                                }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AmberGold else subTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons (Test & Get Free Key)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (groqKeyInput.isBlank()) {
                                Toast.makeText(context, "Enter a Groq key first", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            isTestingGroq = true
                            groqTestResult = null
                            viewModel.testEngineConnection("GROQ", groqKeyInput) { ok, msg, latency ->
                                isTestingGroq = false
                                groqTestSuccess = ok
                                groqTestResult = if (ok) "⚡ Connected! Ping: ${latency}ms" else "🔴 $msg"
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold)
                    ) {
                        if (isTestingGroq) {
                            CircularProgressIndicator(color = AmberGold, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { openUrl("https://console.groq.com/keys") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Free Key ↗", fontSize = 11.sp)
                    }
                }

                // Test result feedback
                if (groqTestResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = groqTestResult.orEmpty(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (groqTestSuccess == true) AmberGold else CoralRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 3. MASTER SAVE & ACTIVATE BUTTON
        // ==========================================
        Button(
            onClick = {
                viewModel.saveAiKeys(
                    groq = groqKeyInput,
                    groqMdl = selectedGroqModel,
                    gemini = geminiKeyInput,
                    geminiMdl = selectedGeminiModel,
                    active = selectedActiveEngine
                )
                Toast.makeText(
                    context,
                    "✅ AI Brain Saved! Active: $selectedActiveEngine",
                    Toast.LENGTH_SHORT
                ).show()
                onClose?.invoke()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedActiveEngine == "GROQ") AmberGold else NeonCyan
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_ai_keys_btn")
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE & ACTIVATE AI BRAIN",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Compact AI Models Status Card for Dashboard embedding.
 */
@Composable
fun JarvisAiModelsCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val activeEngine = viewModel.activeBrain
    val hasKey = viewModel.getEngineKey(activeEngine).isNotBlank()
    val model = viewModel.getEngineModel(activeEngine)

    Surface(
        color = Color(0xF001130A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (hasKey) NeonGreen else AmberGold),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_models_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = if (hasKey) NeonGreen else AmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI BRAIN: $activeEngine",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = if (hasKey) "● ONLINE" else "○ NO KEY",
                    color = if (hasKey) NeonGreen else AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Model: $model\nAvailable Engines: Google Gemini, Groq LPU",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Legacy compatibility function.
 */
@Composable
fun JarvisGeminiSettingsCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    JarvisAiModelsCard(viewModel = viewModel, modifier = modifier)
}

@Composable
fun AiModelsAndFreeKeysCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    JarvisAiModelsCard(viewModel = viewModel, modifier = modifier)
}
