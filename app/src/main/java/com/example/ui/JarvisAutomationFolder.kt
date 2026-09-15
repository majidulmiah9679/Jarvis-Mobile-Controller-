package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun JarvisAutomationStatusFolder(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(viewModel.isAutomationFolderExpanded) }
    var isMasterEnabled by remember { mutableStateOf(viewModel.isAutomationMasterEnabled) }
    val isConnected = viewModel.isAutomationServiceConnected
    val selectedApps = viewModel.automationSelectedApps
    val installedApps = viewModel.installedAppsList
    var searchQuery by remember { mutableStateOf("") }

    // Coordinates state for testing gesture methods from JarvisAutomationService
    var clickX by remember { mutableStateOf("500") }
    var clickY by remember { mutableStateOf("1000") }
    var clickTextQuery by remember { mutableStateOf("") }

    // Periodic check for service connectivity
    LaunchedEffect(Unit) {
        viewModel.checkAutomationServiceStatus()
        if (installedApps.isEmpty()) {
            viewModel.loadInstalledApps()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .border(
                width = 1.dp,
                color = if (isMasterEnabled) ArcCyan.copy(alpha = 0.6f) else Color.DarkGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = HoloSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // FOLDER HEADER & MASTER SWITCH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArcCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                            contentDescription = "Folder",
                            tint = if (isMasterEnabled) ArcCyanGlow else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📁 STATUS FOLDER: AUTO CONTROLLER",
                                color = if (isMasterEnabled) ArcCyan else Color.LightGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status Pulse Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isConnected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isConnected) "ONLINE" else "STANDBY",
                                    color = if (isConnected) Color(0xFF00E676) else Color(0xFFFF9100),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (isMasterEnabled) "${selectedApps.size} apps monitored • Tap to ${if (isExpanded) "collapse" else "expand"}" else "Automation Controller Paused",
                            color = HoloTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Master ON/OFF Switch
                Switch(
                    checked = isMasterEnabled,
                    onCheckedChange = {
                        isMasterEnabled = it
                        viewModel.isAutomationMasterEnabled = it
                        viewModel.logAction("AutoController Master: ${if (it) "ON" else "OFF"}")
                        Toast.makeText(context, "Controller ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ArcCyan,
                        checkedTrackColor = ArcCyan.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }

            // EXPANDED CONTENT: FULL FOLDER ACCESS & CONTROLS
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    HorizontalDivider(color = ArcCyan.copy(alpha = 0.25f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // SECTION 1: ACCESSIBILITY SERVICE ACTIVATION
                    Text(
                        text = "SERVICE STATUS & SYSTEM HOOKS",
                        color = StarkGold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = HoloDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "JarvisAutomationService",
                                        color = HoloTextPrimary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isConnected) "Active and bound to Accessibility Manager" else "Service offline. Please tap enable button below.",
                                        color = if (isConnected) Color(0xFF00E676) else Color(0xFFFFAB40),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                IconButton(onClick = {
                                    viewModel.checkAutomationServiceStatus()
                                    Toast.makeText(context, "Status rechecked", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ArcCyan)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Enable JARVIS Service Button (Exact text and toast requested)
                            Button(
                                onClick = { viewModel.openAccessibilitySettings(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btnEnableAccessibility"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) ArcCyanDark else ArcCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SettingsAccessibility, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Enable JARVIS Service",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: APP SELECTION LIST
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সিলেক্ট করুন কোন কোন অ্যাপে কাজ করবে:",
                            color = StarkGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedApps.size} Selected",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Check the applications where JARVIS should monitor window events and perform automation tasks:",
                        color = HoloTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search and Batch Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search installed apps...", color = HoloTextSecondary, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = HoloDarkBg,
                                unfocusedContainerColor = HoloDarkBg,
                                focusedTextColor = HoloTextPrimary,
                                unfocusedTextColor = HoloTextPrimary,
                                cursorColor = ArcCyan,
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = ArcCyan.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { viewModel.selectAllAutomationApps() },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("All", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = { viewModel.clearAllAutomationApps() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("None", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filtered App List Container
                    val filteredApps = installedApps.filter {
                        searchQuery.isEmpty() ||
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.packageName.contains(searchQuery, ignoreCase = true)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        color = HoloDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyan.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                        ) {
                            if (filteredApps.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No apps found matching '$searchQuery'",
                                        color = HoloTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                filteredApps.take(15).forEach { app ->
                                    val isChecked = selectedApps.contains(app.packageName)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.toggleAutomationApp(app.packageName, !isChecked)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.name,
                                                color = if (isChecked) ArcCyan else HoloTextPrimary,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "(${app.packageName})",
                                                color = HoloTextSecondary,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                viewModel.toggleAutomationApp(app.packageName, checked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = ArcCyan,
                                                uncheckedColor = HoloTextSecondary
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 3: AUTOMATION ACTION EXECUTORS (autoClick, autoScrollDown, clickByText)
                    Text(
                        text = "GESTURE & NODE AUTOMATION TOOLS",
                        color = StarkGold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = HoloDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArcCyanDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Method 1: autoClick(x, y)
                            Text(
                                text = "1. Auto Tap at Screen Coordinates (autoClick):",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = clickX,
                                    onValueChange = { clickX = it },
                                    label = { Text("X", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ArcCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = clickY,
                                    onValueChange = { clickY = it },
                                    label = { Text("Y", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ArcCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        val x = clickX.toFloatOrNull() ?: 500f
                                        val y = clickY.toFloatOrNull() ?: 1000f
                                        viewModel.testAutoClick(x, y)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("TAP", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Method 2: autoScrollDown()
                            Text(
                                text = "2. Auto Scroll Down (autoScrollDown):",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.testAutoScrollDown() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = ArcCyanGlow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EXECUTE AUTO SCROLL DOWN (500,1500 -> 500,500)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Method 3: clickByText(text)
                            Text(
                                text = "3. Click Node by Text (clickByText):",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = clickTextQuery,
                                    onValueChange = { clickTextQuery = it },
                                    placeholder = { Text("Button or element label...", fontSize = 10.sp, color = HoloTextSecondary) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = ArcCyan,
                                        unfocusedBorderColor = Color.DarkGray
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (clickTextQuery.isNotBlank()) {
                                            viewModel.testClickByText(clickTextQuery.trim())
                                        } else {
                                            Toast.makeText(context, "Enter node text to find and click", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("FIND & CLICK", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SECTION 4: FILE & HOOK REGISTRY
                    Text(
                        text = "INTEGRATED SYSTEM FILES & SPECS",
                        color = StarkGold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text("• Service: com.example.JarvisAutomationService", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("• Permissions: QUERY_ALL_PACKAGES & BIND_ACCESSIBILITY_SERVICE", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("• Config XML: res/xml/accessibility_service_config.xml", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text("• Shared Preferences: JarvisSettings.xml -> 'selected_apps'", color = HoloTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
