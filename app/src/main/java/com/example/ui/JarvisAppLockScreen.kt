package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.StarkGold

data class AppLockItem(
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val description: String
)

val DEFAULT_LOCKABLE_APPS = listOf(
    AppLockItem("WhatsApp", "com.whatsapp", Icons.Default.Security, "Messaging & Media Security"),
    AppLockItem("YouTube", "com.google.android.youtube", Icons.Default.PlayArrow, "Video & Stream Protection"),
    AppLockItem("Gallery", "com.google.android.apps.photos", Icons.Default.Collections, "Photos, Videos & Camera Vault"),
    AppLockItem("Facebook", "com.facebook.katana", Icons.Default.Share, "Social Network Access Control")
)

@Composable
fun JarvisAppLockScreen(viewModel: JarvisViewModel) {
    var challengingApp by remember { mutableStateOf<AppLockItem?>(null) }
    val lockedSet = viewModel.lockedApps

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Card
            item {
                AppLockHeaderCard(
                    lockedCount = lockedSet.size,
                    totalCount = DEFAULT_LOCKABLE_APPS.size,
                    onSimulateChallenge = {
                        challengingApp = DEFAULT_LOCKABLE_APPS.first()
                    }
                )
            }

            // Voice Command Instruction Banner
            item {
                AppLockVoiceTipsBanner()
            }

            // Lockable Apps List
            items(DEFAULT_LOCKABLE_APPS) { app ->
                val isLocked = lockedSet.contains(app.packageName)
                AppLockItemRow(
                    app = app,
                    isLocked = isLocked,
                    onToggle = { enable ->
                        viewModel.setAppLock(app.packageName, app.name, enable)
                    },
                    onTestLaunch = {
                        if (isLocked) {
                            challengingApp = app
                        } else {
                            viewModel.launchOrTestApp(app.packageName, app.name)
                        }
                    }
                )
            }

            // Footer Spacer
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Voice Authentication Challenge Overlay Dialog
        challengingApp?.let { app ->
            VoicePasswordChallengeDialog(
                app = app,
                onDismiss = { challengingApp = null },
                onAuthenticated = {
                    challengingApp = null
                    viewModel.launchOrTestApp(app.packageName, app.name)
                },
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun AppLockHeaderCard(
    lockedCount: Int,
    totalCount: Int,
    onSimulateChallenge: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE051326)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ArcCyan.copy(alpha = 0.2f))
                            .border(1.dp, ArcCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = ArcCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "APP LOCKER // VOICE GUARD",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "BIOMETRIC VOICE PASSCODE: 'BOSS'",
                            color = ArcCyanGlow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    color = if (lockedCount > 0) Color(0xFF2E1500) else Color(0xFF00291D),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (lockedCount > 0) StarkGold else Color(0xFF00E676))
                ) {
                    Text(
                        text = "$lockedCount / $totalCount LOCKED",
                        color = if (lockedCount > 0) StarkGold else Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Secure apps with Stark Voice Authentication. When locked apps are accessed, JARVIS prompts: 'SAY PASSWORD: BOSS'.",
                color = Color(0xFFB0C4DE),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSimulateChallenge,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002238)),
                border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "TEST VOICE CHALLENGE OVERLAY",
                    fontSize = 11.sp,
                    color = ArcCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AppLockVoiceTipsBanner() {
    Surface(
        color = Color(0xCC071A2E),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎙️", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "VOICE COMMANDS:",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Say: 'Lock WhatsApp', 'Unlock YouTube', 'Lock Gallery', 'Lock Facebook'",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AppLockItemRow(
    app: AppLockItem,
    isLocked: Boolean,
    onToggle: (Boolean) -> Unit,
    onTestLaunch: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isLocked) Color(0xFFFF5252).copy(alpha = 0.6f) else ArcCyan.copy(alpha = 0.3f),
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE09182E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLocked) Color(0x33FF5252) else Color(0x3300D4FF))
                        .border(1.dp, if (isLocked) Color(0xFFFF5252) else ArcCyan, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = app.name,
                        tint = if (isLocked) Color(0xFFFF5252) else ArcCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (isLocked) Color(0x33FF5252) else Color(0x3300E676),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isLocked) "LOCKED" else "UNLOCKED",
                                color = if (isLocked) Color(0xFFFF5252) else Color(0xFF00E676),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = app.packageName,
                        color = Color(0xFF7FA2BE),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onTestLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C243B)),
                    border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = if (isLocked) "TEST LOCK" else "OPEN",
                        fontSize = 10.sp,
                        color = ArcCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = isLocked,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF5252),
                        checkedTrackColor = Color(0x66FF5252),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color(0x44002844)
                    )
                )
            }
        }
    }
}

@Composable
fun VoicePasswordChallengeDialog(
    app: AppLockItem,
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit,
    viewModel: JarvisViewModel
) {
    var spokenInput by remember { mutableStateOf("") }
    var authFailed by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (authFailed) Color(0xFFFF5252) else ArcCyanGlow, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030A17)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Shield Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (authFailed) Color(0x33FF5252) else Color(0x3300E5FF))
                        .border(2.dp, if (authFailed) Color(0xFFFF5252) else ArcCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authFailed) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = "Lock Challenge",
                        tint = if (authFailed) Color(0xFFFF5252) else ArcCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SECURITY INTERCEPT // ${app.name.uppercase()}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SAY PASSWORD: BOSS",
                    color = if (authFailed) Color(0xFFFF5252) else ArcCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "App is locked by J.A.R.V.I.S. Say 'Boss' into the microphone or submit voice password below.",
                    color = Color(0xFFB0C4DE),
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Mic Button
                IconButton(
                    onClick = {
                        isListening = !isListening
                        if (isListening) {
                            viewModel.startListeningForAppLock { spoken ->
                                spokenInput = spoken
                                isListening = false
                                if (spoken.trim().equals("Boss", ignoreCase = true) || spoken.contains("boss", ignoreCase = true)) {
                                    authFailed = false
                                    viewModel.speak("Access Granted. Welcome Boss.")
                                    viewModel.toast("Access Granted: Welcome Boss")
                                    onAuthenticated()
                                } else {
                                    authFailed = true
                                    errorMessage = "INCORRECT VOICE PASSCODE"
                                    viewModel.speak("Access Denied! Intruder Blocked.")
                                    viewModel.toast("Access Denied! Intruder Detected")
                                }
                            }
                        } else {
                            viewModel.stopListeningForAppLock()
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFFF1744) else ArcCyan.copy(alpha = 0.2f))
                        .border(1.5.dp, if (isListening) Color.White else ArcCyan, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mic Listen",
                        tint = if (isListening) Color.White else ArcCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = if (isListening) "LISTENING FOR 'BOSS'..." else "TAP MIC TO SPEAK",
                    color = if (isListening) Color(0xFFFF1744) else ArcCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Text input fallback
                OutlinedTextField(
                    value = spokenInput,
                    onValueChange = { spokenInput = it },
                    placeholder = { Text("Or type 'Boss'...", color = Color.Gray, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF07182C),
                        unfocusedContainerColor = Color(0xFF07182C),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = ArcCyan,
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = Color(0xFF003859)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val trimmed = spokenInput.trim()
                                if (trimmed.equals("Boss", ignoreCase = true) || trimmed.contains("boss", ignoreCase = true)) {
                                    authFailed = false
                                    viewModel.speak("Access Granted. Welcome Boss.")
                                    viewModel.toast("Access Granted: Welcome Boss")
                                    onAuthenticated()
                                } else {
                                    authFailed = true
                                    errorMessage = "INCORRECT PASSCODE: '$trimmed'"
                                    viewModel.speak("Access Denied! Intruder Blocked.")
                                    viewModel.toast("Access Denied! Intruder Detected")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Submit", tint = ArcCyan)
                        }
                    }
                )

                if (authFailed && errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "ACCESS DENIED",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = {
                            // Instant Boss override for testing
                            authFailed = false
                            viewModel.speak("Access Granted. Welcome Boss.")
                            viewModel.toast("Access Granted: Welcome Boss")
                            onAuthenticated()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00334D)),
                        border = BorderStroke(1.dp, ArcCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("QUICK AUTH (BOSS)", fontSize = 11.sp, color = ArcCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
