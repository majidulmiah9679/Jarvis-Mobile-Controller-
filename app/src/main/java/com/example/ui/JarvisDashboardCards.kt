package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow

// Shared Glassmorphism Colors
val DarkGlassSurface = Color(0xCC060E20)
val NeonCyanBorder = Color(0xFF00E5FF)
val ElectricVioletBorder = Color(0xFF9D4EDD)
val GlowPurple = Color(0xFF8A2BE2)

/**
 * 1. 'AMAR VOICE (MY VOICE LOCK)' security panel
 * Features enrolled biometric status, glowing microphone 'Re-calibrating' button, and floating 'LIVE TALK' badge.
 */
@Composable
fun AmarVoiceLockCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val isEnrolling = viewModel.isEnrollingVoice
    val profile = viewModel.voiceProfileState
    val isVoiceLockEnabled = profile?.voiceLockEnabled ?: true
    val statusText = viewModel.enrollmentStatusText

    var showNameDialog by remember { mutableStateOf(false) }
    var tempOwnerName by remember { mutableStateOf(profile?.ownerName ?: "মজিদুল বস (Boss)") }
    var testResultText by remember { mutableStateOf("") }

    if (showNameDialog) {
        Dialog(onDismissRequest = { showNameDialog = false }) {
            Surface(
                color = Color(0xFF071228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, NeonCyanBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VOICE OWNER SETTINGS",
                        color = NeonCyanBorder,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কণ্ঠের মালিকের নাম দিন যার ভয়েস এখানে সেভ থাকবে:",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempOwnerName,
                        onValueChange = { tempOwnerName = it },
                        label = { Text("Owner Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyanBorder,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showNameDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateVoiceOwnerName(tempOwnerName)
                                showNameDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBorder, contentColor = Color.Black)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.85f), ElectricVioletBorder.copy(alpha = 0.7f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row with Biometric Icon, Title, and Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(NeonCyanBorder.copy(alpha = 0.35f), Color.Transparent)))
                            .border(1.2.dp, NeonCyanBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = NeonCyanBorder,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AMAR VOICE (MY VOICE LOCK)",
                            color = Color(0xFFF1F8FD),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (profile != null && profile.enrolled) "Calibrated & Secured: ${profile.ownerName}" else "Not Enrolled (Open Mode)",
                            color = if (profile != null && profile.enrolled) Color(0xFF80D8FF) else Color(0xFFFFD54F),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Voice Lock Active Switch
                Switch(
                    checked = isVoiceLockEnabled,
                    onCheckedChange = { checked ->
                        viewModel.toggleVoiceLock(checked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF030A18),
                        checkedTrackColor = NeonCyanBorder,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Explanation box in Banglish & English
            Surface(
                color = Color(0xFF040B1A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, NeonCyanBorder.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "🔒 ভয়েস সিকিউরিটি পলিসি:",
                        color = NeonCyanBorder,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "শুধুমাত্র ${profile?.ownerName ?: "আপনার"} কণ্ঠে জারভিস কথা শুনবে এবং ফোন কন্ট্রোল করবে। অন্য কারও ভয়েস স্বয়ংক্রিয়ভাবে ব্লক ও রিজেক্ট হবে।",
                        color = Color(0xFFC0D8F0),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            if (statusText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF0A1E3C),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusText,
                        color = if (isEnrolling) NeonCyanBorder else Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (testResultText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = if (testResultText.contains("MATCH") || testResultText.contains("verified") || testResultText.contains("authorized", ignoreCase = true)) Color(0xFF063322) else Color(0xFF3B0B14),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = testResultText,
                        color = if (testResultText.contains("MATCH") || testResultText.contains("verified") || testResultText.contains("authorized", ignoreCase = true)) Color(0xFF69F0AE) else Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enroll / Re-calibrate Voice Button
                Button(
                    onClick = {
                        viewModel.startVoiceprintEnrollment(profile?.ownerName ?: "মজিদুল বস")
                    },
                    enabled = !isEnrolling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyanBorder,
                        contentColor = Color(0xFF030A18)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).height(40.dp)
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(color = Color(0xFF030A18), strokeWidth = 2.dp, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Listening 3s...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(if (profile == null || !profile.enrolled) "ভয়েস অ্যাড করো" else "Re-Calibrate Voice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Edit Owner Name Button
                OutlinedButton(
                    onClick = {
                        tempOwnerName = profile?.ownerName ?: "মজিদুল বস"
                        showNameDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, NeonCyanBorder.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নাম পরিবর্তন", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verification & Security Testing Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test My Voice (Live Speaker Match)
                OutlinedButton(
                    onClick = {
                        testResultText = "Testing voice match... Speak now for 2s."
                        viewModel.testVoiceBiometrics { result ->
                            testResultText = result.message
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyanBorder),
                    border = BorderStroke(1.dp, NeonCyanBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ভয়েস টেস্ট", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Test Block Other Voice
                OutlinedButton(
                    onClick = {
                        testResultText = "SECURITY ALERT: Simulated non-owner voice command blocked!"
                        viewModel.simulateUnauthorizedVoiceTest()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("অন্য ভয়েস টেস্ট", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Reset Button
                if (profile != null && profile.enrolled) {
                    IconButton(
                        onClick = {
                            viewModel.resetVoiceprint()
                            testResultText = "Voiceprint reset."
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Reset", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/**
 * 2. 'Voice Assistant Settings' card
 * Features a miniature glowing Arc Reactor icon, status 'Active: JARVIS', and 'Configure' button.
 */
@Composable
fun VoiceAssistantSettingsCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_glow")
    val arcPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "arc_pulse"
    )

    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.5f), ElectricVioletBorder.copy(alpha = 0.8f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Miniature Glowing Arc Reactor Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF040B18))
                        .border(1.5.dp, NeonCyanBorder.copy(alpha = arcPulse), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2f
                        // Outer glowing ring
                        drawCircle(color = NeonCyanBorder.copy(alpha = arcPulse), radius = r, style = Stroke(width = 2.dp.toPx()))
                        // Inner reactor triangle / coils
                        drawCircle(color = Color(0xFF00FFFF), radius = r * 0.45f)
                        drawCircle(color = Color.White, radius = r * 0.2f)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "VOICE ASSISTANT SETTINGS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active: JARVIS (${viewModel.selectedVoiceName})",
                        color = NeonCyanBorder,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Neural Core: Gemini Flash • Bengali/English",
                        color = Color(0xFF90A4AE),
                        fontSize = 10.sp
                    )
                }
            }

            // 'Configure' Action Button
            Button(
                onClick = { viewModel.showVoiceConfigDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricVioletBorder),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Configure", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 3. 'Star Chart & Navigation' card
 * Features a sleek holographic compass icon, coordinates, and 'Open Map' button.
 */
@Composable
fun StarChartNavigationCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val compassRot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24000, easing = LinearEasing), RepeatMode.Restart),
        label = "compass_rot"
    )

    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.7f), ElectricVioletBorder.copy(alpha = 0.5f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Holographic Compass Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF07142A))
                            .border(1.2.dp, NeonCyanBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(28.dp)) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            rotate(compassRot, c) {
                                drawCircle(color = NeonCyanBorder.copy(alpha = 0.4f), radius = size.minDimension / 2.2f, style = Stroke(1.5f))
                                // Compass needle
                                val p = Path().apply {
                                    moveTo(c.x, c.y - 12f)
                                    lineTo(c.x + 4f, c.y)
                                    lineTo(c.x, c.y + 12f)
                                    lineTo(c.x - 4f, c.y)
                                    close()
                                }
                                drawPath(p, color = NeonCyanBorder)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "STAR CHART & NAVIGATION",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "COSMIC CELESTIAL ALIGNMENT",
                            color = NeonCyanBorder,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { viewModel.openStarChartMap() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF030A18), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Map", color = Color(0xFF030A18), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates Box
            Surface(
                color = Color(0xFF040A18),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, NeonCyanBorder.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ASTRONOMICAL (ORION HUB)", color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text("RA 05h 35m / DEC -05° 23'", color = Color(0xFFE2F1FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color(0xFF1E3A5F))
                    Column {
                        Text("GEODETIC POSITION", color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text("23.8103° N, 90.4125° E", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 4. 'Historical Threat Logs' card
 * Features a glowing cyber shield icon and mini visual analytics graph.
 */
@Composable
fun HistoricalThreatLogsCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF00E676).copy(alpha = 0.7f), NeonCyanBorder.copy(alpha = 0.5f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676).copy(alpha = 0.15f))
                            .border(1.2.dp, Color(0xFF00E676), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HISTORICAL THREAT LOGS",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0 BREACHES • 14 REPELLED • 24H SCAN",
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("100% SECURE", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini Visual Analytics Graph (Step Histogram of threat activity)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF030712))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val barCount = 18
                val barWidth = size.width / (barCount * 1.5f)
                val heights = listOf(0.15f, 0.25f, 0.4f, 0.2f, 0.1f, 0.6f, 0.35f, 0.2f, 0.45f, 0.1f, 0.2f, 0.75f, 0.3f, 0.15f, 0.4f, 0.25f, 0.1f, 0.3f)
                for (i in 0 until barCount) {
                    val x = i * (barWidth * 1.5f)
                    val h = size.height * (heights.getOrElse(i) { 0.2f })
                    drawRoundRect(
                        color = if (i == 11) Color(0xFFFF5252) else Color(0xFF00E676),
                        topLeft = Offset(x, size.height - h),
                        size = androidx.compose.ui.geometry.Size(barWidth, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                }
            }
        }
    }
}

/**
 * 5. 'Node Network Advanced' card
 * Features 3D mesh network icon, live latency indicators, and 'Diagnose Network' action button.
 */
@Composable
fun NodeNetworkAdvancedCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val meshAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Restart),
        label = "mesh_angle"
    )

    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(ElectricVioletBorder.copy(alpha = 0.8f), NeonCyanBorder.copy(alpha = 0.6f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 3D Mesh Network Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricVioletBorder.copy(alpha = 0.2f))
                            .border(1.2.dp, ElectricVioletBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            rotate(meshAngle, c) {
                                // 3 nodes connected
                                val n1 = Offset(c.x, c.y - 8f)
                                val n2 = Offset(c.x - 7f, c.y + 6f)
                                val n3 = Offset(c.x + 7f, c.y + 6f)
                                drawLine(ElectricVioletBorder, n1, n2, strokeWidth = 1.5f)
                                drawLine(ElectricVioletBorder, n2, n3, strokeWidth = 1.5f)
                                drawLine(ElectricVioletBorder, n3, n1, strokeWidth = 1.5f)
                                drawCircle(Color.White, 2.5f, n1)
                                drawCircle(Color.White, 2.5f, n2)
                                drawCircle(Color.White, 2.5f, n3)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "NODE NETWORK ADVANCED",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "QUANTUM MESH LATTICE • 12 NODES",
                            color = ElectricVioletBorder,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { viewModel.diagnoseNetwork() },
                    enabled = !viewModel.isDiagnosingNetwork,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricVioletBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (viewModel.isDiagnosingNetwork) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scanning...", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Diagnose", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Latency Indicators Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF040A18))
                    .border(1.dp, ElectricVioletBorder.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LATENCY", color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text("18ms", color = NeonCyanBorder, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(22.dp).width(1.dp), color = Color(0xFF1E3A5F))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BANDWIDTH", color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text("1.2 Gbps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Divider(modifier = Modifier.height(22.dp).width(1.dp), color = Color(0xFF1E3A5F))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PACKET LOSS", color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                    Text("0.0%", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 6. 'Account & Permissions' with biometric profile icon and security level badge.
 */
@Composable
fun AccountPermissionsCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.6f), ElectricVioletBorder.copy(alpha = 0.6f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Biometric Profile Avatar Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(NeonCyanBorder.copy(alpha = 0.4f), Color(0xFF091830))))
                        .border(1.2.dp, NeonCyanBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "ACCOUNT & PERMISSIONS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Commander Majidul • Root Access",
                        color = NeonCyanBorder,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Mic • System Settings • Accessibility: ACTIVE",
                        color = Color(0xFF88B2D6),
                        fontSize = 9.sp
                    )
                }
            }

            // Security Level Badge
            Surface(
                color = Color(0xFF1B1438),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ElectricVioletBorder)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CLEARANCE", color = Color(0xFFBDD8F0), fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    Text("ALPHA-5", color = ElectricVioletBorder, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * 7. 'AR HUD Layout' customization card with holographic overlay controls.
 */
@Composable
fun ArHudLayoutCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.7f), ElectricVioletBorder.copy(alpha = 0.5f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NeonCyanBorder.copy(alpha = 0.15f))
                            .border(1.2.dp, NeonCyanBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = NeonCyanBorder,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AR HUD LAYOUT",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "HOLOGRAPHIC OVERLAY CALIBRATION",
                            color = NeonCyanBorder,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Preset selector button
                Surface(
                    onClick = {
                        viewModel.arHudPreset = when (viewModel.arHudPreset) {
                            "Tactical" -> "Minimal"
                            "Minimal" -> "Full Orbit"
                            else -> "Tactical"
                        }
                        viewModel.logAction("AR HUD: Preset switched to ${viewModel.arHudPreset}")
                    },
                    color = Color(0xFF071B38),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonCyanBorder.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = viewModel.arHudPreset.uppercase(),
                        color = NeonCyanBorder,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Holographic Overlay Toggles Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArHudChip(
                    label = "Target Reticle",
                    active = viewModel.arReticleEnabled,
                    onClick = { viewModel.arReticleEnabled = !viewModel.arReticleEnabled },
                    modifier = Modifier.weight(1f)
                )
                ArHudChip(
                    label = "Horizon Level",
                    active = viewModel.arHorizonEnabled,
                    onClick = { viewModel.arHorizonEnabled = !viewModel.arHorizonEnabled },
                    modifier = Modifier.weight(1f)
                )
                ArHudChip(
                    label = "Eye-Tracking",
                    active = viewModel.arEyeTrackingEnabled,
                    onClick = { viewModel.arEyeTrackingEnabled = !viewModel.arEyeTrackingEnabled },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ArHudChip(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        color = if (active) NeonCyanBorder.copy(alpha = 0.15f) else Color(0xFF030712),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (active) NeonCyanBorder else Color.DarkGray),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = if (active) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            maxLines = 1
        )
    }
}

/**
 * 8. 'Cross-Device Sync' card with active cloud synchronization status.
 */
@Composable
fun CrossDeviceSyncCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(ElectricVioletBorder.copy(alpha = 0.7f), NeonCyanBorder.copy(alpha = 0.5f)))),
        modifier = modifier.fillMaxWidth()
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
                        .background(ElectricVioletBorder.copy(alpha = 0.2f))
                        .border(1.2.dp, ElectricVioletBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = ElectricVioletBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "CROSS-DEVICE SYNC",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stark Cloud Matrix • AES-256 GCM",
                        color = ElectricVioletBorder,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Last synced: ${viewModel.lastSyncTimestamp} (Armor + Watch)",
                        color = Color(0xFF88B2D6),
                        fontSize = 10.sp
                    )
                }
            }

            Button(
                onClick = { viewModel.triggerCloudSync() },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricVioletBorder),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Sync Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 9. 'Power Core Status' card with battery arc-energy meter and 'Optimize' button.
 */
@Composable
fun PowerCoreStatusCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val batStatus = viewModel.batteryStatus
    val infiniteTransition = rememberInfiniteTransition(label = "power_arc")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse_glow"
    )

    Surface(
        color = DarkGlassSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(NeonCyanBorder.copy(alpha = 0.8f), Color(0xFF00E676).copy(alpha = 0.6f)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Battery Arc-Energy Meter Gauge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF040A18))
                            .border(1.2.dp, NeonCyanBorder.copy(alpha = pulseGlow), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (batStatus.isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (batStatus.isCharging) Color(0xFFFFD54F) else NeonCyanBorder,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "POWER CORE STATUS",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${batStatus.percentage}% ARC OUTPUT • 4.18V",
                            color = NeonCyanBorder,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Est. Runtime: 19h 42m • Temp: ${batStatus.temperatureCelsius}°C",
                            color = Color(0xFF88B2D6),
                            fontSize = 10.sp
                        )
                    }
                }

                Button(
                    onClick = { viewModel.optimizePowerCore() },
                    enabled = !viewModel.isPowerOptimizing,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyanBorder),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (viewModel.isPowerOptimizing) {
                        CircularProgressIndicator(color = Color(0xFF030A18), strokeWidth = 2.dp, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Balancing...", color = Color(0xFF030A18), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Optimize", color = Color(0xFF030A18), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arc-Energy Meter Bar
            LinearProgressIndicator(
                progress = { batStatus.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (batStatus.percentage < 20) Color(0xFFFF5252) else NeonCyanBorder,
                trackColor = Color(0xFF07142A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Auto Battery Saver Brain Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (viewModel.isAutoBatterySaverEnabled) "⚡ AUTO SAVER ACTIVE (<15% → 20% Brightness & TTS)" else "⚡ AUTO SAVER DISABLED",
                    color = if (viewModel.isAutoBatterySaverEnabled) NeonCyanBorder else Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "TEST <15%",
                    color = Color(0xFFFFB300),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.simulateLowBatterySaver() }
                )
            }
        }
    }
}

/**
 * Voice Configuration Modal Dialog
 */
@Composable
fun VoiceConfigDialog(viewModel: JarvisViewModel) {
    Dialog(onDismissRequest = { viewModel.showVoiceConfigDialog = false }) {
        Surface(
            color = Color(0xFF071224),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, NeonCyanBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICE ASSISTANT SELECTOR",
                        color = NeonCyanBorder,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.showVoiceConfigDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                viewModel.voices.take(6).forEach { voice ->
                    val isSelected = viewModel.selectedVoiceName == voice.name
                    Surface(
                        onClick = {
                            viewModel.selectVoiceProfile(voice.name)
                            viewModel.showVoiceConfigDialog = false
                        },
                        color = if (isSelected) NeonCyanBorder.copy(alpha = 0.2f) else Color(0xFF030A18),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) NeonCyanBorder else Color.DarkGray),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(voice.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyanBorder, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
