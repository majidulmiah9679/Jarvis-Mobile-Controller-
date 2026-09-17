package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.AppThreatReport
import com.example.security.DataLeakEvent
import com.example.security.ThreatLevel
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.HoloSurface
import com.example.ui.theme.HoloTextPrimary
import com.example.ui.theme.HoloTextSecondary
import com.example.ui.theme.StarkGold

@Composable
fun JarvisSentinelVaultCard(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Live Radar & Apps, 1: Leak Logs, 2: Defensive Measures, 3: Quarantine Vault
    var selectedThreatFilter by remember { mutableStateOf<ThreatLevel?>(null) }
    var selectedAppDetail by remember { mutableStateOf<AppThreatReport?>(null) }

    // Pulse animation for Sentinel Radar
    val infiniteTransition = rememberInfiniteTransition(label = "sentinel_pulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_pulse_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jarvis-sentinel-vault-card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030E14)),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    ArcCyan.copy(alpha = 0.8f),
                    Color(0xFFFF2255).copy(alpha = 0.6f),
                    StarkGold.copy(alpha = 0.8f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // HEADER BAR: J.A.R.V.I.S. SENTINEL & PRIVACY VAULT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF002233))
                            .border(1.5.dp, ArcCyan.copy(alpha = radarPulse), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Sentinel Shield",
                            tint = ArcCyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS SENTINEL & PRIVACY VAULT",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF00FF88).copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ACTIVE DEFENSE",
                                    color = Color(0xFF00FF88),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "Continuous Background Leak Radar • Threat Neutralizer",
                            color = HoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ArcCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = ArcCyan.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // LIVE METRIC METERS (TOTAL SCANNED, ROGUE DETECTED, QUARANTINED, LEAKS BLOCKED)
                    val apps = viewModel.sentinelApps
                    val rogueCount = apps.count { it.threatLevel == ThreatLevel.ROGUE }
                    val highRiskCount = apps.count { it.threatLevel == ThreatLevel.HIGH_RISK }
                    val quarantinedCount = apps.count { it.isQuarantined }
                    val blockedCount = apps.count { it.isDataBlocked }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricCounterBadge(
                            label = "APPS MONITORED",
                            value = "${apps.size}",
                            color = ArcCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCounterBadge(
                            label = "ROGUE THREATS",
                            value = "$rogueCount",
                            color = Color(0xFFFF2255),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCounterBadge(
                            label = "QUARANTINED",
                            value = "$quarantinedCount",
                            color = StarkGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCounterBadge(
                            label = "DATA BLOCKED",
                            value = "$blockedCount",
                            color = Color(0xFF00FF88),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // MASTER ONE-TAP KILL-SWITCHES
                    Text(
                        text = "TACTICAL MASTER CONTROLS // BOSS ONE-TAP",
                        color = StarkGold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.revokeAllBackgroundData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B0000).copy(alpha = 0.45f),
                                contentColor = Color(0xFFFF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REVOKE ALL DATA", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.killAllRogueProcesses() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5500).copy(alpha = 0.35f),
                                contentColor = Color(0xFFFF8800)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF8800).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("KILL ROGUE PROCESSES", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.scanSentinelEcosystem() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcCyanGlow),
                            border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RE-SCAN", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SUB-MODULE NAVIGATION PILLS (0: App Threats, 1: Live Leak Logs, 2: Countermeasures, 3: Quarantine Vault)
                    val subTabs = listOf(
                        "THREAT RADAR (${apps.size})",
                        "LEAK LOGS",
                        "DEFENSIVE SHIELD",
                        "QUARANTINE ($quarantinedCount)"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subTabs.forEachIndexed { index, title ->
                            val isSel = activeSubTab == index
                            Surface(
                                onClick = { activeSubTab = index },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) ArcCyan.copy(alpha = 0.25f) else Color(0xFF071B26),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSel) ArcCyan else ArcCyan.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSel) ArcCyanGlow else HoloTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SUB-TAB CONTENT
                    when (activeSubTab) {
                        0 -> {
                            // THREAT RADAR & APPS LIST
                            ThreatRadarAppsView(
                                apps = apps,
                                selectedFilter = selectedThreatFilter,
                                onSelectFilter = { selectedThreatFilter = it },
                                onKillProcess = { viewModel.killAppProcess(it.packageName) },
                                onToggleQuarantine = { viewModel.toggleQuarantine(it.packageName) },
                                onToggleBlockData = { viewModel.toggleBlockAppData(it.packageName) },
                                onToggleFreeze = { viewModel.freezeApp(it.packageName) },
                                onInspectApp = { selectedAppDetail = it },
                                onOpenSystemSettings = { viewModel.openAppSystemSettings(it.packageName) }
                            )
                        }
                        1 -> {
                            // LIVE DATA LEAK TRACKER LOGS
                            LiveLeakLogsView(logs = viewModel.sentinelLeakLogs)
                        }
                        2 -> {
                            // AUTOMATED DEFENSIVE COUNTERMEASURES
                            AutomatedDefensesView(viewModel = viewModel)
                        }
                        3 -> {
                            // SANDBOX QUARANTINE VAULT
                            SandboxQuarantineVaultView(
                                apps = apps.filter { it.isQuarantined },
                                onReleaseQuarantine = { viewModel.toggleQuarantine(it.packageName) },
                                onKillProcess = { viewModel.killAppProcess(it.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }

    // DETAIL MODAL / BOTTOM SHEET FOR INSPECTING APP
    if (selectedAppDetail != null) {
        AppThreatDetailDialog(
            app = selectedAppDetail!!,
            onDismiss = { selectedAppDetail = null },
            onKillProcess = {
                viewModel.killAppProcess(selectedAppDetail!!.packageName)
                selectedAppDetail = null
            },
            onToggleQuarantine = {
                viewModel.toggleQuarantine(selectedAppDetail!!.packageName)
                selectedAppDetail = null
            },
            onToggleBlockData = {
                viewModel.toggleBlockAppData(selectedAppDetail!!.packageName)
                selectedAppDetail = null
            },
            onOpenSettings = {
                viewModel.openAppSystemSettings(selectedAppDetail!!.packageName)
            }
        )
    }
}

@Composable
private fun MetricCounterBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF04151F))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = HoloTextSecondary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ThreatRadarAppsView(
    apps: List<AppThreatReport>,
    selectedFilter: ThreatLevel?,
    onSelectFilter: (ThreatLevel?) -> Unit,
    onKillProcess: (AppThreatReport) -> Unit,
    onToggleQuarantine: (AppThreatReport) -> Unit,
    onToggleBlockData: (AppThreatReport) -> Unit,
    onToggleFreeze: (AppThreatReport) -> Unit,
    onInspectApp: (AppThreatReport) -> Unit,
    onOpenSystemSettings: (AppThreatReport) -> Unit
) {
    var visibleLimit by remember { mutableStateOf(20) }

    LaunchedEffect(selectedFilter) {
        visibleLimit = 20
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // FILTER CHIPS (ALL, ROGUE, HIGH RISK, MODERATE, SAFE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterBadge(label = "ALL", isSelected = selectedFilter == null) { onSelectFilter(null) }
            FilterBadge(label = "ROGUE (80%+)", color = Color(0xFFFF2255), isSelected = selectedFilter == ThreatLevel.ROGUE) {
                onSelectFilter(if (selectedFilter == ThreatLevel.ROGUE) null else ThreatLevel.ROGUE)
            }
            FilterBadge(label = "HIGH RISK", color = Color(0xFFFF8800), isSelected = selectedFilter == ThreatLevel.HIGH_RISK) {
                onSelectFilter(if (selectedFilter == ThreatLevel.HIGH_RISK) null else ThreatLevel.HIGH_RISK)
            }
            FilterBadge(label = "MODERATE", color = StarkGold, isSelected = selectedFilter == ThreatLevel.MODERATE) {
                onSelectFilter(if (selectedFilter == ThreatLevel.MODERATE) null else ThreatLevel.MODERATE)
            }
            FilterBadge(label = "SAFE", color = Color(0xFF00FF88), isSelected = selectedFilter == ThreatLevel.SAFE) {
                onSelectFilter(if (selectedFilter == ThreatLevel.SAFE) null else ThreatLevel.SAFE)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredApps = remember(apps, selectedFilter) {
            if (selectedFilter == null) apps else apps.filter { it.threatLevel == selectedFilter }
        }

        if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HoloSurface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No applications detected under this security threshold.",
                    color = HoloTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            val displayedApps = filteredApps.take(visibleLimit)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayedApps.forEach { app ->
                    AppThreatItemCard(
                        app = app,
                        onKillProcess = { onKillProcess(app) },
                        onToggleQuarantine = { onToggleQuarantine(app) },
                        onToggleBlockData = { onToggleBlockData(app) },
                        onToggleFreeze = { onToggleFreeze(app) },
                        onInspect = { onInspectApp(app) },
                        onOpenSystemSettings = { onOpenSystemSettings(app) }
                    )
                }

                if (filteredApps.size > visibleLimit) {
                    val remaining = filteredApps.size - visibleLimit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { visibleLimit += 25 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002233)),
                            border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                "LOAD NEXT 25 (${remaining} MORE)",
                                fontSize = 10.sp,
                                color = ArcCyan,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { visibleLimit = filteredApps.size },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003344)),
                            border = BorderStroke(1.dp, ArcCyanGlow),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                "SHOW ALL (${filteredApps.size})",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterBadge(
    label: String,
    color: Color = ArcCyan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF081C26))
            .border(1.dp, if (isSelected) color else color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) color else HoloTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AppThreatItemCard(
    app: AppThreatReport,
    onKillProcess: () -> Unit,
    onToggleQuarantine: () -> Unit,
    onToggleBlockData: () -> Unit,
    onToggleFreeze: () -> Unit,
    onInspect: () -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    val levelColor = when (app.threatLevel) {
        ThreatLevel.ROGUE -> Color(0xFFFF2255)
        ThreatLevel.HIGH_RISK -> Color(0xFFFF8800)
        ThreatLevel.MODERATE -> StarkGold
        ThreatLevel.SAFE -> Color(0xFF00FF88)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF06141D))
            .border(1.dp, levelColor.copy(alpha = if (app.isQuarantined) 0.8f else 0.35f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            // Title and Threat Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (app.isQuarantined) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFFF2255).copy(alpha = 0.3f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("QUARANTINED", color = Color(0xFFFF4444), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (app.isDataBlocked) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("NET BLOCKED", color = ArcCyanGlow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = app.packageName,
                        color = HoloTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Risk Score Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${app.riskScore}% RISK",
                        color = levelColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = app.threatLevel.label,
                        color = levelColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Risk progress bar
            LinearProgressIndicator(
                progress = { app.riskScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = levelColor,
                trackColor = Color(0xFF102838)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Data Usage & Installation Source
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DATA USAGE: ${com.example.network.JarvisNetworkTracker.formatBytes(app.backgroundDataBytes)}",
                    color = ArcCyanGlow,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SOURCE: ${app.installSource.take(18)}",
                    color = HoloTextSecondary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Sensitive Permissions Tags
            if (app.permissionsUsed.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    app.permissionsUsed.forEach { perm ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0D2535))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = perm,
                                color = ArcCyan,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Suspicious Socket or Telemetry details if any
            if (app.activeSocketConnection != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF4444), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Socket: ${app.activeSocketConnection}",
                        color = Color(0xFFFF8888),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ACTION BUTTONS (KILL PROCESS, BLOCK DATA, QUARANTINE, INSPECT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Kill Process Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF220008))
                        .border(1.dp, Color(0xFFFF2255).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable { onKillProcess() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💀 KILL PROCESS",
                        color = Color(0xFFFF4444),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Block Internet Access Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (app.isDataBlocked) Color(0xFF003322) else Color(0xFF001A26))
                        .border(
                            1.dp,
                            if (app.isDataBlocked) Color(0xFF00FF88).copy(alpha = 0.6f) else ArcCyan.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onToggleBlockData() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.isDataBlocked) "UNBLOCK DATA" else "BLOCK DATA",
                        color = if (app.isDataBlocked) Color(0xFF00FF88) else ArcCyanGlow,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Quarantine Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (app.isQuarantined) Color(0xFF2B1D00) else Color(0xFF1F1200))
                        .border(1.dp, StarkGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable { onToggleQuarantine() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (app.isQuarantined) "RELEASE" else "QUARANTINE",
                        color = StarkGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Deep Inspect Button
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF082030))
                        .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { onInspect() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AUDIT",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveLeakLogsView(logs: List<DataLeakEvent>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "LIVE TELEMETRY & SOCKET INTERCEPTION LOGS",
            color = StarkGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Real-time inspection of background socket transmissions and telemetry traps:",
            color = HoloTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )

        logs.forEach { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF04131C))
                    .border(1.dp, Color(0xFF00FF88).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00FF88),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = log.appName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = log.timestamp,
                            color = HoloTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target IP: ${log.targetIp}",
                        color = ArcCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Data Leaked: ${log.dataType}",
                        color = Color(0xFFFF8800),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF00FF88).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTION: ${log.actionTaken}",
                            color = Color(0xFF00FF88),
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

@Composable
private fun AutomatedDefensesView(viewModel: JarvisViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "AUTOMATED DEFENSIVE COUNTERMEASURES",
            color = StarkGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        DefenseToggleRow(
            title = "Dummy Data Injection (Telemetry Deception)",
            subtitle = "Feeds fake clipboard strings, null contacts, and spoofed GPS to tracking daemons",
            isChecked = viewModel.isDummyDataActive,
            onCheckedChange = { viewModel.toggleDummyData() }
        )

        DefenseToggleRow(
            title = "Mic & Camera Anti-Spy Interceptor",
            subtitle = "Instantly sever audio/video feed if third-party app attempts background access",
            isChecked = viewModel.isMicCamShieldActive,
            onCheckedChange = { viewModel.toggleMicCamShield() }
        )

        DefenseToggleRow(
            title = "Network Firewall Socket Shield",
            subtitle = "Blocks rogue IP ranges and untrusted analytical CDNs at packet level",
            isChecked = viewModel.isNetworkFirewallActive,
            onCheckedChange = { viewModel.toggleNetworkFirewall() }
        )
    }
}

@Composable
private fun DefenseToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF071924))
            .border(1.dp, ArcCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = HoloTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ArcCyan,
                checkedTrackColor = ArcCyan.copy(alpha = 0.4f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
private fun SandboxQuarantineVaultView(
    apps: List<AppThreatReport>,
    onReleaseQuarantine: (AppThreatReport) -> Unit,
    onKillProcess: (AppThreatReport) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SANDBOX QUARANTINE VAULT",
            color = Color(0xFFFF4444),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Isolated rogue entities. Process killed, networking severed, suspended in sandbox.",
            color = HoloTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF04121A))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vault Empty: No apps currently under Sandbox Quarantine.",
                    color = Color(0xFF00FF88),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                apps.forEach { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A050A))
                            .border(1.dp, Color(0xFFFF2255).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "🔒 ${app.appName}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = Color(0xFFFF8888),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Button(
                                    onClick = { onReleaseQuarantine(app) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StarkGold.copy(alpha = 0.3f),
                                        contentColor = StarkGold
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("RELEASE", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Install Source: ${app.installSource}",
                                color = HoloTextSecondary,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Certificate: ${app.certificateFingerprint}",
                                color = HoloTextSecondary,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppThreatDetailDialog(
    app: AppThreatReport,
    onDismiss: () -> Unit,
    onKillProcess: () -> Unit,
    onToggleQuarantine: () -> Unit,
    onToggleBlockData: () -> Unit,
    onOpenSettings: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF04121A),
            border = BorderStroke(1.5.dp, ArcCyan),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEEP SECURITY AUDIT",
                        color = StarkGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                            .clickable { onDismiss() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("CLOSE [X]", color = ArcCyanGlow, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = ArcCyan.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = app.appName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = app.packageName,
                    color = HoloTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "THREAT LEVEL:", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${app.threatLevel.label} (${app.riskScore}%)", color = Color(0xFFFF2255), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "INSTALL SOURCE:", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = app.installSource, color = ArcCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "CERTIFICATE:", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = app.certificateFingerprint, color = HoloTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "HIDDEN SERVICES:", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "${app.hiddenServicesCount} Services • ${app.autorunTriggers} Autoruns", color = HoloTextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "SUSPICIOUS BEHAVIORS DETECTED:", color = StarkGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                if (app.suspiciousBehaviors.isEmpty()) {
                    Text("• Standard application activity profile.", color = Color(0xFF00FF88), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                } else {
                    app.suspiciousBehaviors.forEach { behavior ->
                        Text("• $behavior", color = Color(0xFFFF8888), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tactical action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onKillProcess,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2255)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("KILL APP", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onToggleBlockData,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077AA)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BLOCK NET", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onToggleQuarantine,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7500)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (app.isQuarantined) "UN-ISOLATE" else "QUARANTINE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onOpenSettings,
                    border = BorderStroke(1.dp, ArcCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OPEN ANDROID APP PERMISSIONS SETTINGS", fontSize = 9.sp, color = ArcCyanGlow, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
