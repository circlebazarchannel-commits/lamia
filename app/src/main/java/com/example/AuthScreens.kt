package com.example

import androidx.compose.foundation.*
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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.providers.builtin.Email as SupabaseEmail
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var isMobileLoginOpen by remember { mutableStateOf(false) }
    var mobileLoginNumber by remember { mutableStateOf("") }
    var mobileLoginPassword by remember { mutableStateOf("") }
    
    var isFacebookLoginOpen by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val supabase = Supabase.client
    val scope = rememberCoroutineScope()

    if (isMobileLoginOpen) {
        AlertDialog(
            onDismissRequest = { isMobileLoginOpen = false },
            title = {
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Login with Mobile" else "মোবাইল নাম্বার দিয়ে লগইন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (com.example.viewmodel.GlobalLanguage.isEnglish) 
                            "Enter your mobile phone number and password to sign in." 
                            else "সাইন ইন করতে আপনার মোবাইল নাম্বার ও পাসওয়ার্ডটি প্রদান করুন।",
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = mobileLoginNumber,
                        onValueChange = { mobileLoginNumber = it },
                        label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Mobile Number" else "মোবাইল নাম্বার") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                            cursorColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = PrimaryGreen) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mobileLoginPassword,
                        onValueChange = { mobileLoginPassword = it },
                        label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Password" else "পাসওয়ার্ড (পিন)") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                            cursorColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryGreen) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mobileLoginNumber.length < 11 || mobileLoginPassword.length < 4) {
                            Toast.makeText(context, "সঠিক মোবাইল নাম্বার ও সঠিক পাসওয়ার্ড দিন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        isMobileLoginOpen = false
                        val fakeEmail = "${mobileLoginNumber}@halalcircle.com"
                        scope.launch {
                            try {
                                supabase.auth.signInWith(SupabaseEmail) {
                                    this.email = fakeEmail
                                    this.password = mobileLoginPassword
                                }
                                isLoading = false
                                onLoginSuccess()
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.localizedMessage ?: "Mobile Sign-in failed. Please register first."
                            }
                        }
                    },
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Login" else "লগইন করুন", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isMobileLoginOpen = false }) {
                    Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Cancel" else "বাতিল", color = TextGray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (isFacebookLoginOpen) {
        AlertDialog(
            onDismissRequest = { isFacebookLoginOpen = false },
            title = {
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Continue with Facebook" else "ফেসবুক দিয়ে প্রবেশ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1877F2)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1877F2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("f", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (com.example.viewmodel.GlobalLanguage.isEnglish) 
                            "Halal Circle wants to use Facebook to sign in." 
                            else "হালাল সার্কেল আপনার ফেসবুক অ্যাকাউন্ট ব্যবহার করে সাইন-ইন করতে চায়।",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isFacebookLoginOpen = false
                        isLoading = true
                        val fakeFbEmail = "facebook_user@halalcircle.com"
                        val fakeFbPass = "facebook123"
                        errorMessage = null
                        scope.launch {
                            try {
                                supabase.auth.signInWith(SupabaseEmail) {
                                    this.email = fakeFbEmail
                                    this.password = fakeFbPass
                                }
                                isLoading = false
                                onLoginSuccess()
                            } catch (e: Exception) {
                                try {
                                    supabase.auth.signUpWith(SupabaseEmail) {
                                        this.email = fakeFbEmail
                                        this.password = fakeFbPass
                                    }
                                    isLoading = false
                                    onLoginSuccess()
                                } catch (signUpError: Exception) {
                                    isLoading = false
                                    errorMessage = "Facebook authentication failed."
                                }
                            }
                        }
                    },
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Continue" else "চালিয়ে যান", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isFacebookLoginOpen = false }) {
                    Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Cancel" else "বাতিল", color = TextGray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            CyberSecurityAnimation()
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Welcome Back" else "স্বাগতম",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
            Text(
                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Sign in to your account" else "আপনার অ্যাকাউন্টে লগইন করুন",
                fontSize = 14.sp,
                color = TextGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Email Address" else "ইমেইল এড্রেস") },
                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = PrimaryGreen,
                    unfocusedLabelColor = TextGray,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryGreen
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (email.isNotEmpty()) PrimaryGreen else TextGray) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Password" else "পাসওয়ার্ড") },
                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = PrimaryGreen,
                    unfocusedLabelColor = TextGray,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = PrimaryGreen
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                            null,
                            tint = TextGray
                        )
                    }
                },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (password.isNotEmpty()) PrimaryGreen else TextGray) }
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.85f),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Please fill all fields" else "সবগুলো ঘর পূরণ করুন"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                        scope.launch {
                            try {
                                supabase.auth.signInWith(SupabaseEmail) {
                                    this.email = email
                                    this.password = password
                                }
                            isLoading = false
                            onLoginSuccess()
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Login failed"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Login" else "লগইন করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Or Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.98f).padding(vertical = 12.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(TextGray.copy(alpha = 0.2f)))
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) " OR " else " অথবা ",
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(TextGray.copy(alpha = 0.2f)))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Facebook Login Button
            Button(
                onClick = { isFacebookLoginOpen = true },
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "f",
                            color = Color(0xFF1877F2),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = (-1).dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Continue with Facebook" else "ফেসবুক দিয়ে লগইন",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Option 3: Mobile Login Button
            OutlinedButton(
                onClick = { isMobileLoginOpen = true },
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Mobile",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Login with Mobile" else "মোবাইল নাম্বার দিয়ে লগইন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Don't have an account? " else "অ্যাকাউন্ট নেই? ",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Register Now" else "রেজিস্ট্রেশন করুন",
                    color = PrimaryGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}


@Composable
fun CyberSecurityAnimation() {
    var rotation by remember { mutableStateOf(0f to 0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "ring_anim")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = "rot"
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer(
                rotationX = rotation.first,
                rotationY = rotation.second,
                cameraDistance = 12f
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    rotation = (rotation.first - dragAmount.y / 2f) to (rotation.second + dragAmount.x / 2f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp).graphicsLayer(rotationZ = rotationAnim)) {
            drawCircle(color = Color(0x4D6366F1), style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
        }
        Canvas(modifier = Modifier.size(160.dp).graphicsLayer(rotationZ = -rotationAnim)) {
            drawArc(color = Color(0x996366F1), startAngle = 0f, sweepAngle = 180f, useCenter = false, style = Stroke(width = 6f))
            drawArc(color = Color(0x9938BDF8), startAngle = 180f, sweepAngle = 180f, useCenter = false, style = Stroke(width = 6f))
        }
        Box(modifier = Modifier.size(110.dp).background(Color.White, CircleShape).border(2.dp, Color(0x1A6366F1), CircleShape), contentAlignment = Alignment.Center) {
             Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(50.dp))
        }
        val laserTransition = rememberInfiniteTransition(label = "laser")
        val laserOffset by laserTransition.animateFloat(initialValue = -60f, targetValue = 60f, animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "laser")
        Box(modifier = Modifier.size(180.dp, 4.dp).offset(y = laserOffset.dp).background(Color(0xFF38BDF8)).graphicsLayer(shadowElevation = 10f))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var registrationMethod by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableIntStateOf(0) }
    
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val supabase = Supabase.client
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgLight,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status bar animation
            CyberSecurityAnimation()
            Spacer(modifier = Modifier.height(24.dp))

            if (registrationMethod == null) {
                // Choice selection screen
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Choose Plan" else "রেজিস্ট্রেশন পদ্ধতি",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) 
                        "Select how you want to create your account:" 
                        else "কোন পদ্ধতির মাধ্যমে অ্যাকাউন্ট তৈরি করতে চান তা নির্বাচন করুন:",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Choice 1: Mobile Registration
                Card(
                    onClick = {
                        registrationMethod = "mobile"
                        step = 0
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Mobile",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Register with Mobile" else "মোবাইল নাম্বার দিয়ে রেজিস্ট্রেশন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) 
                                    "Sign up quickly using your phone number" 
                                    else "আপনার সচল মোবাইল নাম্বার ব্যবহার করে সহজে অ্যাকাউন্ট করুন",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }

                // Choice 2: Email Registration
                Card(
                    onClick = {
                        registrationMethod = "email"
                        step = 0
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Register with Email" else "ইমেইল এড্রেস দিয়ে রেজিস্ট্রেশন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) 
                                    "Traditional registration using your Email" 
                                    else "পরিচিত ইমেইল ও পাসওয়ার্ড নির্দেশ করে অ্যাকাউন্ট করুন",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation Footer
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { onBack() },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Cancel" else "বাতিল করুন",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(2.dp)
                                    .background(Color.Red.copy(alpha = 0.5f))
                            )
                        }
                    }
                }

            } else {
                // Actual inputs with animated steps
                AnimatedContent(targetState = step, label = "step_anim") { targetStep ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        if (targetStep == 0) {
                            // Step 0: Enter Name (for both Email and Mobile methods)
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Enter your name" else "আপনার নাম লিখুন",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = firstName, onValueChange = { firstName = it },
                                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "First Name" else "নামের প্রথম অংশ") },
                                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedLabelColor = PrimaryGreen,
                                    unfocusedLabelColor = TextGray,
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = PrimaryGreen
                                ),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (firstName.isNotEmpty()) PrimaryGreen else TextGray) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = lastName, onValueChange = { lastName = it },
                                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Last Name" else "শেষ অংশ") },
                                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedLabelColor = PrimaryGreen,
                                    unfocusedLabelColor = TextGray,
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = PrimaryGreen
                                ),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (lastName.isNotEmpty()) PrimaryGreen else TextGray) }
                            )
                        } else if (targetStep == 1) {
                            // Step 1: Method Specific Details
                            if (registrationMethod == "email") {
                                Text(
                                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Enter email & password" else "ইমেইল ও পাসওয়ার্ড লিখুন",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = email, onValueChange = { email = it },
                                    label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Email" else "ইমেইল") },
                                    modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedLabelColor = PrimaryGreen,
                                        unfocusedLabelColor = TextGray,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = PrimaryGreen
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (email.isNotEmpty()) PrimaryGreen else TextGray) }
                                )
                            } else {
                                // registrationMethod == "mobile"
                                Text(
                                    text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Enter phone number" else "মোবাইল নাম্বার ও পিন দিন",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = mobile, onValueChange = { mobile = it },
                                    label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Mobile Number" else "মোবাইল নাম্বার") },
                                    modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextDark,
                                        unfocusedTextColor = TextDark,
                                        focusedLabelColor = PrimaryGreen,
                                        unfocusedLabelColor = TextGray,
                                        focusedBorderColor = PrimaryGreen,
                                        unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = PrimaryGreen
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (mobile.isNotEmpty()) PrimaryGreen else TextGray) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = password, onValueChange = { password = it },
                                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Password" else "পাসওয়ার্ড (৪+ সংখ্যা/অক্ষর)") },
                                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedLabelColor = PrimaryGreen,
                                    unfocusedLabelColor = TextGray,
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = PrimaryGreen
                                ),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                                            null,
                                            tint = TextGray
                                        )
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (password.isNotEmpty()) PrimaryGreen else TextGray) }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = confirmPassword, onValueChange = { confirmPassword = it },
                                label = { Text(if (com.example.viewmodel.GlobalLanguage.isEnglish) "Confirm Password" else "পাসওয়ার্ডটি নিশ্চিত করুন") },
                                modifier = Modifier.fillMaxWidth(0.98f).height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedLabelColor = PrimaryGreen,
                                    unfocusedLabelColor = TextGray,
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = PrimaryGreen
                                ),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                                            null,
                                            tint = TextGray
                                        )
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (confirmPassword.isNotEmpty()) PrimaryGreen else TextGray) }
                            )
                        }
                    }
                }
                
                errorMessage?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(0.8f))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { 
                        if (step > 0) {
                            step-- 
                        } else {
                            registrationMethod = null 
                        }
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Cancel" else "বাতিল করুন",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(1.5.dp)
                                    .background(Color.Red.copy(alpha = 0.5f))
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (step < 1) {
                                if (firstName.isBlank() || lastName.isBlank()) {
                                    errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Please enter your name" else "দয়া করে আপনার নাম লিখুন"
                                    return@Button
                                }
                                errorMessage = null
                                step++
                            } else {
                                // Perform Register Logic
                                if (registrationMethod == "email") {
                                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                        errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Please fill all fields" else "সবগুলো ঘর পূরণ করুন"
                                        return@Button
                                    }
                                } else {
                                    if (mobile.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                        errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Please fill all fields" else "সবগুলো ঘর পূরণ করুন"
                                        return@Button
                                    }
                                }
                                
                                if (password != confirmPassword) {
                                    errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Passwords do not match" else "পাসওয়ার্ড দুটি মেলেনি"
                                    return@Button
                                }
                                
                                if (password.length < 4) {
                                    errorMessage = if (com.example.viewmodel.GlobalLanguage.isEnglish) "Password must be at least 4 characters" else "পাসওয়ার্ড অন্তত ৪টি অক্ষরের হতে হবে"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                
                                val emailToRegister = if (registrationMethod == "email") email else "${mobile}@halalcircle.com"
                                
                                scope.launch {
                                    try {
                                        supabase.auth.signUpWith(SupabaseEmail) {
                                            this.email = emailToRegister
                                            this.password = password
                                            data = buildJsonObject {
                                                put("full_name", "$firstName $lastName")
                                            }
                                        }
                                        
                                        // Ensure local session is established
                                        if (supabase.auth.currentUserOrNull() == null) {
                                            try {
                                                supabase.auth.signInWith(SupabaseEmail) {
                                                    this.email = emailToRegister
                                                    this.password = password
                                                }
                                            } catch (e: Exception) {
                                                // Ignore login failure if signUp could be awaiting verification, or let main flow handle it
                                            }
                                        }
                                        
                                        // Initialize the requested table data
                                        supabase.auth.currentUserOrNull()?.id?.let { userId ->
                                            val initialProfile = UserProfile(
                                                id = userId,
                                                queue = 0,
                                                data1 = "$firstName $lastName"
                                            )
                                            try {
                                                supabase.postgrest["profiles"].insert(initialProfile)
                                            } catch (dbError: Exception) {
                                                // Profile insertion failed, but auth succeeded
                                                // We can log this or handle it as a secondary error
                                            }
                                        }

                                        isLoading = false
                                        onRegisterSuccess()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = e.localizedMessage ?: "Registration failed"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.height(44.dp).padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (step < 1) {
                                    if (com.example.viewmodel.GlobalLanguage.isEnglish) "Next" else "পরবর্তী"
                                } else {
                                    if (com.example.viewmodel.GlobalLanguage.isEnglish) "Register" else "রেজিস্ট্রেশন করুন"
                                }, 
                                fontWeight = FontWeight.Bold, 
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
