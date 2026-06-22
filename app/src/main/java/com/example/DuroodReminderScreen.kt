package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.DuroodHelper
import com.example.ui.theme.BgLight
import com.example.ui.theme.CardBg
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.viewmodel.GlobalLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuroodReminderScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isEnglish = GlobalLanguage.isEnglish

    // SharedPreferences state
    var isEnabled by remember { mutableStateOf(DuroodHelper.isEnabled(context)) }
    var selectedIntervalMins by remember { mutableStateOf(DuroodHelper.getIntervalMins(context)) }
    var isVoiceEnabled by remember { mutableStateOf(DuroodHelper.isVoiceEnabled(context)) }
    var selectedText by remember { mutableStateOf(DuroodHelper.getSelectedText(context)) }

    val intervalOptions = listOf(
        Triple(15, if (isEnglish) "15 Mins" else "১৫ মিনিট", Icons.Default.Schedule),
        Triple(30, if (isEnglish) "30 Mins" else "৩০ মিনিট", Icons.Default.Schedule),
        Triple(60, if (isEnglish) "1 Hour" else "১ ঘণ্টা", Icons.Default.HourglassTop),
        Triple(120, if (isEnglish) "2 Hours" else "২ ঘণ্টা", Icons.Default.HourglassBottom),
        Triple(300, if (isEnglish) "5 Hours" else "৫ ঘণ্টা", Icons.Default.Update)
    )

    val duroodTexts = listOf(
        Pair("ﷺ", if (isEnglish) "Shortest Sallallahu Alayhi Wa Sallam symbol" else "সংক্ষিপ্ত সাঃ প্রতীক"),
        Pair("সাল্লাল্লাহু আলাইহি ওয়াসাল্লাম", if (isEnglish) "Full Sallallahu Alayhi Wa Sallam" else "পূর্ণ সাল্লাল্লাহু আলাইহি ওয়াসাল্লাম"),
        Pair("আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ", if (isEnglish) "Allahumma Salli Ala Muhammad" else "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ (সংক্ষিপ্ত)"),
        Pair("আল্লাহুম্মা সাল্লি ওয়া সাল্লিম আলা নাবিয়্যিনা মুহাম্মাদ", if (isEnglish) "Allahumma Salli Wa Sallim Ala Nabiyyina Muhammad" else "নবীজির প্রতি রহমত ও শান্তির দোয়া")
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Text(
                    text = if (isEnglish) "Durood Reminder" else "দরুদ রিমাইন্ডার",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        containerColor = BgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero card calling to action / displaying Quranic/Hadith value of Durood
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryGreen.copy(alpha = 0.08f), Color.White)
                            )
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ﷺ",
                            fontSize = 32.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isEnglish) "Significance of Durood" else "দরূদের ফজিলত ও গুরুত্ব",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEnglish) {
                            "\"Whoever sends blessings upon me once, Allah will send blessings upon him ten times.\" (Sahih Muslim)"
                        } else {
                            "“যে ব্যক্তি আমার উপর একবার দরূদ পাঠ করবে, আল্লাহ তাআলা তার উপর দশটি রহমত বর্ষণ করবেন।”\n— সহীহ মুসলিম"
                        },
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Main Reminder Enable/Disable toggle card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.10f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEnglish) "Durood Reminder" else "দরুদ রিমাইন্ডার চালু করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                            Text(
                                text = if (isEnabled) {
                                    if (isEnglish) "Reminder is Active" else "রিমাইন্ডার এখন সচল আছে"
                                } else {
                                    if (isEnglish) "Reminder is Off" else "রিমাইন্ডার এখন বন্ধ আছে"
                                },
                                fontSize = 12.sp,
                                color = if (isEnabled) PrimaryGreen else TextGray
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            isEnabled = checked
                            DuroodHelper.setEnabled(context, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = BgLight
                        )
                    )
                }
            }

            // Options container (enabled/disabled states)
            if (isEnabled) {
                // Select Interval Option Section
                Text(
                    text = if (isEnglish) "Select Interval" else "রিমাইন্ডার ইন্টারভাল নির্বাচন করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        intervalOptions.forEach { option ->
                            val isSelected = selectedIntervalMins == option.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable {
                                        selectedIntervalMins = option.first
                                        DuroodHelper.setIntervalMins(context, option.first)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = option.third,
                                        contentDescription = null,
                                        tint = if (isSelected) PrimaryGreen else TextGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = option.second,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PrimaryGreen else TextDark
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Select preferred Durood text
                Text(
                    text = if (isEnglish) "Select Durood Text" else "পছন্দের দরূদ নির্বাচন করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        duroodTexts.forEach { option ->
                            val isSelected = selectedText == option.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable {
                                        selectedText = option.first
                                        DuroodHelper.setSelectedText(context, option.first)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.first,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PrimaryGreen else TextDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = option.second,
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Voice Reminder Switch
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryGreen.copy(alpha = 0.10f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVoiceEnabled) Icons.Default.RecordVoiceOver else Icons.Default.VolumeMute,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEnglish) "Voice & Sound Reminder" else "ভয়েস ও শব্দ রিমাইন্ডার",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = if (isEnglish) {
                                        "App speaks 'Durood Porun' aloud"
                                    } else {
                                        "রিমাইন্ডারে 'দরুদ পড়ুন' বলা হবে"
                                    },
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                        }

                        Switch(
                            checked = isVoiceEnabled,
                            onCheckedChange = { checked ->
                                isVoiceEnabled = checked
                                DuroodHelper.setVoiceEnabled(context, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryGreen,
                                uncheckedThumbColor = TextGray,
                                uncheckedTrackColor = BgLight
                            )
                        )
                    }
                }
            }
        }
    }
}
