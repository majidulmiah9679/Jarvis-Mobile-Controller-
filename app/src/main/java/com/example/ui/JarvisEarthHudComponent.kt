package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * Deep Space Midnight Starfield & Cosmic Nebulae Background
 */
@Composable
fun DeepSpaceCosmicBackground(
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!isDark) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F7FB),
                            Color(0xFFEDF2F9),
                            Color(0xFFF5F7FB)
                        )
                    )
                )
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle"
    )

    val starCoords = remember {
        listOf(
            Offset(0.12f, 0.08f), Offset(0.35f, 0.05f), Offset(0.78f, 0.12f),
            Offset(0.92f, 0.07f), Offset(0.05f, 0.25f), Offset(0.22f, 0.32f),
            Offset(0.88f, 0.28f), Offset(0.48f, 0.18f), Offset(0.68f, 0.38f),
            Offset(0.15f, 0.55f), Offset(0.82f, 0.62f), Offset(0.95f, 0.72f),
            Offset(0.32f, 0.78f), Offset(0.08f, 0.85f), Offset(0.62f, 0.90f)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Deep space midnight base gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020612),
                    Color(0xFF060E22),
                    Color(0xFF09142E),
                    Color(0xFF030816)
                )
            )
        )

        // Cosmic Nebulae Auras (Cyan Top-Left, Electric Violet Bottom-Right)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.15f),
                radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.2f, size.height * 0.15f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF9D4EDD).copy(alpha = 0.07f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.45f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.85f, size.height * 0.45f)
        )

        // Ambient Twinkling Starfield
        starCoords.forEachIndexed { idx, coord ->
            val x = coord.x * size.width
            val y = coord.y * size.height
            val alpha = if (idx % 2 == 0) starTwinkle else (1.2f - starTwinkle)
            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceIn(0.2f, 1f)),
                radius = if (idx % 3 == 0) 1.8f else 1.2f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Top Section: Sleek status bar with sci-fi indicators, 'GLOBAL MONITOR - ACTIVE - SECURE' glowing badge at the top center.
 */
@Composable
fun JarvisEarthHudHeader(viewModel: JarvisViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_glow")
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "badge_pulse"
    )

    Surface(
        color = Color(0xCC050E22),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.7f), Color(0xFF9D4EDD).copy(alpha = 0.7f)))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sci-Fi Status Bar top indicators row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "QUANTUM ENCRYPTION",
                        color = Color(0xFF88B2D6),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI UPLINK 100% • 60FPS",
                        color = Color(0xFF00E676),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Centered Glowing Badge: 'GLOBAL MONITOR - ACTIVE - SECURE'
            Surface(
                color = Color(0xFF081836),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = badgePulse))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GLOBAL MONITOR • ACTIVE • SECURE",
                        color = Color(0xFFE2F1FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Branding Title with Mode Switcher
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
                            .background(Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF0A152E))))
                            .border(1.2.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "JARVIS",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF00E5FF),
                            shadow = Shadow(
                                color = Color(0x9900E5FF),
                                offset = Offset(0f, 0f),
                                blurRadius = 10f
                            )
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Sun / Moon Theme Toggle Button
                    val isDark = viewModel.isDarkTheme
                    Surface(
                        onClick = { viewModel.toggleTheme() },
                        color = if (isDark) Color(0xFF091A36) else Color(0xFFE8EEF8),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF00E5FF).copy(alpha = 0.6f) else Color(0xFF0078FF).copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Light/Dark Theme",
                                tint = if (isDark) Color(0xFFFFD700) else Color(0xFF0078FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDark) "DARK" else "LIGHT",
                                color = if (isDark) Color(0xFFE2F1FF) else Color(0xFF1A2332),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val isGfMode = viewModel.currentPersona == com.example.persona.JarvisPersona.GIRLFRIEND_MODE
                    Surface(
                        onClick = {
                            val next = if (isGfMode) com.example.persona.JarvisPersona.NORMAL_MODE else com.example.persona.JarvisPersona.GIRLFRIEND_MODE
                            viewModel.selectPersona(next)
                        },
                    color = if (isGfMode) Color(0xFF8A2BE2).copy(alpha = 0.3f) else Color(0xFF091A36),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isGfMode) Color(0xFFFF69B4) else Color(0xFF00E5FF).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isGfMode) Icons.Default.Favorite else Icons.Default.Memory,
                            contentDescription = null,
                            tint = if (isGfMode) Color(0xFFFF69B4) else Color(0xFF00E5FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isGfMode) "GF MODE" else "STARK HUD",
                            color = if (isGfMode) Color(0xFFFFB6C1) else Color(0xFFE2F1FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
}

/**
 * Orbit Status Indicator Row
 * ORBIT 408KM, TILT 23.5°, ROT +24.6°/s, HUD SYNC 99.8% STABLE
 */
@Composable
fun OrbitStatusBar(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xCC050E22),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrbitItem(label = "ORBIT", value = "408KM", color = Color(0xFF00E5FF))
            Divider(modifier = Modifier.height(18.dp).width(1.dp), color = Color(0xFF1E3A5F))
            OrbitItem(label = "TILT", value = "23.5°", color = Color(0xFFD188FF))
            Divider(modifier = Modifier.height(18.dp).width(1.dp), color = Color(0xFF1E3A5F))
            OrbitItem(label = "ROT", value = "+24.6°/s", color = Color(0xFF00E5FF))
            Divider(modifier = Modifier.height(18.dp).width(1.dp), color = Color(0xFF1E3A5F))
            OrbitItem(label = "SYNC", value = "99.8%", color = Color(0xFF00E676))
        }
    }
}

@Composable
private fun OrbitItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF88B2D6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Center Core Display:
 * High-tech circular HUD with animated cosmic Gemini twin aura / glowing digital voice core,
 * pulsating neon cyan and electric purple waves.
 * Next to it is an interactive glowing circular 'LIVE MODE' AI toggle button with audio wave accents.
 */
@Composable
fun CenterCoreDisplayCard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isLive = viewModel.isLiveMode || viewModel.isAllMobileLiveMode
    val isAllMobileLive = viewModel.isAllMobileLiveMode
    val isListening = viewModel.isListening
    val isSpeaking = viewModel.isSpeaking
    val liveRms = viewModel.liveAudioRms
    val isAudioActive = isListening || isSpeaking || isLive || isAllMobileLive

    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_arc_reactor")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.saveUserAvatar(context, uri)
        }
    }

    // 1. Stator 12-Electromagnet Rotation
    val statorRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "stator_rot"
    )

    // 2. Counter-Rotating Inner Vernier Dial
    val vernierRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "vernier_rot"
    )

    // 3. Real Holographic 360° Radar Beam Sweep
    val radarSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "radar_sweep"
    )

    // 4. Acoustic Voice Resonance Pulse
    val reactorPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            tween(if (isAudioActive) 350 else 1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "reactor_pulse"
    )

    // Interactive button audio wave height animation
    val audioWaveAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "audio_wave"
    )

    val dottedCyanDash = remember { androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 6f)) }
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
    val haloBrush = remember {
        Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFFFFD700), Color(0xFF00E5FF)))
    }

    Surface(
        color = Color(0xDD040C1E),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF9D4EDD)))),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT/CENTER: Real J.A.R.V.I.S. Arc Reactor Engine with Live Audio Spectrum & Commander Photo
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clickable { viewModel.toggleAllMobileLiveMode(context) },
                contentAlignment = Alignment.Center
            ) {
                // High-Tech Real Arc Reactor Canvas Engine
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.minDimension / 2f
                    val photoRadius = 52.dp.toPx()

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

                    // 2. Outer Precision Reticle & Quadrant Crosshair Brackets
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        radius = maxR * 0.95f,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    val crosshairLen = 8.dp.toPx()
                    // 4 Cardinal Crosshairs
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(c.x - maxR * 0.95f - crosshairLen, c.y),
                        end = Offset(c.x - maxR * 0.95f + crosshairLen, c.y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(c.x + maxR * 0.95f - crosshairLen, c.y),
                        end = Offset(c.x + maxR * 0.95f + crosshairLen, c.y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(c.x, c.y - maxR * 0.95f - crosshairLen),
                        end = Offset(c.x, c.y - maxR * 0.95f + crosshairLen),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF00E5FF),
                        start = Offset(c.x, c.y + maxR * 0.95f - crosshairLen),
                        end = Offset(c.x, c.y + maxR * 0.95f + crosshairLen),
                        strokeWidth = 1.5.dp.toPx()
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
                            color = Color(0xFF9D4EDD).copy(alpha = 0.35f),
                            radius = vernierRadius,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dottedCyanDash)
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
                                color = if (isMajor) Color(0xFFFFD700) else Color(0xFF9D4EDD).copy(alpha = 0.5f),
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
                        } else if (isLive) {
                            Color(0xFFFFD700)
                        } else {
                            Color(0xFF00E5FF).copy(alpha = 0.5f)
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
                            radius = (photoRadius + 5.dp.toPx()) * reactorPulse,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }

                // CENTER PHOTO: Embedded inside the focal aperture of the Arc Reactor
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF030712))
                        .border(BorderStroke(2.dp, haloBrush), CircleShape)
                        .clickable { viewModel.toggleAllMobileLiveMode(context) },
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

                    // Futuristic HUD circular vignette & lens glare
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF).copy(alpha = 0.08f),
                                        Color(0xFF020716).copy(alpha = 0.42f)
                                    )
                                )
                            )
                    )

                    // Real-Time Live Status Pill at bottom of photo
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xEE03091B),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSpeaking) Color(0xFF00FFFF) else if (isListening) Color(0xFF00E676) else if (isAllMobileLive) Color(0xFF00FFCC) else Color(0xFF00E5FF).copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeUp else if (isListening) Icons.Default.GraphicEq else if (isAllMobileLive) Icons.Default.Sensors else Icons.Default.CameraAlt,
                                contentDescription = "Commander Status",
                                tint = if (isSpeaking) Color(0xFF00FFFF) else if (isListening) Color(0xFF00E676) else if (isAllMobileLive) Color(0xFF00FFCC) else Color(0xFF00E5FF),
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSpeaking) "JARVIS TX" else if (isListening) "VOICE RX" else if (isAllMobileLive) "ALL-MOBILE LIVE" else "BOSS // LIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // RIGHT: Interactive glowing circular 'LIVE MODE' AI toggle button with audio wave accents
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Audio Wave Accents Left & Right flanking the circular button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left wave bars
                    AudioWaveAccentBars(multiplier = audioWaveAnim, isLive = isLive)

                    Spacer(modifier = Modifier.width(8.dp))

                    // Glowing Circular Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isLive) Color(0xFF00E5FF).copy(alpha = 0.3f) else Color(0xFF1E293B),
                                        Color(0xFF060E22)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    if (isAllMobileLive) listOf(Color(0xFF00FFCC), Color(0xFF00E5FF), Color(0xFF9D4EDD), Color(0xFF00FFCC))
                                    else listOf(Color(0xFF00E5FF), Color(0xFF9D4EDD), Color(0xFF00E5FF))
                                ),
                                shape = CircleShape
                            )
                            .clickable { viewModel.toggleAllMobileLiveMode(context) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (isAllMobileLive) Icons.Default.Sensors else Icons.Default.GraphicEq,
                                contentDescription = "Live Mode Toggle",
                                tint = if (isAllMobileLive) Color(0xFF00FFCC) else if (isLive) Color(0xFF00E5FF) else Color.LightGray,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAllMobileLive) "ALL-MOBILE" else "LIVE MODE",
                                color = if (isAllMobileLive) Color(0xFF00FFCC) else if (isLive) Color(0xFFE2F1FF) else Color.LightGray,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right wave bars
                    AudioWaveAccentBars(multiplier = 1.3f - audioWaveAnim, isLive = isLive)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Badge under button
                Surface(
                    color = if (isAllMobileLive) Color(0x3300FFCC) else if (isLive) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isAllMobileLive) Color(0xFF00FFCC) else if (isLive) Color(0xFF00E5FF) else Color.Gray)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isAllMobileLive) Color(0xFF00FFCC) else if (isLive) Color(0xFF00E676) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAllMobileLive) "24/7 AUTONOMOUS" else if (isLive) "ACTIVE UPLINK" else "STANDBY",
                            color = if (isAllMobileLive) Color(0xFF00FFCC) else if (isLive) Color(0xFF00E5FF) else Color.LightGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Audio Wave Accents beside the Live Mode circular toggle
 */
@Composable
private fun AudioWaveAccentBars(multiplier: Float, isLive: Boolean) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val heights = listOf(14.dp, 24.dp, 10.dp)
        heights.forEachIndexed { i, baseH ->
            val h = if (isLive) (baseH.value * multiplier).coerceIn(6f, 28f).dp else 6.dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isLive) Color(0xFF00E5FF) else Color(0xFF475569))
            )
        }
    }
}

/**
 * System Telemetry Cards:
 * System (33%), Nodes (12 Active), Threats (0 Detected)
 */
@Composable
fun HudBottomControlsCards(viewModel: JarvisViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HudGlassCard(
            title = "SYSTEM",
            value = "33% LOAD",
            icon = Icons.Default.Memory,
            accentColor = Color(0xFF00E5FF),
            modifier = Modifier.weight(1f),
            onClick = { viewModel.toggleListeningState() }
        )

        HudGlassCard(
            title = "NODES",
            value = "12 ACTIVE",
            icon = Icons.Default.Hub,
            accentColor = Color(0xFF9D4EDD),
            modifier = Modifier.weight(1f),
            onClick = { viewModel.diagnoseNetwork() }
        )

        HudGlassCard(
            title = "THREATS",
            value = "0 DETECTED",
            icon = Icons.Default.Security,
            accentColor = Color(0xFF00E676),
            modifier = Modifier.weight(1f),
            onClick = { viewModel.logAction("Threat Firewall: Zero Threats Detected") }
        )
    }
}

@Composable
fun HudGlassCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xDD050E22),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.7f)),
        modifier = modifier.height(82.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF88B2D6),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Live smooth audio wave telemetry display with neon cyan frequency graph.
 */
@Composable
fun GlobalActivityWaveformCard(viewModel: JarvisViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_telemetry")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Surface(
        color = Color(0xDD050E22),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AUDIO WAVE TELEMETRY GRAPH",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "NEON FREQUENCY 44.1kHz",
                    color = Color(0xFF00E676),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Smooth Multi-Harmonic Neon Cyan Sine Wave Graph Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF020714))
            ) {
                val width = size.width
                val height = size.height
                val midY = height / 2f

                // Primary Neon Cyan Wave
                val path1 = Path()
                path1.moveTo(0f, midY)
                var x = 0f
                while (x <= width) {
                    val y = midY + sin((x / width * 3.5f * Math.PI) + phase).toFloat() * (height * 0.35f)
                    path1.lineTo(x, y)
                    x += 14f
                }
                drawPath(path = path1, color = Color(0xFF00E5FF), style = Stroke(width = 2.2.dp.toPx()))

                // Secondary Electric Violet Harmonic Wave
                val path2 = Path()
                path2.moveTo(0f, midY)
                x = 0f
                while (x <= width) {
                    val y = midY + sin((x / width * 6f * Math.PI) - phase * 0.8f).toFloat() * (height * 0.22f)
                    path2.lineTo(x, y)
                    x += 14f
                }
                drawPath(path = path2, color = Color(0xFF9D4EDD).copy(alpha = 0.7f), style = Stroke(width = 1.4.dp.toPx()))
            }
        }
    }
}
