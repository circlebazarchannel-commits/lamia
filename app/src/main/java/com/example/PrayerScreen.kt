package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ViewState
import com.example.viewmodel.GlobalLanguage

data class PrayerQuad(val id: String, val name: String, val startTime: String, val endTime: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    state: ViewState,
    onBack: () -> Unit,
    onToggleAlarm: (String) -> Unit,
    onOpenAlarmPage: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgLight,
        topBar = {
            TopAppBar(
                title = { Text(if (GlobalLanguage.isEnglish) "Prayer Times" else "সালাতের সময়", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAlarmPage) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Alarms", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Header Section
            PrayerHeaderCard(state)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (GlobalLanguage.isEnglish) "Daily Schedule" else "প্রতিদিনের সময়সূচী",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Prayer List
            val times = state.prayerTimes
            if (times != null) {
                val prayers = listOf(
                    PrayerQuad("Fajr", if (GlobalLanguage.isEnglish) "Fajr" else "ফজর", times.fajr, times.sunrise),
                    PrayerQuad("Dhuhr", if (GlobalLanguage.isEnglish) "Dhuhr" else "যোহর", times.dhuhr, times.asr),
                    PrayerQuad("Asr", if (GlobalLanguage.isEnglish) "Asr" else "আসর", times.asr, times.maghrib),
                    PrayerQuad("Maghrib", if (GlobalLanguage.isEnglish) "Maghrib" else "মাগরিব", times.maghrib, times.isha),
                    PrayerQuad("Isha", if (GlobalLanguage.isEnglish) "Isha" else "এশা", times.isha, times.fajr)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(prayers) { _, p ->
                        val isActive = p.id == state.currentPrayerName
                        val isAlarmOn = state.alarms[p.id] == true
                        UnifiedPrayerRow(
                            name = p.name,
                            startTime = p.startTime,
                            endTime = p.endTime,
                            isActive = isActive,
                            isAlarmOn = isAlarmOn,
                            onToggleAlarm = { onToggleAlarm(p.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerHeaderCard(state: ViewState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PrimaryGreen)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                state.locationName,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                state.nextPrayerRemaining,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (GlobalLanguage.isEnglish) "Remaining for next prayer" else "পরবর্তী ওয়াক্তের বাকি সময়",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (GlobalLanguage.isEnglish) "Current" else "বর্তমান",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        state.currentPrayerNameBen.ifEmpty { "--" },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ContainerDivider()
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (GlobalLanguage.isEnglish) "Next" else "পরবর্তী",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        state.nextPrayerNameBen.ifEmpty { "--" },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ContainerDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
fun UnifiedPrayerRow(
    name: String,
    startTime: String,
    endTime: String,
    isActive: Boolean,
    isAlarmOn: Boolean,
    onToggleAlarm: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) PrimaryGreen.copy(alpha = 0.1f) else Color.White,
        label = "bgColor"
    )
    val borderColor = if (isActive) PrimaryGreen.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                color = if (isActive) PrimaryGreen else TextDark,
                fontSize = 18.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$startTime - $endTime",
                color = if (isActive) PrimaryGreen.copy(alpha = 0.8f) else TextGray,
                fontSize = 14.sp
            )
        }

        IconButton(
            onClick = onToggleAlarm,
            modifier = Modifier
                .background(
                    if (isAlarmOn) PrimaryGreen.copy(alpha = 0.1f) else BgLight,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isAlarmOn) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                contentDescription = if (isAlarmOn) "Alarm On" else "Alarm Off",
                tint = if (isAlarmOn) PrimaryGreen else TextGray
            )
        }
    }
}
