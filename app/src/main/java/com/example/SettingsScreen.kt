package com.example

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LocalAppStrings
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.viewmodel.AppLanguage
import com.example.viewmodel.SettingsViewModel

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    prayerAlarms: Map<String, Boolean>,
    onTogglePrayerAlarm: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentLanguage by viewModel.language.collectAsState()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    
    val selectedAdhan by viewModel.selectedAdhan.collectAsState()
    val customAdhanName by viewModel.customAdhanName.collectAsState()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsState()
    val customLogoPath by viewModel.customLogoPath.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPreview()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectCustomAdhan(context, it) }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectCustomLogo(context, it) }
    }

    val isEn = currentLanguage == AppLanguage.ENGLISH

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.app_settings, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Beautiful Spiritual & Aesthetic Poster/Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .padding(bottom = 20.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(PrimaryGreen, Color(0xFF0D9488))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Decorative semi-transparent graphic circles
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-15).dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.08f), androidx.compose.foundation.shape.CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (25).dp, y = (25).dp)
                        .background(Color.White.copy(alpha = 0.07f), androidx.compose.foundation.shape.CircleShape)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isEn) "Notifications & Sound Settings" else "রিমাইন্ডার ও আযান সেটিংস",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEn) "Configure your daily salah notification alerts and customized adhan audio tones beautifully." 
                                   else "প্রতিদিনের সালাতের সঠিক সময় নোটিফিকেশন অ্যালার্ট এবং আযান সাউন্ড টিউনসমূহ সুন্দরভাবে আপনার মোবাইল ডিভাইসে সেট করুন।",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }

            // New Section: App Branding / Logo Update
            Text(
                text = if (isEn) "App Branding" else "অ্যাপ ব্রান্ডিং (লোগো পরিবর্তন)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                onClick = { logoPickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customLogoPath.isNotEmpty()) {
                            AsyncImage(
                                model = customLogoPath,
                                contentDescription = "Custom Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo_custom),
                                contentDescription = "Default Logo",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Update App Logo" else "অ্যাপের লোগো পরিবর্তন করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Text(
                            text = if (isEn) "Choose an image to use as your app's main logo everywhere internal." 
                                   else "আপনার পছন্দমতো একটি ছবি আপলোড করুন যা অ্যাপের ভেতরে সব জায়গায় লোগো হিসেবে ব্যবহৃত হবে।",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Text(
                strings.select_language,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LanguageOption(
                language = AppLanguage.BENGALI,
                isSelected = currentLanguage == AppLanguage.BENGALI,
                onClick = { viewModel.setLanguage(AppLanguage.BENGALI) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LanguageOption(
                language = AppLanguage.ENGLISH,
                isSelected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isEn) "Prayer Time Alerts" else "ওয়াক্তভিত্তিক নোটিফিকেশন অ্যালার্ট",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val prayerList = listOf(
                "Fajr" to (if (isEn) "Fajr Alert" else "ফজর অ্যালার্ট"),
                "Sunrise" to (if (isEn) "Sunrise Alert" else "সূর্যোদয় অ্যালার্ট"),
                "Dhuhr" to (if (isEn) "Dhuhr Alert" else "যোহর অ্যালার্ট"),
                "Asr" to (if (isEn) "Asr" else "আসর অ্যালার্ট"),
                "Maghrib" to (if (isEn) "Maghrib" else "মাগরিব অ্যালার্ট"),
                "Isha" to (if (isEn) "Isha" else "এশা অ্যালার্ট")
            )

            prayerList.forEach { (prayerId, displayName) ->
                val isEnabled = prayerAlarms[prayerId] ?: (prayerId != "Sunrise")
                Surface(
                    onClick = { onTogglePrayerAlarm(prayerId) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { onTogglePrayerAlarm(prayerId) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isEn) "Adhan Sound (Prayer Alerts)" else "আযান সাউন্ড (সালাত রিমাইন্ডার সাউন্ড)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val adhanOptions = listOf(
                Triple("pleasant", if (isEn) "System Notification Tone" else "সিস্টেম নোটিফিকেশন টোন", "pleasant"),
                Triple("mecca", if (isEn) "Mecca Adhan" else "মক্কার মোয়াজ্জিন আযান", "mecca"),
                Triple("medina", if (isEn) "Medina Adhan" else "মদীনার মোয়াজ্জিন আযান", "medina"),
                Triple("custom", if (isEn) "Custom Sound (Upload MP3)" else "কাস্টম সাউন্ড (এমপি৩ ফাইল আপলোড)", "custom")
            )

            adhanOptions.forEach { (key, label, type) ->
                val isSelected = selectedAdhan == key
                val isPlaying = isPlayingPreview == key

                Surface(
                    onClick = { viewModel.setSelectedAdhan(context, key) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) PrimaryGreen else Color.LightGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setSelectedAdhan(context, key) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = TextDark
                                    )
                                    if (key == "custom") {
                                        Text(
                                            text = if (customAdhanName.isNotEmpty()) customAdhanName else (if (isEn) "No custom file selected" else "কোন ফাইল আপলোড করা হয়নি"),
                                            fontSize = 12.sp,
                                            color = if (customAdhanName.isNotEmpty()) PrimaryGreen else TextGray
                                        )
                                    }
                                }
                            }

                            // Preview Button
                            IconButton(
                                onClick = { viewModel.togglePlayPreview(context, key) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Stop Preview",
                                    tint = if (isPlaying) PrimaryGreen else TextGray
                                )
                            }
                        }

                        // Upload button for Custom sound
                        if (key == "custom") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                Text(
                                    text = if (isEn) "Choose Audio File" else "অডিও ফাইল সিলেক্ট করুন",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                "${strings.version} ১.০.১",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun LanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else Color.White,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isSelected) PrimaryGreen else TextDark
            )
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
            }
        }
    }
}
