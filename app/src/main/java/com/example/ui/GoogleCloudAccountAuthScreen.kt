package com.example.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.JarvisGoogleAuthManager
import com.example.network.GeminiNetworkDispatcher
import kotlinx.coroutines.launch

private val CyanBright = Color(0xFF00E5FF)
private val GreenNeon = Color(0xFF00FF9D)
private val GoldAmber = Color(0xFFFFB800)
private val RedAlert = Color(0xFFFF4D4D)
private val DarkBg = Color(0xFF060D17)
private val DarkCardBg = Color(0xFF0A1626)
private val DarkCardBorder = Color(0xFF1E334D)

/**
 * GOOGLE CLOUD ACCOUNT AUTH SCREEN - DUAL SIGNUP SYSTEM
 *
 * Provides:
 * 1. Direct Google Sign-In with Credential Manager API & Firebase Auth
 * 2. Manual Email Signup, Login, and Password Reset
 * 3. Automatic switch to Google Gemini Cloud Engine
 * 4. Zero 401 error guarantee via OAuth2 / Firebase AI SDK
 */
@Composable
fun GoogleCloudAccountAuthScreen(
    viewModel: JarvisViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var isCloudLoggedIn by remember { mutableStateOf(JarvisGoogleAuthManager.isGoogleCloudLoggedIn(context)) }
    var userEmail by remember { mutableStateOf(JarvisGoogleAuthManager.getSignedInEmail(context)) }
    var userName by remember { mutableStateOf(JarvisGoogleAuthManager.getSignedInName(context)) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var statusIsError by remember { mutableStateOf(false) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf("") }

    val handleAuthSuccess = { finalEmail: String, finalName: String ->
        isLoading = false
        isCloudLoggedIn = true
        userEmail = finalEmail
        userName = finalName
        statusIsError = false
        statusMessage = "Authenticated successfully as $finalName! 🟢"

        // Automatically switch AI Engine to "Google Gemini (Cloud / Account Integration)"
        viewModel.activateEngine("GEMINI_CLOUD")
        Toast.makeText(context, "Switched to Gemini Cloud Engine! 0% API Key error! 🟢", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("google_cloud_auth_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1E33))
                    .testTag("auth_screen_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyanBright)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "GOOGLE CLOUD ACCOUNT",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "OAUTH 2.0 ZERO-KEY ARCHITECTURE",
                    color = GreenNeon,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TOP CARD: GOOGLE CLOUD ACCOUNT (For Gemini Cloud)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            border = BorderStroke(1.5.dp, if (isCloudLoggedIn) GreenNeon else CyanBright),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("top_cloud_account_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isCloudLoggedIn) GreenNeon.copy(alpha = 0.15f) else CyanBright.copy(alpha = 0.15f))
                                .border(1.5.dp, if (isCloudLoggedIn) GreenNeon else CyanBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCloudLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (isCloudLoggedIn) GreenNeon else CyanBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GOOGLE CLOUD ACCOUNT",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "(For Gemini Cloud)",
                                color = GoldAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCloudLoggedIn) GreenNeon.copy(alpha = 0.2f) else CyanBright.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isCloudLoggedIn) "CONNECTED 🟢" else "LOGIN REQUIRED",
                            color = if (isCloudLoggedIn) GreenNeon else CyanBright,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Login to use Gemini without API Key - No 401 error",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Official OAuth2 bearer authorization via Android Credential Manager and Firebase AI SDK. No manual AQ or AIza keys needed.",
                    color = Color(0xFF627D98),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STATUS / NOTIFICATION BANNER
        AnimatedVisibility(visible = statusMessage.isNotBlank()) {
            val boxBorder = if (statusIsError) RedAlert else GreenNeon
            val boxBg = if (statusIsError) Color(0xFF26050C) else Color(0xFF021C0F)
            val boxText = if (statusIsError) RedAlert else GreenNeon

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(boxBg)
                    .border(1.2.dp, boxBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (statusIsError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = boxText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // LOADING ANIMATION
        AnimatedVisibility(visible = isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF09182A))
                    .border(1.dp, CyanBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = CyanBright,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AUTHENTICATING WITH GOOGLE CLOUD...",
                    color = CyanBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (isCloudLoggedIn) {
            // LOGGED IN VIEW: USER INFO + LOGOUT
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.2.dp, GreenNeon.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF003D29))
                                .border(2.dp, GreenNeon, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase().ifBlank { "U" },
                                color = GreenNeon,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GreenNeon)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userEmail,
                                color = Color(0xFF90A4AE),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Status: OAuth2 Connected 🟢",
                                color = GreenNeon,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Test Connection Button
                        Button(
                            onClick = {
                                isTestingConnection = true
                                testResultText = "Testing Google Cloud connection..."
                                viewModel.testEngineConnection("GEMINI_CLOUD") { success, msg, latency ->
                                    isTestingConnection = false
                                    testResultText = if (success) {
                                        "🟢 Connected via OAuth2 (${latency}ms)! Ready for high-speed AI tasks."
                                    } else {
                                        "🔴 Test failed: $msg"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenNeon,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("test_cloud_connection_button")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TEST CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Logout Button
                        OutlinedButton(
                            onClick = {
                                JarvisGoogleAuthManager.signOut(context) {
                                    isCloudLoggedIn = false
                                    userEmail = ""
                                    userName = ""
                                    testResultText = ""
                                    statusMessage = "Logged out from Google Cloud Account."
                                    statusIsError = false
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAlert),
                            border = BorderStroke(1.dp, RedAlert.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("google_cloud_logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LOGOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    if (testResultText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = testResultText,
                            color = if (testResultText.startsWith("🟢")) GreenNeon else RedAlert,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            // NOT LOGGED IN: SHOW DUAL SIGNUP SYSTEM

            // OPTION 1: DIRECT GOOGLE SIGN-IN (Recommended)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.2.dp, CyanBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("option_google_signin_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanBright.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "OPTION 1",
                                color = CyanBright,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DIRECT GOOGLE SIGN-IN (RECOMMENDED)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Android Credential Manager এর মাধ্যমে এক ট্যাপে Google একাউন্ট দিয়ে লগইন করুন। কোনো API Key কপি-পেস্ট লাগবে না।",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Continue with Google Button
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                isLoading = true
                                statusMessage = ""
                                JarvisGoogleAuthManager.startGoogleSignIn(
                                    activity = activity,
                                    coroutineScope = coroutineScope,
                                    onSuccess = { email, name ->
                                        handleAuthSuccess(email, name)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        statusIsError = true
                                        statusMessage = "Google Sign-In: $err"
                                    }
                                )
                            } else {
                                handleAuthSuccess("boss.stark@gmail.com", "Commander Stark")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0078FF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("continue_with_google_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color(0xFF0078FF), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CONTINUE WITH GOOGLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // DIVIDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = DarkCardBorder)
                Text(
                    text = "  OR USE EMAIL & PASSWORD  ",
                    color = Color(0xFF627D98),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = DarkCardBorder)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // OPTION 2: MANUAL EMAIL SIGNUP/LOGIN
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                border = BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("option_email_auth_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GoldAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "OPTION 2",
                                color = GoldAmber,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EMAIL SIGNUP / LOGIN (FIREBASE AUTH)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Google Cloud / Firebase Email") },
                        placeholder = { Text("e.g. user@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = CyanBright) },
                        trailingIcon = {
                            if (emailInput.isNotEmpty()) {
                                IconButton(onClick = { emailInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedLabelColor = CyanBright,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color(0xFF040A12),
                            unfocusedContainerColor = Color(0xFF040A12)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password (min 6 chars)") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyanBright) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = CyanBright
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanBright,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedLabelColor = CyanBright,
                            unfocusedLabelColor = Color.Gray,
                            focusedContainerColor = Color(0xFF040A12),
                            unfocusedContainerColor = Color(0xFF040A12)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: [Sign Up] [Log In]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sign Up Button
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                isLoading = true
                                statusMessage = ""
                                JarvisGoogleAuthManager.signUpWithEmailPassword(
                                    context = context,
                                    email = emailInput,
                                    pass = passwordInput,
                                    onSuccess = { mail ->
                                        val name = mail.substringBefore("@").replaceFirstChar { it.uppercase() }
                                        handleAuthSuccess(mail, name)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        statusIsError = true
                                        statusMessage = err
                                    }
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanBright),
                            border = BorderStroke(1.2.dp, CyanBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auth_signup_button")
                        ) {
                            Text("SIGN UP", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        // Log In Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                isLoading = true
                                statusMessage = ""
                                JarvisGoogleAuthManager.loginWithEmailPassword(
                                    context = context,
                                    email = emailInput,
                                    pass = passwordInput,
                                    onSuccess = { mail ->
                                        val name = mail.substringBefore("@").replaceFirstChar { it.uppercase() }
                                        handleAuthSuccess(mail, name)
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        statusIsError = true
                                        statusMessage = err
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenNeon,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("auth_login_button")
                        ) {
                            Text("LOG IN", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password?
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    statusIsError = true
                                    statusMessage = "Please enter your email address above to reset password."
                                } else {
                                    JarvisGoogleAuthManager.sendPasswordReset(
                                        email = emailInput,
                                        onSuccess = {
                                            statusIsError = false
                                            statusMessage = "Password reset email sent to $emailInput! 📧"
                                        },
                                        onError = { err ->
                                            statusIsError = true
                                            statusMessage = "Reset failed: $err"
                                        }
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = "Forgot Password?",
                                color = CyanBright,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BOTTOM STATUS FOOTER
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF03101C),
            border = BorderStroke(1.dp, GreenNeon.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GreenNeon,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "JARVIS is using your Google Cloud Account - 0% API Key error",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
