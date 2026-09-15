package com.example.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ENUM & FOLDER STACK FOR SECURITY SETTINGS REORGANIZATION
enum class SecurityFolder(val title: String, val subtitle: String, val icon: ImageVector) {
    SENTINEL_VAULT("SENTINEL & PRIVACY VAULT", "Threat Radar, 253 Apps Audited & Data Leak Tracker", Icons.Default.Security),
    VOICE_BIOMETRICS("VOICE BIOMETRICS & AMAR LOCK", "Operator Majidul Voice Signature & Vocal Shield", Icons.Default.RecordVoiceOver),
    DELETE_PROTECTION("DELETE PROTECTION PROTOCOL", "Zero-Accident Safety Shield & Password Verification", Icons.Default.VerifiedUser),
    DEFENSE_SHIELD("DEFENSE SHIELD & INTRUDER SENTINEL", "Intruder Auto-Snap, Quantum Firewall & DEFCON 1", Icons.Default.Shield),
    EMERGENCY_LOCKDOWN("EMERGENCY PROTOCOLS & LOCKDOWN", "Defensive Lockdown & Master Threat Isolation", Icons.Default.Lock)
}

@Composable
fun SecurityFolderItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color(0xFF1A2332)
    val subTextColor = if (isDark) HoloTextSecondary else Color(0xFF5A6E85)
    val accentColor = if (isDark) Color(0xFF00F5FF) else Color(0xFF0078FF)
    val dividerColor = if (isDark) Color(0xFF1A2332) else Color(0xFFE0E6ED)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 16.dp),
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
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = subTextColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open $title",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Divider(color = dividerColor, thickness = 1.dp)
    }
}

// TAB 1: SECURITY SCREEN (CLEAN FOLDER STACK ARCHITECTURE)
@Composable
fun JarvisSecurityScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val isDark = viewModel.isDarkTheme

    val bgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF080B14) else Color(0xFFF5F7FB),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_bg"
    )
    val cardBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0A1526) else Color.White,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_card_bg"
    )
    val textColorPrimary by animateColorAsState(
        targetValue = if (isDark) Color.White else Color(0xFF1A2332),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_text_primary"
    )
    val textColorSecondary by animateColorAsState(
        targetValue = if (isDark) HoloTextSecondary else Color(0xFF5A6E85),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_text_secondary"
    )
    val cyanAccent by animateColorAsState(
        targetValue = if (isDark) Color(0xFF00F5FF) else Color(0xFF0078FF),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_cyan_accent"
    )
    val dividerColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1A2332) else Color(0xFFE0E6ED),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "sec_divider_color"
    )

    var activeFolder by remember { mutableStateOf<SecurityFolder?>(null) }

    BackHandler(enabled = activeFolder != null) {
        activeFolder = null
    }

    // Password change dialog if triggered
    if (viewModel.showPasswordChangeDialog) {
        DeletePasswordChangeDialog(viewModel = viewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // TOP HEADER: Back Arrow (<-) + Text "SECURITY SETTINGS" in cyan + Shield icon top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (activeFolder != null) {
                            activeFolder = null
                        } else {
                            viewModel.selectTab(0)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = cyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = activeFolder?.title ?: "SECURITY SETTINGS",
                    color = cyanAccent,
                    fontSize = if ((activeFolder?.title?.length ?: 0) > 20) 14.sp else 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = {
                    Toast.makeText(context, "DEFCON 1 • STARK ACTIVE ENCRYPTION", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Status",
                    tint = cyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Below header: Thin cyan line separator
        Divider(
            color = cyanAccent.copy(alpha = 0.6f),
            thickness = 1.dp
        )

        // Sub-page or Main Folder Stack
        when (activeFolder) {
            null -> {
                // MAIN FOLDER STACK FOR SECURITY
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Security Quick Status Banner
                    Surface(
                        color = cardBgColor,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, cyanAccent.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(cyanAccent.copy(alpha = 0.15f))
                                    .border(1.2.dp, cyanAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DEFCON LEVEL 1 • ACTIVE ENCRYPTION",
                                    color = StarkGold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "All security shields, monitors & biometrics active",
                                    color = textColorSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }

                    // 1. JARVIS SENTINEL & PRIVACY VAULT (Explicitly requested as a dedicated Folder)
                    SecurityFolderItemRow(
                        title = SecurityFolder.SENTINEL_VAULT.title,
                        subtitle = SecurityFolder.SENTINEL_VAULT.subtitle,
                        icon = SecurityFolder.SENTINEL_VAULT.icon,
                        isDark = isDark,
                        onClick = { activeFolder = SecurityFolder.SENTINEL_VAULT }
                    )

                    // 2. VOICE BIOMETRICS & AMAR LOCK
                    SecurityFolderItemRow(
                        title = SecurityFolder.VOICE_BIOMETRICS.title,
                        subtitle = SecurityFolder.VOICE_BIOMETRICS.subtitle,
                        icon = SecurityFolder.VOICE_BIOMETRICS.icon,
                        isDark = isDark,
                        onClick = { activeFolder = SecurityFolder.VOICE_BIOMETRICS }
                    )

                    // 3. DELETE PROTECTION PROTOCOL
                    SecurityFolderItemRow(
                        title = SecurityFolder.DELETE_PROTECTION.title,
                        subtitle = SecurityFolder.DELETE_PROTECTION.subtitle,
                        icon = SecurityFolder.DELETE_PROTECTION.icon,
                        isDark = isDark,
                        onClick = { activeFolder = SecurityFolder.DELETE_PROTECTION }
                    )

                    // 4. DEFENSE SHIELD & INTRUDER SENTINEL
                    SecurityFolderItemRow(
                        title = SecurityFolder.DEFENSE_SHIELD.title,
                        subtitle = SecurityFolder.DEFENSE_SHIELD.subtitle,
                        icon = SecurityFolder.DEFENSE_SHIELD.icon,
                        isDark = isDark,
                        onClick = { activeFolder = SecurityFolder.DEFENSE_SHIELD }
                    )

                    // 5. EMERGENCY PROTOCOLS & LOCKDOWN
                    SecurityFolderItemRow(
                        title = SecurityFolder.EMERGENCY_LOCKDOWN.title,
                        subtitle = SecurityFolder.EMERGENCY_LOCKDOWN.subtitle,
                        icon = SecurityFolder.EMERGENCY_LOCKDOWN.icon,
                        isDark = isDark,
                        onClick = { activeFolder = SecurityFolder.EMERGENCY_LOCKDOWN }
                    )

                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.height(32.dp))

                    // Footer: Small text center bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "v2.4.1 • DEFCON 1 ACTIVE SHIELD",
                            color = cyanAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            SecurityFolder.SENTINEL_VAULT -> {
                // FOLDER 1: JARVIS SENTINEL & PRIVACY VAULT
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    JarvisSentinelVaultCard(viewModel = viewModel)
                }
            }

            SecurityFolder.VOICE_BIOMETRICS -> {
                // FOLDER 2: VOICE BIOMETRICS & AMAR LOCK
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Operator Profile Badge
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cyanAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(cyanAccent.copy(alpha = 0.15f))
                                    .border(1.2.dp, cyanAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "OPERATOR: MAJIDUL (BOSS)",
                                    color = textColorPrimary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "SECURITY CLEARANCE: LEVEL 10 (MASTER ADMIN)",
                                    color = cyanAccent,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Biometric Shield Switch
                    SecurityToggleCard(
                        title = "Voice Biometric Shield",
                        subtitle = "Lock control exclusively to authorized vocal pattern (Majidul Boss)",
                        isChecked = viewModel.securityVoiceLock,
                        onCheckedChange = {
                            viewModel.securityVoiceLock = it
                            viewModel.logAction("Voice Biometric Shield ${if (it) "ENABLED" else "DISABLED"}")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amar Voice Biometric Calibration & Signature Card
                    VoiceBiometricEnrollmentCard(viewModel = viewModel)
                }
            }

            SecurityFolder.DELETE_PROTECTION -> {
                // FOLDER 3: DELETE PROTECTION PROTOCOL
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    DeleteProtectionProtocolCard(viewModel = viewModel)
                }
            }

            SecurityFolder.DEFENSE_SHIELD -> {
                // FOLDER 4: DEFENSE SHIELD & INTRUDER SENTINEL
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // DEFCON 1 Status Banner
                    Surface(
                        color = cardBgColor,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StarkGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppGood,
                                contentDescription = null,
                                tint = StarkGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "DEFCON LEVEL 1 • ACTIVE ENCRYPTION",
                                    color = StarkGold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Stark Industries 4096-bit live packet encryption active",
                                    color = textColorSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }
                    }

                    // Intruder Auto-Sentinel Toggle
                    SecurityToggleCard(
                        title = "Intruder Auto-Sentinel",
                        subtitle = "Capture photo and block device upon unauthorized access",
                        isChecked = viewModel.securityIntruderAlert,
                        onCheckedChange = {
                            viewModel.securityIntruderAlert = it
                            viewModel.logAction("Intruder Sentinel ${if (it) "ARMED" else "DISARMED"}")
                        }
                    )

                    // Quantum Encryption Firewall Toggle
                    SecurityToggleCard(
                        title = "Quantum Encryption Firewall",
                        subtitle = "Stark Industries 4096-bit live packet encryption & network shield",
                        isChecked = viewModel.securityFirewall,
                        onCheckedChange = {
                            viewModel.securityFirewall = it
                            viewModel.logAction("Quantum Firewall ${if (it) "ACTIVATED" else "DEACTIVATED"}")
                        }
                    )

                    // Network Port & Rogue Telemetry Blocker Info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cyanAccent.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "TACTICAL THREAT INTERCEPTION",
                                color = cyanAccent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Automated real-time port sniffing and heuristic blocking active. Unrecognized outbound sockets are instantly severed.",
                                color = textColorSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            SecurityFolder.EMERGENCY_LOCKDOWN -> {
                // FOLDER 5: EMERGENCY PROTOCOLS & LOCKDOWN
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, Color(0xFFFF4444).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "EMERGENCY PROTOCOL OVERRIDE",
                                    color = Color(0xFFFF4444),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Triggering emergency lockdown will immediately sever all external network uplinks, lock all biometric and voice channels, and require manual passcode verification to restore normal telemetry.",
                                color = textColorSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // EMERGENCY LOCKDOWN BUTTON
                    Button(
                        onClick = {
                            viewModel.logAction("EMERGENCY PROTOCOL: COMPLETE LOCKDOWN")
                            Toast.makeText(context, "EMERGENCY LOCKDOWN ENGAGED", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "TRIGGER EMERGENCY LOCKDOWN",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityToggleCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = HoloSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ArcCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = HoloTextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = HoloTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ArcCyan,
                    checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun DeleteProtectionProtocolCard(viewModel: JarvisViewModel) {
    var isPassVisible by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF0F1E36),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, if (viewModel.isDeleteProtectionEnabled) Color(0xFF00E5FF) else Color.Gray.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (viewModel.isDeleteProtectionEnabled) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.DarkGray)
                            .border(1.dp, if (viewModel.isDeleteProtectionEnabled) Color(0xFF00E5FF) else Color.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = if (viewModel.isDeleteProtectionEnabled) Color(0xFF00E5FF) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DELETE PROTECTION PROTOCOL",
                            color = Color(0xFFF1F8FD),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ZERO-ACCIDENT SAFETY SHIELD",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = viewModel.isDeleteProtectionEnabled,
                    onCheckedChange = { viewModel.toggleDeleteProtection(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Under no circumstances are files, contacts, messages, media, or data deleted without vocal or written security password verification.",
                color = Color(0xFFBDD8F0),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Password Status Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070F1E))
                    .border(1.dp, Color(0xFF00D4FF).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ACTIVE SECURITY PASSWORD",
                        color = Color(0xFF88B2D6),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isPassVisible) viewModel.jarvisDeletePass else "•••• (${viewModel.jarvisDeletePass.length} chars)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isPassVisible = !isPassVisible },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextButton(
                        onClick = { viewModel.showPasswordChangeDialog = true }
                    ) {
                        Text(
                            text = "CHANGE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test button to verify the protocol
            OutlinedButton(
                onClick = {
                    viewModel.requestProtectedDeletion("Stark Diagnostics & Temporary Files") {
                        viewModel.logAction("TEST DELETION COMPLETED: Verified under Boss Security Password.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.7f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEST DELETE SHIELD PROTOCOL",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun DeletePasswordChangeDialog(viewModel: JarvisViewModel) {
    var newPasswordInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { viewModel.showPasswordChangeDialog = false },
        title = {
            Text(
                text = "CHANGE DELETE PASSWORD",
                color = Color(0xFF00E5FF),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter new security password required before any deletion action:",
                    color = Color(0xFFBDD8F0),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    placeholder = { Text("e.g. 1234 or Stark99", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPasswordInput.isNotBlank()) {
                        viewModel.updateDeletePassword(newPasswordInput)
                        viewModel.showPasswordChangeDialog = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("SAVE PASSWORD", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showPasswordChangeDialog = false }) {
                Text("CANCEL", color = Color.LightGray)
            }
        },
        containerColor = Color(0xFF0B172B)
    )
}
