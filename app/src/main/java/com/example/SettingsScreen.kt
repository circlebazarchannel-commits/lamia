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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val currentLanguage by viewModel.language.collectAsState()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    
    val selectedAdhan by viewModel.selectedAdhan.collectAsState()
    val customAdhanName by viewModel.customAdhanName.collectAsState()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsState()

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
