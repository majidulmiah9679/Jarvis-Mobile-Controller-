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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.StarkGold

/**
 * 4. THEFT ALARM (Lite) Card:
 * Switch for Theft Guard ON/OFF.
 * Listens to accelerometer movement (delta > 5.0) and charger unplug.
 * Plays loud alarm sound + flashlight strobe + TTS "Alert! Don't touch Stark Tech!".
 * Stop alarm button + voice command "Stop Alarm" support.
 */
@Composable
fun TheftGuardCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isArmed = viewModel.isTheftGuardActive
    val isAlarming = viewModel.isTheftAlarmTriggered

    val infiniteTransition = rememberInfiniteTransition(label = "theft_alarm_pulse")
    val alarmPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "alarm_pulse"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isAlarming -> Color(0xFFFF1744)
            isArmed -> ArcCyan
            else -> Color(0xFF1E3A5F)
        },
        label = "theft_card_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.2.dp, cardBorderColor, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlarming) Color(0xFF2B0A11) else Color(0xEE051326)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title Row with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .scale(if (isAlarming) alarmPulse else 1f)
                            .clip(CircleShape)
                            .background(if (isAlarming) Color(0xFFFF1744) else if (isArmed) ArcCyan.copy(alpha = 0.2f) else Color(0xFF0F253E))
                            .border(1.dp, if (isAlarming) Color.White else if (isArmed) ArcCyan else Color(0xFF2A4A6F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAlarming) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = "Theft Guard",
                            tint = if (isAlarming) Color.White else if (isArmed) ArcCyan else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "THEFT GUARD // MOTION SENSORS",
                            color = if (isAlarming) Color(0xFFFF5252) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isAlarming) "⚠️ BREACH DETECTED! ALARM SOUNDING" else if (isArmed) "ARMED • ACCELEROMETER & CHARGER DETECT" else "DISARMED • SENSORS STANDBY",
                            color = if (isAlarming) Color(0xFFFF5252) else if (isArmed) ArcCyanGlow else Color(0xFF88A2BF),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Switch(
                    checked = isArmed,
                    onCheckedChange = { viewModel.toggleTheftGuard(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF0F253E)
                    )
                )
            }

            // Alarming Banner if active
            AnimatedVisibility(visible = isAlarming) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF500914))
                        .border(1.dp, Color(0xFFFF1744), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚨 INTRUDER / MOVEMENT TRIGGERED! 🚨",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Flashing Strobe • Loud Siren • Voice Loop: 'Alert! Don't touch Stark Tech!'",
                        color = Color(0xFFFFCDD2),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.stopTheftAlarm() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "STOP ALARM (VOICE: 'STOP ALARM')",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Control Buttons: Simulate Theft & Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.simulateTheft() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F253E)),
                    border = BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SIMULATE THEFT",
                        fontSize = 10.sp,
                        color = Color(0xFFFF9100),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isArmed && !isAlarming) {
                    Button(
                        onClick = { viewModel.toggleTheftGuard(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002238)),
                        border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "DISARM GUARD",
                            fontSize = 10.sp,
                            color = ArcCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. SMART CLIPBOARD (Lite) Card:
 * Listens to clipboard copies.
 * Offers "Save it" command -> Room DB "clips" table.
 * Offers "Paste last clip" command -> Copies last saved clip back to clipboard.
 * Shows last 3 clips in a small horizontal list.
 */
@Composable
fun SmartClipboardCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val lastCopied = viewModel.lastCopiedText
    val recentClips = viewModel.recentClips

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE051326)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
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
                            .background(ArcCyan.copy(alpha = 0.15f))
                            .border(1.dp, ArcCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Clipboard",
                            tint = ArcCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "SMART CLIPBOARD // ROOM DB",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Say: 'Save it' or 'Paste last clip'",
                            color = ArcCyanGlow,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    color = Color(0xFF00291D),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E676))
                ) {
                    Text(
                        text = "${recentClips.size} CLIPS SAVED",
                        color = Color(0xFF00E676),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Active buffer banner if text copied
            if (!lastCopied.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0A223D),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BUFFER: \"${if (lastCopied.length > 35) lastCopied.take(35) + "..." else lastCopied}\"",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Say 'Save it Boss' or tap button to commit",
                                color = ArcCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { viewModel.saveClipboardToMemory() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00557A)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("SAVE IT", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-header for Last 3 Clips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LAST 3 SAVED CLIPS (HORIZONTAL VAULT):",
                    color = Color(0xFF88A8C9),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "PASTE LAST CLIP",
                    color = ArcCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.pasteLastClipToSystem() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal List of Last 3 Clips
            if (recentClips.isEmpty()) {
                Surface(
                    color = Color(0xFF08192D),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No clips saved yet. Copy text anywhere and say 'Save it' to store.",
                        color = Color(0xFF6B8BAA),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentClips.take(3).forEachIndexed { index, clip ->
                        Surface(
                            color = Color(0xFF081F38),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .width(200.dp)
                                .clickable { viewModel.copyClipToClipboard(clip.text) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${index + 1} CLIP",
                                        color = StarkGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy clip",
                                        tint = ArcCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = clip.text,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "TAP TO COPY",
                                    color = ArcCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
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
