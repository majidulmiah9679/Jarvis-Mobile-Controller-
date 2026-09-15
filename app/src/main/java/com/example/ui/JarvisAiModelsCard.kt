package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Distinct cyberpunk colors matching Boss Edition HTML
val NeonGreenBorder = Color(0xFF00FF88)
val NeonCyanLink = Color(0xFF00E5FF)
val DarkGreenGlass = Color(0xFF04140D)
val InputSurfaceColor = Color(0xFF072016)

/**
 * AI MODELS & FREE KEYS - BOSS EDITION
 * Real Android Jetpack Compose implementation of the Boss Edition key card.
 * Saves keys persistently and routes real queries to Groq, Gemini, OpenRouter, DeepSeek, HuggingFace.
 */
@Composable
fun AiModelsAndFreeKeysCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Local form states initialized with persistent values
    var groqKeyInput by remember { mutableStateOf(viewModel.groqKey) }
    var groqModelInput by remember { mutableStateOf(viewModel.groqModel) }
    var showGroqKey by remember { mutableStateOf(false) }
    var groqDropdownExpanded by remember { mutableStateOf(false) }

    var geminiKeyInput by remember { mutableStateOf(viewModel.geminiKey.ifBlank { viewModel.apiKey }) }
    var geminiModelInput by remember { mutableStateOf(viewModel.geminiModel) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var geminiDropdownExpanded by remember { mutableStateOf(false) }

    var openrouterKeyInput by remember { mutableStateOf(viewModel.openrouterKey) }
    var showOpenrouterKey by remember { mutableStateOf(false) }

    var deepseekKeyInput by remember { mutableStateOf(viewModel.deepseekKey) }
    var showDeepseekKey by remember { mutableStateOf(false) }

    var hfKeyInput by remember { mutableStateOf(viewModel.hfKey) }
    var showHfKey by remember { mutableStateOf(false) }

    var activeBrainChoice by remember { mutableStateOf(viewModel.activeBrain) }
    var saveStatusMsg by remember { mutableStateOf(viewModel.aiSaveStatusText) }

    val groqModelOptions = listOf(
        "llama-3.3-70b-versatile" to "llama-3.3-70b (Best Bangla + Fast)",
        "llama-3.1-8b-instant" to "llama-3.1-8b (Super Fast)",
        "mixtral-8x7b-32768" to "mixtral (Long Memory)"
    )

    val geminiModelOptions = listOf(
        "gemini-2.0-flash" to "gemini-2.0-flash (Fast + Vision)",
        "gemini-1.5-flash" to "gemini-1.5-flash (Free Long)"
    )

    fun openBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("URL", url))
            Toast.makeText(context, "URL Copied: $url", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        color = DarkGreenGlass,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, NeonGreenBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_models_free_keys_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonGreenBorder.copy(alpha = 0.2f))
                            .border(1.dp, NeonGreenBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧠", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI MODELS & FREE KEYS",
                            color = NeonGreenBorder,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Sob REAL API, 100% Free. Key bosalei JARVIS cholbe.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Brain Selector Chips
            Text(
                text = "ACTIVE BRAIN ENGINE (SELECT PRIMARY):",
                color = NeonGreenBorder,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("GROQ", "GEMINI", "OPENROUTER", "AUTO").forEach { brain ->
                    val isSelected = activeBrainChoice == brain
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonGreenBorder else Color(0xFF0A2B1D))
                            .border(1.dp, if (isSelected) Color.White else NeonGreenBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { activeBrainChoice = brain }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brain,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= 1. GROQ (FASTEST) =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF02170D))
                    .border(1.dp, if (activeBrainChoice == "GROQ") NeonGreenBorder else NeonGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1. GROQ (Main Brain - Fastest)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (groqKeyInput.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonGreenBorder))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("READY", color = NeonGreenBorder, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Key input
                OutlinedTextField(
                    value = groqKeyInput,
                    onValueChange = { groqKeyInput = it },
                    placeholder = { Text("gsk_xxxxxxxxxxxx", color = Color.Gray, fontSize = 12.sp) },
                    visualTransformation = if (showGroqKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGroqKey = !showGroqKey }) {
                            Icon(
                                if (showGroqKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonGreenBorder,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputSurfaceColor,
                        unfocusedContainerColor = InputSurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonGreenBorder,
                        focusedBorderColor = NeonGreenBorder,
                        unfocusedBorderColor = NeonGreenBorder.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("groq_key_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Model Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(InputSurfaceColor)
                            .border(1.dp, NeonGreenBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { groqDropdownExpanded = true }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val label = groqModelOptions.find { it.first == groqModelInput }?.second ?: groqModelInput
                            Text(text = label, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = NeonGreenBorder, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = groqDropdownExpanded,
                        onDismissRequest = { groqDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF031A0F))
                    ) {
                        groqModelOptions.forEach { (modelId, modelLabel) ->
                            DropdownMenuItem(
                                text = { Text(modelLabel, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    groqModelInput = modelId
                                    groqDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Free Key Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openBrowser("https://console.groq.com/keys") }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyanLink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔗 Free Key Paoar Link: console.groq.com/keys",
                        color = NeonCyanLink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 2. GEMINI (BEST BANGLA) =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF02170D))
                    .border(1.dp, if (activeBrainChoice == "GEMINI") NeonGreenBorder else NeonGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "2. GEMINI (Best for Bangla Voice)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (geminiKeyInput.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonGreenBorder))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("READY", color = NeonGreenBorder, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = geminiKeyInput,
                    onValueChange = { geminiKeyInput = it },
                    placeholder = { Text("AIzaxxxxxxxxxxxx", color = Color.Gray, fontSize = 12.sp) },
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Icon(
                                if (showGeminiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonGreenBorder,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputSurfaceColor,
                        unfocusedContainerColor = InputSurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonGreenBorder,
                        focusedBorderColor = NeonGreenBorder,
                        unfocusedBorderColor = NeonGreenBorder.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("gemini_key_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Gemini Model Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(InputSurfaceColor)
                            .border(1.dp, NeonGreenBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { geminiDropdownExpanded = true }
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val label = geminiModelOptions.find { it.first == geminiModelInput }?.second ?: geminiModelInput
                            Text(text = label, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = NeonGreenBorder, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = geminiDropdownExpanded,
                        onDismissRequest = { geminiDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF031A0F))
                    ) {
                        geminiModelOptions.forEach { (modelId, modelLabel) ->
                            DropdownMenuItem(
                                text = { Text(modelLabel, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    geminiModelInput = modelId
                                    geminiDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openBrowser("https://aistudio.google.com/app/apikey") }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyanLink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔗 Free Key Paoar Link: aistudio.google.com/app/apikey",
                        color = NeonCyanLink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 3. OPENROUTER =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF02170D))
                    .border(1.dp, if (activeBrainChoice == "OPENROUTER") NeonGreenBorder else NeonGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "3. OPENROUTER (100+ Model Free)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = openrouterKeyInput,
                    onValueChange = { openrouterKeyInput = it },
                    placeholder = { Text("sk-or-xxxxxxxx", color = Color.Gray, fontSize = 12.sp) },
                    visualTransformation = if (showOpenrouterKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOpenrouterKey = !showOpenrouterKey }) {
                            Icon(
                                if (showOpenrouterKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonGreenBorder,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputSurfaceColor,
                        unfocusedContainerColor = InputSurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonGreenBorder,
                        focusedBorderColor = NeonGreenBorder,
                        unfocusedBorderColor = NeonGreenBorder.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("openrouter_key_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openBrowser("https://openrouter.ai/keys") }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyanLink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔗 Free Key Paoar Link: openrouter.ai/keys ($5 Free)",
                        color = NeonCyanLink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 4. DEEPSEEK =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF02170D))
                    .border(1.dp, NeonGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "4. DEEPSEEK (Coding Brain)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = deepseekKeyInput,
                    onValueChange = { deepseekKeyInput = it },
                    placeholder = { Text("sk-xxxxxxxx", color = Color.Gray, fontSize = 12.sp) },
                    visualTransformation = if (showDeepseekKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showDeepseekKey = !showDeepseekKey }) {
                            Icon(
                                if (showDeepseekKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonGreenBorder,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputSurfaceColor,
                        unfocusedContainerColor = InputSurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonGreenBorder,
                        focusedBorderColor = NeonGreenBorder,
                        unfocusedBorderColor = NeonGreenBorder.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("deepseek_key_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openBrowser("https://platform.deepseek.com/api_keys") }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyanLink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔗 Free Key Paoar Link: platform.deepseek.com/api_keys",
                        color = NeonCyanLink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= 5. HUGGINGFACE =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF02170D))
                    .border(1.dp, NeonGreenBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "5. HUGGINGFACE (Free Vision + Voice)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = hfKeyInput,
                    onValueChange = { hfKeyInput = it },
                    placeholder = { Text("hf_xxxxxxxx", color = Color.Gray, fontSize = 12.sp) },
                    visualTransformation = if (showHfKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showHfKey = !showHfKey }) {
                            Icon(
                                if (showHfKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = NeonGreenBorder,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputSurfaceColor,
                        unfocusedContainerColor = InputSurfaceColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonGreenBorder,
                        focusedBorderColor = NeonGreenBorder,
                        unfocusedBorderColor = NeonGreenBorder.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("hf_key_input")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { openBrowser("https://huggingface.co/settings/tokens") }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyanLink, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔗 Free Key Paoar Link: huggingface.co/settings/tokens",
                        color = NeonCyanLink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================= SAVE ALL KEYS BUTTON =================
            Button(
                onClick = {
                    viewModel.saveAllAiKeys(
                        groq = groqKeyInput,
                        groqMdl = groqModelInput,
                        gemini = geminiKeyInput,
                        geminiMdl = geminiModelInput,
                        openrouter = openrouterKeyInput,
                        deepseek = deepseekKeyInput,
                        hf = hfKeyInput,
                        active = activeBrainChoice
                    )
                    saveStatusMsg = "✅ Boss, Jekono Key Save Done! A-Z sob cholbe."
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreenBorder,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_all_keys_btn")
            ) {
                Text(
                    text = "SAVE ALL KEYS TO LOCALSTORAGE (REAL)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (saveStatusMsg.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0A3320),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonGreenBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreenBorder, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveStatusMsg,
                            color = NeonGreenBorder,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
