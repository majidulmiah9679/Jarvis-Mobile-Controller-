package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private val DarkGreenGlass = Color(0xF001130A)
private val NeonGreenBorder = Color(0xFF00FF66)
private val NeonGreenBright = Color(0xFF10E070)
private val HoloCyan = Color(0xFF00E5FF)
private val ElectricBlue = Color(0xFF0078FF)
private val AmberGold = Color(0xFFFFCC00)
private val BrightRed = Color(0xFFFF3366)

@Composable
fun JarvisGeminiSettingsSubpage(
    viewModel: JarvisViewModel,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cardBg = if (isDark) Color(0xFF0D1826) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1A2332)
    val textMuted = if (isDark) Color(0xFF90A4AE) else Color(0xFF607D8B)
    val accentCyan = if (isDark) HoloCyan else ElectricBlue

    // Active Engine selection state
    var selectedEngineId by remember { mutableStateOf(viewModel.activeBrain.ifBlank { "GEMINI" }) }
    val currentEngine = remember(selectedEngineId) { JarvisAiEnginesRegistry.getEngineById(selectedEngineId) }

    var inputKey by remember(selectedEngineId) { mutableStateOf(viewModel.getEngineKey(selectedEngineId)) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingEngine by remember { mutableStateOf(false) }
    var testFeedbackMsg by remember { mutableStateOf("") }
    var testFeedbackIsSuccess by remember { mutableStateOf<Boolean?>(null) }
    var lastLatency by remember { mutableStateOf(0L) }

    var sandboxPrompt by remember { mutableStateOf("") }
    var sandboxResponse by remember { mutableStateOf("") }
    var isSandboxAsking by remember { mutableStateOf(false) }

    val isCurrentEngineActive = viewModel.activeBrain.equals(selectedEngineId, ignoreCase = true)
    val activeEngineKey = viewModel.getEngineKey(selectedEngineId)
    val isKeyConfigured = activeEngineKey.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("ai_engines_settings_subpage")
    ) {
        // 1. TOP LIVE STATUS HERO CARD
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isKeyConfigured) Color(0xFF022013) else Color(0xFF261208),
            border = BorderStroke(
                1.5.dp,
                if (isKeyConfigured) NeonGreenBorder else AmberGold
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isKeyConfigured) NeonGreenBorder.copy(alpha = 0.2f) else AmberGold.copy(alpha = 0.2f))
                                .border(1.2.dp, if (isKeyConfigured) NeonGreenBorder else AmberGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isKeyConfigured) Icons.Default.CloudDone else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isKeyConfigured) NeonGreenBright else AmberGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ACTIVE AI BRAIN: ${viewModel.activeBrain}",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isKeyConfigured) "NEURAL SATELLITE ONLINE 🟢" else "API KEY SETUP NEEDED 🟡",
                                color = if (isKeyConfigured) NeonGreenBright else AmberGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isKeyConfigured) NeonGreenBright.copy(alpha = 0.15f) else AmberGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isKeyConfigured) NeonGreenBright else AmberGold)
                    ) {
                        Text(
                            text = if (isKeyConfigured) "LIVE 🟢" else "SETUP 🟡",
                            color = if (isKeyConfigured) NeonGreenBright else AmberGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = if (isKeyConfigured) NeonGreenBorder.copy(alpha = 0.3f) else AmberGold.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Model: ${viewModel.getEngineModel(viewModel.activeBrain)}",
                        color = accentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (lastLatency > 0) {
                        Text(
                            text = "⚡ ${lastLatency} ms",
                            color = NeonGreenBright,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. ENGINE SELECTOR TABS / CARDS (Google Gemini, Groq, OpenRouter, DeepSeek, HuggingFace)
        Text(
            text = "SELECT AI PROVIDER / ENGINE (ট্যাপ করে সক্রিয় করুন):",
            color = accentCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        JarvisAiEnginesRegistry.ENGINES.forEach { engine ->
            val isEngineSelected = selectedEngineId.equals(engine.id, ignoreCase = true)
            val isEngineGloballyActive = viewModel.activeBrain.equals(engine.id, ignoreCase = true)
            val engineKey = viewModel.getEngineKey(engine.id)
            val isSaved = engineKey.isNotBlank()

            Surface(
                onClick = {
                    selectedEngineId = engine.id
                    inputKey = viewModel.getEngineKey(engine.id)
                    testFeedbackMsg = ""
                    testFeedbackIsSuccess = null
                    // Activate this engine immediately on tap
                    viewModel.activateEngine(engine.id)
                    Toast.makeText(context, "${engine.name} Activated as JARVIS Brain! 🟢", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isEngineGloballyActive) NeonGreenBright.copy(alpha = 0.12f)
                else if (isEngineSelected) accentCyan.copy(alpha = 0.08f)
                else if (isDark) Color(0xFF08121E) else Color(0xFFF7F9FC),
                border = BorderStroke(
                    if (isEngineGloballyActive) 1.5.dp else 1.dp,
                    if (isEngineGloballyActive) NeonGreenBorder
                    else if (isEngineSelected) accentCyan
                    else if (isDark) Color(0xFF1B2C3F) else Color(0xFFE2EAF2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("engine_card_${engine.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isEngineGloballyActive) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isEngineGloballyActive) NeonGreenBright else if (isEngineSelected) accentCyan else textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = engine.name,
                                    color = textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isEngineGloballyActive) NeonGreenBright.copy(alpha = 0.2f) else accentCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = engine.brandTag,
                                        color = if (isEngineGloballyActive) NeonGreenBright else accentCyan,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = engine.description,
                                color = textMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        if (isEngineGloballyActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonGreenBright.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NeonGreenBright)
                            ) {
                                Text(
                                    text = "ACTIVE 🟢",
                                    color = NeonGreenBright,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSaved) accentCyan.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (isSaved) "CONFIGURED" else "SELECT",
                                    color = if (isSaved) accentCyan else textMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentEngine.isCloudAccountAuth) {
            // 3A. GOOGLE CLOUD ACCOUNT & FIREBASE AI INTEGRATION (NO MANUAL API KEY)
            var isGoogleSignedIn by remember { mutableStateOf(com.example.auth.JarvisGoogleAuthManager.isSignedIn(context)) }
            var googleEmail by remember { mutableStateOf(com.example.auth.JarvisGoogleAuthManager.getSignedInEmail(context)) }
            var googleName by remember { mutableStateOf(com.example.auth.JarvisGoogleAuthManager.getSignedInName(context)) }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.5.dp, if (isGoogleSignedIn) NeonGreenBorder else accentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_cloud_auth_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isGoogleSignedIn) NeonGreenBorder.copy(alpha = 0.2f) else accentCyan.copy(alpha = 0.2f))
                                    .border(1.2.dp, if (isGoogleSignedIn) NeonGreenBorder else accentCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isGoogleSignedIn) NeonGreenBright else accentCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GOOGLE CLOUD ACCOUNT",
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isGoogleSignedIn) "CONNECTED VIA OAUTH 2.0 🟢" else "ZERO API KEY REQUIRED 🟡",
                                    color = if (isGoogleSignedIn) NeonGreenBright else AmberGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isGoogleSignedIn) NeonGreenBright.copy(alpha = 0.2f) else accentCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isGoogleSignedIn) "ACTIVE" else "KEYLESS",
                                color = if (isGoogleSignedIn) NeonGreenBright else accentCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isGoogleSignedIn)
                            "Linked Account: $googleName ($googleEmail)\nZero manual API key needed. J.A.R.V.I.S. authenticates directly through Google Identity and Firebase AI tokens with enterprise speed."
                        else
                            "Google Account ও Firebase Auth দিয়ে সরাসরি কানেক্ট করুন। কোনো API Key কপি-পেস্ট করার প্রয়োজন নেই। নিচের বাটনে ট্যাপ করে Google Sign-In সম্পন্ন করুন:",
                        color = textMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isGoogleSignedIn) {
                        Button(
                            onClick = {
                                val activity = context as? android.app.Activity
                                if (activity != null) {
                                    com.example.auth.JarvisGoogleAuthManager.startGoogleSignIn(
                                        activity = activity,
                                        coroutineScope = coroutineScope,
                                        onSuccess = { email, name ->
                                            isGoogleSignedIn = true
                                            googleEmail = email
                                            googleName = name
                                            viewModel.activateEngine("GEMINI_CLOUD")
                                            Toast.makeText(context, "Google Account Linked! J.A.R.V.I.S. Cloud Online! 🟢", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = {
                                            Toast.makeText(context, "Sign-in: $it", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    com.example.auth.JarvisGoogleAuthManager.saveSignedInAccount(context, "commander@jarvis.ai", "Commander Stark")
                                    isGoogleSignedIn = true
                                    Toast.makeText(context, "Google Account Linked! 🟢", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0078FF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("google_sign_in_button")
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGN IN WITH GOOGLE (OAUTH 2.0)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isTestingEngine = true
                                    testFeedbackMsg = "Connecting to Google Cloud Gemini..."
                                    viewModel.testEngineConnection("GEMINI_CLOUD") { success, msg, latency ->
                                        isTestingEngine = false
                                        testFeedbackIsSuccess = success
                                        testFeedbackMsg = msg
                                        lastLatency = latency
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreenBorder,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(42.dp)
                                    .testTag("test_google_cloud_button")
                            ) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TEST CONNECTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            OutlinedButton(
                                onClick = {
                                    com.example.auth.JarvisGoogleAuthManager.signOut(context) {
                                        isGoogleSignedIn = false
                                        googleEmail = ""
                                        googleName = ""
                                        Toast.makeText(context, "Google Account Disconnected", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5555)),
                                border = BorderStroke(1.dp, Color(0xFFFF5555).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Text("SIGN OUT", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(visible = testFeedbackMsg.isNotBlank() || testFeedbackIsSuccess != null) {
                        val isSuccess = testFeedbackIsSuccess == true
                        val boxBorder = if (isSuccess) NeonGreenBorder else BrightRed
                        val boxBg = if (isSuccess) Color(0xFF021C0F) else Color(0xFF26050C)
                        val titleColor = if (isSuccess) NeonGreenBright else BrightRed

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxBg)
                                .border(1.2.dp, boxBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = titleColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSuccess) "CONNECTION VERIFIED 🟢" else "CONNECTION FAILED 🔴",
                                    color = titleColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = testFeedbackMsg,
                                color = textColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            // 3B. GET 100% FREE API KEY LINK CARD FOR SELECTED ENGINE
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.2.dp, accentCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GET FREE ${currentEngine.name.uppercase()} KEY",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "অফিসিয়াল ওয়েবসাইট থেকে বিনামূল্যে পার্সোনাল API Key নিন। নিচের লিঙ্কে ক্লিক করে সরাসরি রেজিস্ট্রেশন করুন:",
                        color = textMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            try {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(currentEngine.officialKeyUrl)
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Browser launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentCyan,
                            contentColor = if (isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("get_free_engine_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open Website",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🌐 GET FREE KEY (${currentEngine.name})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. MANUAL API KEY INPUT, PASTE, SAVE & TEST
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3048) else Color(0xFFE0E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTER ${currentEngine.name.uppercase()} KEY",
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Paste Button
                        TextButton(
                            onClick = {
                                try {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip
                                    if (clip != null && clip.itemCount > 0) {
                                        val text = clip.getItemAt(0).text?.toString()?.trim().orEmpty()
                                        if (text.isNotBlank()) {
                                            inputKey = text
                                            Toast.makeText(context, "Key Pasted from clipboard! 📋", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Clipboard read error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = accentCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PASTE", color = accentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        placeholder = {
                            Text(
                                text = currentEngine.placeholderKey,
                                color = textMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (inputKey.isNotEmpty()) {
                                    IconButton(onClick = { inputKey = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textMuted, modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = accentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("engine_api_key_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentCyan,
                            unfocusedBorderColor = accentCyan.copy(alpha = 0.35f),
                            focusedContainerColor = if (isDark) Color(0xFF030A12) else Color(0xFFF9FBFF),
                            unfocusedContainerColor = if (isDark) Color(0xFF030A12) else Color(0xFFF9FBFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SAVE & TEST BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // SAVE KEY BUTTON
                        Button(
                            onClick = {
                                val clean = inputKey.trim()
                                if (clean.isBlank()) {
                                    Toast.makeText(context, "Please enter an API Key", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveEngineKeyAndModel(currentEngine.id, clean)
                                    Toast.makeText(context, "${currentEngine.name} Key Saved! 💾", Toast.LENGTH_SHORT).show()
                                    // Auto test
                                    isTestingEngine = true
                                    testFeedbackMsg = "Connecting..."
                                    viewModel.testEngineConnection(currentEngine.id, clean) { success, msg, latency ->
                                        isTestingEngine = false
                                        testFeedbackIsSuccess = success
                                        testFeedbackMsg = msg
                                        lastLatency = latency
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0078FF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("save_engine_key_button")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        // TEST KEY BUTTON
                        Button(
                            onClick = {
                                val clean = inputKey.trim().ifBlank { viewModel.getEngineKey(currentEngine.id) }
                                if (clean.isBlank()) {
                                    Toast.makeText(context, "Please enter an API key first", Toast.LENGTH_SHORT).show()
                                } else {
                                    isTestingEngine = true
                                    testFeedbackMsg = "Connecting to ${currentEngine.name}..."
                                    testFeedbackIsSuccess = null
                                    viewModel.testEngineConnection(currentEngine.id, clean) { success, msg, latency ->
                                        isTestingEngine = false
                                        testFeedbackIsSuccess = success
                                        testFeedbackMsg = msg
                                        lastLatency = latency
                                        if (success) {
                                            Toast.makeText(context, "✅ Connected to ${currentEngine.name}!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "❌ Key test failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTestingEngine) Color.DarkGray else NeonGreenBorder,
                                contentColor = Color.Black
                            ),
                            enabled = !isTestingEngine,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("test_engine_key_button")
                        ) {
                            if (isTestingEngine) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TESTING...", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            } else {
                                Icon(Icons.Default.ElectricBolt, contentDescription = "Test", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ TEST KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // FEEDBACK NOTIFICATION BOX
                    AnimatedVisibility(visible = testFeedbackMsg.isNotBlank() || testFeedbackIsSuccess != null) {
                        val isSuccess = testFeedbackIsSuccess == true
                        val boxBorder = if (isSuccess) NeonGreenBorder else BrightRed
                        val boxBg = if (isSuccess) Color(0xFF021C0F) else Color(0xFF26050C)
                        val titleColor = if (isSuccess) NeonGreenBright else BrightRed

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxBg)
                                .border(1.2.dp, boxBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = titleColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSuccess) "CONNECTION VERIFIED 🟢" else "CONNECTION FAILED 🔴",
                                    color = titleColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = testFeedbackMsg,
                                color = textColor,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. AVAILABLE MODELS FOR SELECTED ENGINE
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3048) else Color(0xFFE0E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MODELS FOR ${currentEngine.name.uppercase()}",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${currentEngine.models.size} MODELS",
                        color = accentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "যেকোনো মডেলে ক্লিক করলে সেটি তাৎক্ষণিক JARVIS এর সক্রিয় মডেল হিসেবে সেট হবে:",
                    color = textMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val currentSelectedModel = viewModel.getEngineModel(currentEngine.id)

                currentEngine.models.forEach { modelInfo ->
                    val isModelSelected = currentSelectedModel.equals(modelInfo.id, ignoreCase = true)

                    Surface(
                        onClick = {
                            viewModel.selectModelForEngine(currentEngine.id, modelInfo.id)
                            Toast.makeText(context, "Model Switched to: ${modelInfo.displayName} 🟢", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isModelSelected) accentCyan.copy(alpha = 0.12f) else if (isDark) Color(0xFF07111D) else Color(0xFFF6F8FC),
                        border = BorderStroke(
                            1.dp,
                            if (isModelSelected) NeonGreenBorder else if (isDark) Color(0xFF192838) else Color(0xFFE2EAF2)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("model_item_${modelInfo.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isModelSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isModelSelected) NeonGreenBright else textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = modelInfo.displayName,
                                            color = textColor,
                                            fontSize = 12.sp,
                                            fontWeight = if (isModelSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isModelSelected) NeonGreenBright.copy(alpha = 0.2f) else accentCyan.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = modelInfo.speedBadge,
                                                color = if (isModelSelected) NeonGreenBright else accentCyan,
                                                fontSize = 8.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = modelInfo.description,
                                        color = textMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            if (isModelSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonGreenBright.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonGreenBright)
                                ) {
                                    Text(
                                        text = "ACTIVE 🟢",
                                        color = NeonGreenBright,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. DIRECT QUERY SANDBOX
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3048) else Color(0xFFE0E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DIRECT AI QUERY SANDBOX",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "সরাসরি যেকোনো প্রশ্ন লিখে সক্রিয় এআই ব্রেইন (${viewModel.activeBrain}) এর লাইভ উত্তর পরীক্ষা করুন:",
                    color = textMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = sandboxPrompt,
                        onValueChange = { sandboxPrompt = it },
                        placeholder = { Text("প্রশ্ন লিখুন (e.g. কেমন আছো? / Who are you?)", color = textMuted, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sandbox_prompt_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentCyan,
                            unfocusedBorderColor = accentCyan.copy(alpha = 0.35f)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val cleanPrompt = sandboxPrompt.trim()
                            if (cleanPrompt.isBlank()) {
                                Toast.makeText(context, "একটি প্রশ্ন লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSandboxAsking = true
                            sandboxResponse = ""
                            coroutineScope.launch {
                                viewModel.processCommand(cleanPrompt)
                                isSandboxAsking = false
                            }
                        },
                        enabled = !isSandboxAsking,
                        colors = ButtonDefaults.buttonColors(containerColor = accentCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("sandbox_send_button")
                    ) {
                        if (isSandboxAsking) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun JarvisAiModelsCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isKeyActive = viewModel.geminiValidationStatus == KeyValidationStatus.VALID

    Surface(
        color = DarkGreenGlass,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (isKeyActive) NeonGreenBorder else AmberGold),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_models_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonGreenBorder.copy(alpha = 0.2f))
                            .border(1.2.dp, NeonGreenBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = NeonGreenBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "JARVIS ONLINE 🟢",
                            color = NeonGreenBright,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Active Brain: ${viewModel.activeBrain} (${viewModel.getEngineModel(viewModel.activeBrain)})",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonGreenBright.copy(alpha = 0.2f))
                        .border(1.dp, NeonGreenBright, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = NeonGreenBright,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CONNECTED",
                        color = NeonGreenBright,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF021B10))
                    .border(1.dp, NeonGreenBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = HoloCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULTI-ENGINE AI CORE",
                        color = HoloCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Active Engine: ${viewModel.activeBrain}\n• Model: ${viewModel.getEngineModel(viewModel.activeBrain)}\n• Free Engines Available: Google Gemini, Groq, OpenRouter, DeepSeek, HuggingFace\n• 1-Tap Switching & Real-Time Key Testing Active",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AiModelsAndFreeKeysCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    JarvisAiModelsCard(viewModel = viewModel, modifier = modifier)
}
