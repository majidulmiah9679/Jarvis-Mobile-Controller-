package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.HoloDarkBg
import com.example.ui.theme.HoloSurface
import com.example.ui.theme.HoloSurfaceElevated
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary
import com.example.ui.theme.StarkGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Voice Enrollment & Biometric Calibration Card ("Amar Voice" / My Voice)
 * Allows user to calibrate & save their vocal signature so JARVIS only obeys their voice.
 */
@Composable
fun VoiceBiometricEnrollmentCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    AmarVoiceLockCard(viewModel = viewModel, modifier = modifier)
}

/**
 * Live Talk Floating Round Action Pill ("Live kotha bolar option side gol round kore")
 */
@Composable
fun LiveTalkRoundFloatingBar(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val isLiveOrbActive = viewModel.isLiveOrbActive
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Surface(
        onClick = { viewModel.toggleLiveOrb() },
        color = HoloSurfaceElevated.copy(alpha = 0.95f),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 8.dp,
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.5.dp,
                color = if (isLiveOrbActive) ArcCyanGlow else ArcCyan.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Pulsing round dot / icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isLiveOrbActive) Color.Red else ArcCyan.copy(alpha = 0.25f))
                    .border(1.dp, if (isLiveOrbActive) Color.White else ArcCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isLiveOrbActive) Color.White else ArcCyanGlow,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "LIVE TALK",
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isLiveOrbActive) "TAP TO CLOSE" else "LIVE KOTHA BOLUN",
                    color = HoloTextSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.GraphicEq,
                contentDescription = null,
                tint = if (isLiveOrbActive) ArcCyanGlow else HoloTextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Full-screen / Modal Rounded Live Talk Orb Dialog
 * Displays a glowing round orb with real-time waveform pulses for natural duplex speaking.
 */
@Composable
fun LiveTalkRoundOrbDialog(
    viewModel: JarvisViewModel,
    onDismissRequest: () -> Unit
) {
    val isListening = viewModel.isListening
    val isSpeaking = viewModel.isSpeaking
    val liveRms = viewModel.liveAudioRms
    val isDuplex = viewModel.isDuplexEnabled
    val lastLog = viewModel.coreLog
    val isAudioActive = isListening || isSpeaking || isDuplex

    val infiniteTransition = rememberInfiniteTransition(label = "orb_arc_reactor")
    val statorRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "stator_rot"
    )
    val vernierRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "vernier_rot"
    )
    val radarSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "radar_sweep"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isAudioActive) 350 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val arcSweepBrush = remember {
        Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x1500E5FF),
                Color(0x4500E5FF),
                Color(0xBB00FFFF)
            )
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .background(HoloSurface.copy(alpha = 0.96f))
                    .border(2.dp, ArcCyan.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                // Header with rounded pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ArcCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyan),
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) ArcCyan else if (isListening) Color(0xFF00E676) else Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpeaking) "JARVIS TRANSMITTING // ACTIVE" else if (isListening) "VOICE RECEIVER // LISTENING" else "LIVE TALK // 2-WAY DUPLEX",
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Interactive Real Arc Reactor Engine with Live Audio Spectrum & Commander Photo
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clickable {
                            viewModel.toggleListeningState()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val maxR = size.minDimension / 2f
                        val photoRadius = 48.dp.toPx()

                        // 1. Holographic Radar Sweep Beam with Phosphor Trail
                        rotate(radarSweep, c) {
                            drawArc(
                                brush = arcSweepBrush,
                                startAngle = 0f,
                                sweepAngle = 42f,
                                useCenter = true,
                                size = Size(maxR * 1.9f, maxR * 1.9f),
                                topLeft = Offset(c.x - maxR * 0.95f, c.y - maxR * 0.95f)
                            )
                            val sweepRad = Math.toRadians(42.0)
                            drawLine(
                                color = Color(0xFF00FFFF),
                                start = c,
                                end = Offset(
                                    (c.x + maxR * 0.95f * cos(sweepRad)).toFloat(),
                                    (c.y + maxR * 0.95f * sin(sweepRad)).toFloat()
                                ),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }

                        // 2. Outer Precision Reticle
                        drawCircle(
                            color = ArcCyan.copy(alpha = 0.35f),
                            radius = maxR * 0.95f,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // 3. 12 Segmented Arc Reactor Electromagnet Coils (Stator Ring)
                        val statorRadius = maxR * 0.85f
                        val statorWidth = 4.5.dp.toPx()
                        rotate(statorRotation, c) {
                            for (i in 0 until 12) {
                                val startDeg = i * 30f + 4f
                                val coilActive = isAudioActive || (((statorRotation + i * 30f) % 360f) < 85f)
                                val coilColor = if (coilActive) Color(0xFF00FFFF) else Color(0xFF00838F).copy(alpha = 0.45f)
                                drawArc(
                                    color = coilColor,
                                    startAngle = startDeg,
                                    sweepAngle = 22f,
                                    useCenter = false,
                                    topLeft = Offset(c.x - statorRadius, c.y - statorRadius),
                                    size = Size(statorRadius * 2f, statorRadius * 2f),
                                    style = Stroke(width = statorWidth)
                                )
                            }
                        }

                        // 4. Counter-Rotating Inner Vernier Dial with 24 Precision Ticks
                        val vernierRadius = maxR * 0.72f
                        rotate(vernierRotation, c) {
                            drawCircle(
                                color = StarkGold.copy(alpha = 0.4f),
                                radius = vernierRadius,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            for (i in 0 until 24) {
                                val rad = Math.toRadians(i * 15.0)
                                val isMajor = (i % 6 == 0)
                                val tickLen = if (isMajor) 4.dp.toPx() else 2.5.dp.toPx()
                                val innerP = Offset(
                                    (c.x + (vernierRadius - tickLen) * cos(rad)).toFloat(),
                                    (c.y + (vernierRadius - tickLen) * sin(rad)).toFloat()
                                )
                                val outerP = Offset(
                                    (c.x + (vernierRadius + tickLen) * cos(rad)).toFloat(),
                                    (c.y + (vernierRadius + tickLen) * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (isMajor) StarkGold else ArcCyan.copy(alpha = 0.5f),
                                    start = innerP,
                                    end = outerP,
                                    strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                                )
                            }
                        }

                        // 5. Real Radial Audio Spectrum Visualizer (32 Voice Frequency Bars)
                        val barCount = 32
                        val eqBaseRadius = photoRadius + 3.dp.toPx()
                        for (i in 0 until barCount) {
                            val angleDeg = (i * (360f / barCount)) + (statorRotation * 0.4f)
                            val rad = Math.toRadians(angleDeg.toDouble())

                            val dynamicWave = sin((i * 0.65f + radarSweep * 0.12f).toDouble()).toFloat()
                            val barLen = if (isAudioActive) {
                                (3.dp.toPx() + (liveRms * 20.dp.toPx()) + (dynamicWave.coerceAtLeast(0f) * 10.dp.toPx()))
                            } else {
                                (2.dp.toPx() + ((dynamicWave + 1f) * 1.5.dp.toPx()))
                            }

                            val pStart = Offset(
                                (c.x + eqBaseRadius * cos(rad)).toFloat(),
                                (c.y + eqBaseRadius * sin(rad)).toFloat()
                            )
                            val pEnd = Offset(
                                (c.x + (eqBaseRadius + barLen) * cos(rad)).toFloat(),
                                (c.y + (eqBaseRadius + barLen) * sin(rad)).toFloat()
                            )

                            val barColor = if (isSpeaking) {
                                Color(0xFF00FFFF)
                            } else if (isListening) {
                                Color(0xFF00E676)
                            } else {
                                ArcCyan.copy(alpha = 0.55f)
                            }

                            drawLine(
                                color = barColor,
                                start = pStart,
                                end = pEnd,
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // 6. Central Arc Energy Resonance Wave
                        if (isAudioActive) {
                            drawCircle(
                                color = (if (isSpeaking) Color(0xFF00FFFF) else Color(0xFF00E676)).copy(alpha = 0.3f),
                                radius = (photoRadius + 5.dp.toPx()) * pulseScale,
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                    }

                    // CENTER PHOTO: Embedded inside the focal aperture of the Arc Reactor
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF030712))
                            .border(
                                BorderStroke(
                                    2.dp,
                                    Brush.sweepGradient(
                                        listOf(ArcCyan, StarkGold, ArcCyanGlow, ArcCyan)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUri = viewModel.userAvatarUriString
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Commander Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.22f)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_commander_photo),
                                contentDescription = "Commander Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.22f)
                            )
                        }

                        // Sci-fi holographic sheen overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            ArcCyan.copy(alpha = 0.10f),
                                            Color(0xFF020716).copy(alpha = 0.38f)
                                        )
                                    )
                                )
                        )

                        // Center / Bottom status chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xEE03091B),
                            border = BorderStroke(
                                0.8.dp,
                                if (isSpeaking) Color(0xFF00FFFF) else if (isListening) Color(0xFF00E676) else ArcCyanGlow
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isSpeaking) Color(0xFF00FFFF) else if (isListening) Color(0xFF00E676) else ArcCyanGlow,
                                    modifier = Modifier.size(9.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSpeaking) "JARVIS TX" else if (isListening) "LISTENING" else "SPEAK",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Live kotha bolun, JARVIS shunchhe...",
                    color = ArcCyanGlow,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (viewModel.voiceProfileState != null) "🔒 Protected: Obeying only your voice" else "🔓 Open Mic: Any speaker accepted",
                    color = if (viewModel.voiceProfileState != null) ArcCyan else StarkGold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Live Feedback / Transcript
                Surface(
                    color = HoloDarkBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, ArcCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lastLog.takeLast(160),
                            color = HoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Round Action Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Barge-in / Duplex Button (Rounded)
                    OutlinedButton(
                        onClick = { viewModel.toggleDuplexMode() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDuplex) ArcCyanGlow else Color.LightGray
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDuplex) ArcCyan else Color.DarkGray
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            if (isDuplex) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isDuplex) "Duplex ON" else "Duplex OFF",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    // Close Button (Rounded)
                    Button(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Close Live Talk", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
