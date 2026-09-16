package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloSurface
import com.example.ui.theme.HoloSurfaceElevated
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary
import com.example.ui.theme.StarkGold
import com.example.persona.JarvisPersona
import com.example.persona.PersonaEngine
import com.example.hardware.SystemHardwareController
import com.example.memory.JarvisMemoryRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainAppScreen(viewModel: JarvisViewModel) {
    val currentTab = viewModel.currentTab
    val isTorchOn = viewModel.isTorchOn
    val isDark = viewModel.isDarkTheme

    val appBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF080B14) else Color(0xFFF5F7FB),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "app_bg"
    )
    val navBarColor by animateColorAsState(
        targetValue = if (isDark) HoloSurface else Color.White,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "nav_bar_bg"
    )
    val primaryAccent by animateColorAsState(
        targetValue = if (isDark) ArcCyan else Color(0xFF0078FF),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "primary_accent"
    )
    val activeNavColor by animateColorAsState(
        targetValue = if (isDark) ArcCyanGlow else Color(0xFF0078FF),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "active_nav_color"
    )
    val inactiveNavColor by animateColorAsState(
        targetValue = if (isDark) HoloTextSecondary else Color(0xFF5A6E85),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "inactive_nav_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Torch active banner notification (non-blocking, interactive)
            if (isTorchOn) {
                Surface(
                    color = StarkGold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleTorchOnly() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TORCH ACTIVE (MAX BEAM)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "[TAP TO TURN OFF]",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Screen content area based on current navigation tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    0 -> JarvisHomeScreen(viewModel = viewModel)
                    1 -> JarvisSecurityScreen(viewModel = viewModel)
                    2 -> JarvisControllerScreen(viewModel = viewModel)
                    3 -> JarvisSettingsScreen(viewModel = viewModel)
                    4 -> JarvisReplyHistoryScreen(viewModel = viewModel)
                    5 -> JarvisAppLockScreen(viewModel = viewModel)
                    else -> JarvisHomeScreen(viewModel = viewModel)
                }
            }

            // 6-Item Bottom Navigation Bar (Main UI, Security, Controller, Settings, Replies, App Lock)
            NavigationBar(
                containerColor = navBarColor,
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryAccent.copy(alpha = 0.25f))
            ) {
                // Tab 0: Main UI
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = {
                        viewModel.selectTab(0)
                        viewModel.logAction("Navigation: Main UI")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Main UI",
                            tint = if (currentTab == 0) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "Main UI",
                            color = if (currentTab == 0) activeNavColor else inactiveNavColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 1: Security
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        viewModel.selectTab(1)
                        viewModel.logAction("Navigation: Security")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Security",
                            tint = if (currentTab == 1) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "Security",
                            color = if (currentTab == 1) activeNavColor else inactiveNavColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 2: Controller
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = {
                        viewModel.selectTab(2)
                        viewModel.logAction("Navigation: Controller")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Controller",
                            tint = if (currentTab == 2) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "Controller",
                            color = if (currentTab == 2) activeNavColor else inactiveNavColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 3: Settings
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = {
                        viewModel.selectTab(3)
                        viewModel.logAction("Navigation: Settings")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (currentTab == 3) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "Settings",
                            color = if (currentTab == 3) activeNavColor else inactiveNavColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 4: Reply History
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = {
                        viewModel.selectTab(4)
                        viewModel.logAction("Navigation: Reply History")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Reply History",
                            tint = if (currentTab == 4) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "Replies",
                            color = if (currentTab == 4) activeNavColor else inactiveNavColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )

                // Tab 5: App Lock
                NavigationBarItem(
                    selected = currentTab == 5,
                    onClick = {
                        viewModel.selectTab(5)
                        viewModel.logAction("Navigation: App Lock")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "App Lock",
                            tint = if (currentTab == 5) activeNavColor else inactiveNavColor
                        )
                    },
                    label = {
                        Text(
                            "App Lock",
                            color = if (currentTab == 5) activeNavColor else inactiveNavColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (currentTab == 5) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = primaryAccent.copy(alpha = 0.15f)
                    )
                )
            }
        }

        // Display Luminance Dim simulation overlay (opacity 0.5 for DIM)
        if (viewModel.dimOverlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = viewModel.dimOverlayAlpha))
            )
        }

        // [FEATURE 44 - SMART NOTIFICATION READER] Floating Pulse Mic Button at bottom right
        // Kept intact, but hidden as requested: zero display on Main UI (display: none)
        if (false) {
            FloatingPulseMicButton(
                isListening = viewModel.isListening,
                onClick = {
                    viewModel.toggleListeningState()
                    viewModel.logAction(if (viewModel.isListening) "FLOATING MIC: LISTENING" else "FLOATING MIC: STOPPED")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 148.dp)
            )
        }

        // Floating Live Talk round pill button & modal - Completely HIDDEN / REMOVED from Main UI as requested
    }
}

@Composable
fun JarvisHomeScreen(viewModel: JarvisViewModel) {
    val coreLog = viewModel.coreLog
    val isListening = viewModel.isListening
    val isProcessing = viewModel.isProcessing

    var commandText by remember { mutableStateOf("") }
    val coreLogScrollState = rememberScrollState()

    // Auto-scroll to bottom of logs on updates
    LaunchedEffect(coreLog) {
        coreLogScrollState.animateScrollTo(coreLogScrollState.maxValue)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DeepSpaceCosmicBackground(isDark = viewModel.isDarkTheme)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP SECTION
            item {
                JarvisEarthHudHeader(viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
                OrbitStatusBar()
                Spacer(modifier = Modifier.height(10.dp))
                CenterCoreDisplayCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                HudBottomControlsCards(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                GlobalActivityWaveformCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // MIDDLE TO BOTTOM SCROLLABLE DASHBOARD SECTION:
            item {
                AmarVoiceLockCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                VoiceAssistantSettingsCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                StarChartNavigationCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                HistoricalThreatLogsCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                NodeNetworkAdvancedCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                AccountPermissionsCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                ArHudLayoutCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                CrossDeviceSyncCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                PowerCoreStatusCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                TheftGuardCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                SmartClipboardCard(viewModel = viewModel)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // INTERACTIVE ACTION BUTTONS ROW ("OPEN", "PLAY YOUTUBE", "SPOTIFY", "WHATSAPP")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HUDActionButton(
                        modifier = Modifier.weight(1f),
                        title = "OPEN",
                        icon = Icons.Default.FolderOpen,
                        onClick = { viewModel.logAction("OPEN") }
                    )
                    HUDActionButton(
                        modifier = Modifier.weight(1.2f),
                        title = "YOUTUBE",
                        icon = Icons.Default.PlayArrow,
                        onClick = { viewModel.logAction("PLAY YOUTUBE") }
                    )
                    HUDActionButton(
                        modifier = Modifier.weight(1.1f),
                        title = "SPOTIFY",
                        icon = Icons.Default.MusicNote,
                        onClick = { viewModel.logAction("SPOTIFY") }
                    )
                    HUDActionButton(
                        modifier = Modifier.weight(1.1f),
                        title = "WHATSAPP",
                        icon = Icons.Default.Message,
                        onClick = { viewModel.logAction("WHATSAPP") }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // J.A.R.V.I.S. EXPANSION BAR (Core Engine, Duplex Audio, Screen Vision, Live Talk)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00D4FF).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFF00D4FF).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00D4FF).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF7DF9FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "CORE", color = Color(0xFF7DF9FF), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewModel.isDuplexEnabled) Color(0xFF80D8FF).copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.4f))
                            .clickable { viewModel.toggleDuplexMode() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = if (viewModel.isDuplexEnabled) Color(0xFF80D8FF) else Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (viewModel.isDuplexEnabled) "DUPLEX LIVE" else "DUPLEX OFF", color = if (viewModel.isDuplexEnabled) Color(0xFF80D8FF) else Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00D4FF).copy(alpha = 0.15f))
                            .clickable { viewModel.captureAndAnalyzeScreen() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFF00FFFF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "VISION", color = Color(0xFF00FFFF), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // AI BRAIN STATUS BANNER - BOSS EDITION
            item {
                Surface(
                    color = Color(0xFF031A0F),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00FF88).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectTab(3) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🧠", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            val hasKey = when (viewModel.activeBrain) {
                                "GROQ" -> viewModel.groqKey.isNotBlank()
                                "GEMINI" -> viewModel.geminiKey.isNotBlank() || viewModel.apiKey.isNotBlank()
                                "OPENROUTER" -> viewModel.openrouterKey.isNotBlank()
                                else -> viewModel.groqKey.isNotBlank() || viewModel.geminiKey.isNotBlank()
                            }
                            Text(
                                text = "AI BRAIN: ${viewModel.activeBrain} [${if (hasKey) "ACTIVE" else "NO KEY"}]",
                                color = Color(0xFF00FF88),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "[FREE KEYS // EDIT]",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // JARVIS CORE LOG BOX
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .border(1.dp, Color(0xFF00D4FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(coreLogScrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = coreLog,
                            color = Color(0xFFE2F1FF),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.fillMaxWidth().testTag("jarvis-core-log")
                        )
                    }
                }
            }

            // INTERACTIVE DELETE PROTECTION SHIELD BANNER (When awaiting confirmation)
            item {
                if (viewModel.isAwaitingDeletePassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF261214),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DELETE PROTECTION INTERCEPT",
                                        color = Color(0xFFFF5252),
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.cancelProtectedDeletion() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("CANCEL", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Boss, '${viewModel.pendingDeleteItemName ?: "আইটেম"}' ডিলিট করার জন্য সিকিউরিটি পাসওয়ার্ড ইনপুট বক্সে লিখে সেন্ড করুন:",
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // BOTTOM COMMAND INPUT ROW WITH MIC & SEND
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commandText,
                        onValueChange = { commandText = it },
                        placeholder = { Text("Command JARVIS...", color = Color(0xFF7FA2BE), fontSize = 13.sp) },
                        modifier = Modifier.weight(1f).testTag("command-input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0A1931),
                            unfocusedContainerColor = Color(0xFF0A1931),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00FFFF),
                            focusedBorderColor = Color(0xFF00FFFF),
                            unfocusedBorderColor = Color(0xFF00D4FF).copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val toSend = commandText.ifBlank { "System Diagnostics" }
                                viewModel.logAction(toSend)
                                viewModel.processCommand(toSend)
                                commandText = ""
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.toggleListeningState()
                            viewModel.logAction(if (viewModel.isListening) "MIC ACTIVATED" else "MIC DEACTIVATED")
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color.Red else Color(0xFF00D4FF).copy(alpha = 0.2f))
                            .border(1.dp, if (isListening) Color.White else Color(0xFF00FFFF), CircleShape)
                            .testTag("mic-btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isListening) Color.White else Color(0xFF7DF9FF)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            val toSend = commandText.ifBlank { "Run System Diagnostics" }
                            viewModel.logAction(toSend)
                            viewModel.processCommand(toSend)
                            commandText = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00D4FF).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF7DF9FF), CircleShape)
                            .testTag("send-btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Command",
                            tint = Color(0xFF7DF9FF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        if (viewModel.showVoiceConfigDialog) {
            VoiceConfigDialog(viewModel = viewModel)
        }
    }
}

// Reusable Interactive HUD Action Button with Ripple Feedback
@Composable
fun HUDActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .border(1.dp, ArcCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = HoloSurface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = ArcCyanGlow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

// Security Tab implemented in JarvisSecurityScreen.kt (Folder Stack Architecture)


// TAB 2: CONTROLLER SCREEN (UPGRADED TO FULL MOBILE CONTROL - REAL APIS)
@Composable
fun JarvisControllerScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    val battery = viewModel.batteryStatus
    val isTorchOn = viewModel.isTorchOn
    val isCleaningSpeaker = viewModel.isCleaningSpeaker
    val cleanSpeakerCountdown = viewModel.cleanSpeakerCountdown
    val isScreenKeepAwake = viewModel.isScreenKeepAwake
    val currentRingerMode = viewModel.currentRingerMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1931), Color(0xFF102542), Color(0xFF060D1A))
                )
            )
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER: Dark Navy + Neon Glassmorphism
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF00FFFF), Color(0xFF0A1931))))
                            .border(1.5.dp, Color(0xFF00D4FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DEVICE CONTROLLER",
                            color = Color(0xFF7DF9FF),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FULL MOBILE HARDWARE BUS // ACTIVE",
                            color = Color(0xFF7DF9FF).copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    color = Color(0xFF102542),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE 5S",
                            color = Color(0xFF00E676),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. BATTERY STATUS (REAL BATTERY LEVEL - UPDATES EVERY 5 SEC)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
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
                            if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = null,
                            tint = if (battery.isCharging) Color(0xFF00E676) else Color(0xFF00FFFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BATTERY STATUS",
                            color = Color(0xFF7DF9FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = if (battery.isCharging) Color(0x3300E676) else Color(0x3300D4FF),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (battery.isCharging) Color(0xFF00E676) else Color(0xFF00D4FF))
                    ) {
                        Text(
                            text = battery.statusText.uppercase(),
                            color = if (battery.isCharging) Color(0xFF00E676) else Color(0xFF7DF9FF),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${battery.percentage}%",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TEMP: ${battery.temperatureCelsius}°C",
                            color = Color(0xFF7DF9FF).copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "CORE: ${battery.voltageMv} mV",
                            color = Color(0xFF7DF9FF).copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val batteryColor = when {
                    battery.percentage > 50 -> Color(0xFF00E676)
                    battery.percentage > 20 -> Color(0xFF00FFFF)
                    else -> Color(0xFFFF5252)
                }
                LinearProgressIndicator(
                    progress = { (battery.percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = batteryColor,
                    trackColor = Color(0xFF102542)
                )
            }
        }

        // 2. FLASHLIGHT BEAM (REAL TORCH TOGGLE WITH ERROR FEEDBACK)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isTorchOn) Color(0xFFFFD700) else Color(0xFF00D4FF).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isTorchOn) Color(0x33FFD700) else Color(0x2200D4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = if (isTorchOn) Color(0xFFFFD700) else Color(0xFF00D4FF)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Flashlight Beam",
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isTorchOn) "ACTIVE (BEAM ON)" else "STANDBY (BEAM OFF)",
                            color = if (isTorchOn) Color(0xFFFFD700) else Color(0xFF7DF9FF).copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
                Switch(
                    checked = isTorchOn,
                    onCheckedChange = {
                        val (success, msg) = viewModel.toggleTorch()
                        if (success) {
                            Toast.makeText(context, "Command Executed: $msg", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Torch not supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFD700),
                        checkedTrackColor = Color(0x66FFD700),
                        uncheckedThumbColor = Color(0xFF7DF9FF),
                        uncheckedTrackColor = Color(0xFF102542)
                    )
                )
            }
        }

        // 3. AUDIO OUTPUT LEVEL: MUTE / 50% / 100% + SLIDER + HAPTIC FEEDBACK
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
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
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF00FFFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUDIO OUTPUT LEVEL",
                            color = Color(0xFF7DF9FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = viewModel.controllerVolume,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real volume interactive slider
                Slider(
                    value = viewModel.volumeSliderValue,
                    onValueChange = {
                        viewModel.setVolumeFromSlider(it)
                    },
                    onValueChangeFinished = {
                        Toast.makeText(context, "Command Executed: Audio Volume ${viewModel.volumeSliderValue.toInt()}%", Toast.LENGTH_SHORT).show()
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00FFFF),
                        activeTrackColor = Color(0xFF00D4FF),
                        inactiveTrackColor = Color(0xFF102542)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // MUTE / 50% / 100% Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("MUTE" to 0, "50%" to 50, "100%" to 100).forEach { (label, pct) ->
                        val isSelected = (pct == 0 && viewModel.controllerVolume == "0%") ||
                                viewModel.controllerVolume.startsWith("$pct%")
                        Button(
                            onClick = {
                                viewModel.setSystemVolume(pct)
                                Toast.makeText(context, "Command Executed: Audio Output $label", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF00D4FF).copy(alpha = 0.35f) else Color(0xFF102542),
                                contentColor = if (isSelected) Color(0xFF00FFFF) else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00FFFF) else Color(0xFF00D4FF).copy(alpha = 0.3f))
                        ) {
                            Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. SYSTEM RINGER MODE: NORMAL / VIBRATE / SILENT (WITH 200MS VIBRATE ON VIBRATE)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
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
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF8A2BE2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RINGER MODE",
                            color = Color(0xFF7DF9FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = currentRingerMode.name,
                        color = Color(0xFFE0B0FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        "NORMAL" to SystemHardwareController.RingerMode.NORMAL,
                        "VIBRATE" to SystemHardwareController.RingerMode.VIBRATE,
                        "SILENT" to SystemHardwareController.RingerMode.SILENT
                    )
                    modes.forEach { (label, mode) ->
                        val isSelected = currentRingerMode == mode
                        Button(
                            onClick = {
                                viewModel.setRingerMode(mode)
                                Toast.makeText(context, "Command Executed: Ringer Mode $label", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF8A2BE2).copy(alpha = 0.4f) else Color(0xFF102542),
                                contentColor = if (isSelected) Color(0xFFE0B0FF) else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF8A2BE2) else Color(0xFF00D4FF).copy(alpha = 0.25f))
                        ) {
                            Text(label, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. DISPLAY LUMINANCE: DIM / NORMAL / MAX (OVERLAY 0.5 FOR DIM, WAKE LOCK FOR MAX)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
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
                            Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = Color(0xFF00FFFF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DISPLAY LUMINANCE",
                            color = Color(0xFF7DF9FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = viewModel.controllerBrightness,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DIM", "NORMAL", "MAX").forEach { b ->
                        val isSelected = viewModel.controllerBrightness == b
                        Button(
                            onClick = {
                                viewModel.setDisplayLuminance(activity, b)
                                Toast.makeText(context, "Command Executed: Luminance $b", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF00D4FF).copy(alpha = 0.35f) else Color(0xFF102542),
                                contentColor = if (isSelected) Color(0xFF00FFFF) else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00FFFF) else Color(0xFF00D4FF).copy(alpha = 0.25f))
                        ) {
                            Text(b, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. CLEAN SPEAKER 165Hz (165Hz SINE WAVE FOR 30s TO EJECT WATER + ANIMATION)
        Surface(
            color = if (isCleaningSpeaker) Color(0xCC1A0033) else Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (isCleaningSpeaker) Color(0xFFFF0055) else Color(0xFF00D4FF).copy(alpha = 0.4f)),
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
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isCleaningSpeaker) Color(0xFFFF0055) else Color(0xFF00FFFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CLEAN SPEAKER 165Hz",
                                color = if (isCleaningSpeaker) Color(0xFFFF5252) else Color(0xFF7DF9FF),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ACOUSTIC WATER & DUST EJECTOR",
                                color = Color(0xFF7DF9FF).copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (isCleaningSpeaker) {
                        Surface(
                            color = Color(0xFFFF0055).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF0055))
                        ) {
                            Text(
                                text = "${cleanSpeakerCountdown}s LEFT",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isCleaningSpeaker) {
                    // Pulsing animated soundwave concentric canvas
                    val infiniteTransition = rememberInfiniteTransition(label = "speaker_anim")
                    val pulseRadius by infiniteTransition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulse_radius"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                color = Color(0xFFFF0055).copy(alpha = (1f - pulseRadius).coerceIn(0f, 1f)),
                                radius = 32.dp.toPx() * pulseRadius,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFF00FFFF).copy(alpha = 0.7f),
                                radius = 10.dp.toPx()
                            )
                        }
                        Text(
                            text = "165 Hz RESONANCE ACTIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (cleanSpeakerCountdown / 30f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFFF0055),
                        trackColor = Color(0xFF102542)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.stopCleanSpeaker()
                            Toast.makeText(context, "Command Executed: Speaker Cleaning Halted", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF0055).copy(alpha = 0.3f),
                            contentColor = Color(0xFFFF5252)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFF0055)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("STOP CLEANING", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.startCleanSpeaker()
                            Toast.makeText(context, "Command Executed: Clean Speaker 165Hz Started", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00D4FF).copy(alpha = 0.25f),
                            contentColor = Color(0xFF00FFFF)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF00D4FF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START CLEAN SPEAKER (30s)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 7. MORE HARDWARE CONTROLS (VIBRATION TEST, SCREEN KEEP AWAKE, COPY DEVICE INFO)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "HARDWARE UTILITIES & DIAGNOSTICS",
                    color = Color(0xFF7DF9FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // 1. Vibration Test
                Button(
                    onClick = {
                        viewModel.triggerVibrationTest()
                        Toast.makeText(context, "Command Executed: Vibration Diagnostics Test Initiated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102542), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF00FFFF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("VIBRATION TEST", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("PULSE // WAVE", color = Color(0xFF7DF9FF).copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    }
                }

                // 2. Screen Keep Awake Toggle
                Button(
                    onClick = {
                        viewModel.toggleScreenKeepAwake(activity)
                        Toast.makeText(context, "Command Executed: Screen Keep Awake ${if (viewModel.isScreenKeepAwake) "ENABLED" else "DISABLED"}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScreenKeepAwake) Color(0xFF00E676).copy(alpha = 0.25f) else Color(0xFF102542),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isScreenKeepAwake) Color(0xFF00E676) else Color(0xFF00D4FF).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = if (isScreenKeepAwake) Color(0xFF00E676) else Color(0xFF00FFFF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SCREEN KEEP AWAKE", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (isScreenKeepAwake) "ACTIVE" else "STANDBY",
                            color = if (isScreenKeepAwake) Color(0xFF00E676) else Color(0xFF7DF9FF).copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Copy Device Info
                Button(
                    onClick = {
                        viewModel.copyDeviceInfo()
                        Toast.makeText(context, "Command Executed: Device Telemetry Copied to Clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102542), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF8A2BE2).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFE0B0FF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("COPY DEVICE INFO", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("RAM // BATTERY // NET", color = Color(0xFFE0B0FF).copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                    }
                }
            }
        }

        // 8. DIRECT APP LAUNCH HUBS (REAL WORKING INTENTS)
        Surface(
            color = Color(0xAA0A1931),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF00D4FF).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DIRECT APP LAUNCH HUBS",
                    color = Color(0xFF7DF9FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                val hubApps = listOf(
                    Triple("CAMERA", Icons.Default.PhotoCamera, Color(0xFF00FFFF)),
                    Triple("GALLERY", Icons.Default.PhotoLibrary, Color(0xFF00E676)),
                    Triple("BROWSER", Icons.Default.Language, Color(0xFF00B0FF)),
                    Triple("DIALER", Icons.Default.Phone, Color(0xFF8A2BE2)),
                    Triple("FILES", Icons.Default.Folder, Color(0xFFFFD700))
                )

                hubApps.forEach { (app, icon, accent) ->
                    Surface(
                        onClick = {
                            val (success, _) = viewModel.launchAppTarget(app)
                            if (success) {
                                Toast.makeText(context, "Command Executed: Launched $app", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Command Executed: Launched $app fallback", Toast.LENGTH_SHORT).show()
                            }
                        },
                        color = Color(0xFF102542),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00D4FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "LAUNCH $app",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF00FFFF), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Settings tab implemented in JarvisSettingsScreen.kt (Variation 01 - Folder Stack)


// ==========================================
// FEATURE 44: FLOATING PULSE MIC BUTTON
// ==========================================
@Composable
fun FloatingPulseMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing neon wave
        Box(
            modifier = Modifier
                .size(54.dp * pulseScale)
                .clip(CircleShape)
                .background(ArcCyanGlow.copy(alpha = pulseAlpha))
        )

        // Floating action button
        FloatingActionButton(
            onClick = onClick,
            containerColor = if (isListening) Color(0xFFFF2A6D) else Color(0xFF001524),
            contentColor = ArcCyan,
            shape = CircleShape,
            modifier = Modifier
                .size(54.dp)
                .border(2.dp, if (isListening) Color(0xFFFF2A6D) else ArcCyan, CircleShape)
                .shadow(elevation = 10.dp, shape = CircleShape, ambientColor = ArcCyan, spotColor = ArcCyanGlow)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Floating Voice Recognition Mic",
                tint = if (isListening) Color.White else ArcCyanGlow,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ==========================================
// FEATURE 36: AUTO REPLY AI SETTINGS CARD
// ==========================================
@Composable
fun JarvisAutoReplyCard(viewModel: JarvisViewModel) {
    val isAutoReplyEnabled = viewModel.isAutoReplyEnabled
    val historyCount = viewModel.autoReplyHistory.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcCyan, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HoloSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = ArcCyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Auto Reply AI (36)",
                            color = ArcCyanGlow,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Notification API Auto-Responder",
                            color = HoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Switch(
                    checked = isAutoReplyEnabled,
                    onCheckedChange = { viewModel.toggleAutoReply(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ArcCyan.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Logic classification explanation
            Text(
                text = "Autonomous Notification Classifier & Response Matrix:",
                color = StarkGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(10.dp)
            ) {
                // Rule 1: Maa / Mom
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "👑 ", fontSize = 12.sp)
                    Column {
                        Text(
                            text = "Sender contains \"Maa\" / \"Mom\":",
                            color = Color(0xFFFF80AB),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"আসসালামু আলাইকুম মা, Boss ekhon busy ache, 20 minute por reply debe - JARVIS\"",
                            color = HoloTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rule 2: Sir / Boss
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💼 ", fontSize = 12.sp)
                    Column {
                        Text(
                            text = "Sender contains \"Sir\" / \"Boss\":",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"Greetings Sir, Boss ekhon busy ache, 30 minute por reply debe - JARVIS\"",
                            color = HoloTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Rule 3: Others
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💬 ", fontSize = 12.sp)
                    Column {
                        Text(
                            text = "Others / Friends:",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"Boss ekhon busy ache, 30 minute por reply debe - JARVIS\"",
                            color = HoloTextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View History & Test Simulation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.selectTab(4) },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ArcCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = ArcCyanGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reply History ($historyCount)",
                        color = ArcCyanGlow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Simulation test buttons
            Text(
                text = "Quick Verification Simulators:",
                color = HoloTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.testSimulateAutoReply("Maa", "Kothay tui baba? Khaaoya hoyeche?", "WhatsApp")
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF80AB).copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Test Maa",
                        color = Color(0xFFFF80AB),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.testSimulateAutoReply("Sir / Team Boss", "Please update me on the project.", "WhatsApp")
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Test Boss",
                        color = ArcCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.testSimulateAutoReply("Tanvir", "Ki khobor bondhu? Free achis?", "Messenger")
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Test Friend",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ==========================================
// FEATURE 44: NOTIFICATION READER SETTINGS CARD
// ==========================================
@Composable
fun JarvisNotificationReaderCard(viewModel: JarvisViewModel) {
    val isReaderEnabled = viewModel.isNotificationReaderEnabled
    val selectedApp = viewModel.readerSelectedApp
    val volume = viewModel.jarvisVoiceVolume
    val appOptions = listOf("All", "WhatsApp", "Messenger", "bKash")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcCyan, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HoloSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = ArcCyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Notification Reader (44)",
                            color = ArcCyanGlow,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Voice Announcer & Direct Reply",
                            color = HoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Switch(
                    checked = isReaderEnabled,
                    onCheckedChange = { viewModel.toggleNotificationReader(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = ArcCyan.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Bangla Speech Announcement template
            Text(
                text = "Bangla Speech Formula:",
                color = StarkGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "\"Boss, [appName] theke message: [messageBody]\"",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Selector
            Text(
                text = "Target App Selector:",
                color = StarkGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                appOptions.forEach { app ->
                    val isSelected = selectedApp == app
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ArcCyan.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                if (isSelected) ArcCyan else ArcCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.selectReaderApp(app) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app,
                            color = if (isSelected) ArcCyanGlow else HoloTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice command instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF002233))
                    .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = ArcCyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voice Command Protocol:",
                        color = ArcCyanGlow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "After reading, speak: \"Reply [message]\" to dispatch a live response via notification action. Operates in background when screen is locked.",
                    color = HoloTextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = StarkGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JARVIS Voice Volume",
                        color = StarkGold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = volume,
                onValueChange = { viewModel.setVoiceVolume(it) },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = ArcCyan,
                    activeTrackColor = ArcCyan,
                    inactiveTrackColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Test button (Silent check - zero voice on click)
            OutlinedButton(
                onClick = {
                    val app = if (selectedApp == "All") "WhatsApp" else selectedApp
                    viewModel.logAction("Notification Reader Checked: $app")
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ArcCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ArcCyanGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Audio Announcement",
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// TAB 4: REPLY HISTORY SCREEN
// ==========================================
@Composable
fun JarvisReplyHistoryScreen(viewModel: JarvisViewModel) {
    val history = viewModel.autoReplyHistory
    val totalCount = history.size
    val respectfulCount = history.count { it.category.contains("Maa", ignoreCase = true) }
    val formalCount = history.count { it.category.contains("Sir", ignoreCase = true) }
    val generalCount = totalCount - (respectfulCount + formalCount)
    val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTO REPLY AI // TRANSMISSION LOG",
                    color = ArcCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time Notification Action Records",
                    color = HoloTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAutoReplyHistory() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = ArcCyan.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Stat Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Sent
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloSurface)
                    .border(1.dp, ArcCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalCount",
                        color = ArcCyanGlow,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Sent",
                        color = HoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Respectful (Maa)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloSurface)
                    .border(1.dp, Color(0xFFFF80AB).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$respectfulCount",
                        color = Color(0xFFFF80AB),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Maa/Respect",
                        color = HoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Formal (Sir)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloSurface)
                    .border(1.dp, StarkGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$formalCount",
                        color = StarkGold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sir/Formal",
                        color = HoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // General
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloSurface)
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$generalCount",
                        color = Color(0xFF00E676),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "General",
                        color = HoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // History items list or empty state
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HoloSurface)
                    .border(1.dp, ArcCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = ArcCyan.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Auto-Replies Dispatched Yet",
                        color = ArcCyan,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Turn ON 'Auto Reply AI (36)' in Settings or trigger a test simulator to verify instant autonomous replies.",
                        color = HoloTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.testSimulateAutoReply("Maa", "Ammi phone korlam dhorli na ken?", "WhatsApp")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, ArcCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Simulate Incoming Test Reply",
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HoloSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Top row: App name, Category & Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // App Tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ArcCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = record.appName,
                                            color = ArcCyanGlow,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Category Tag
                                    val catColor = when {
                                        record.category.contains("Maa") -> Color(0xFFFF80AB)
                                        record.category.contains("Sir") -> StarkGold
                                        record.category.contains("Voice") -> Color(0xFF00E5FF)
                                        else -> Color(0xFF00E676)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(catColor.copy(alpha = 0.15f))
                                            .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = record.category,
                                            color = catColor,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Text(
                                    text = dateFormat.format(Date(record.timestamp)),
                                    color = HoloTextSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sender & Incoming Text
                            Text(
                                text = "FROM: ${record.sender}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            if (record.originalText.isNotBlank()) {
                                Text(
                                    text = "\"${record.originalText}\"",
                                    color = HoloTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Auto-reply box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF001F2D))
                                    .border(1.dp, ArcCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = null,
                                            tint = ArcCyanGlow,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "JARVIS AUTO-REPLY:",
                                            color = ArcCyanGlow,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = record.replySent,
                                        color = HoloTextPrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
