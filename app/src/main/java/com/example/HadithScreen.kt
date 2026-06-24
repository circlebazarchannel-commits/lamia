package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.data.HadithData
import com.example.model.Hadith
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }

    val allHadiths = HadithData.hadithList
    val filteredHadiths = if (searchQuery.isEmpty()) {
        allHadiths
    } else {
        allHadiths.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.translation.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            if (selectedHadith == null) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "হাদিস সমূহ",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextDark
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )

                    // Search Bar - Thinner and slightly rounded
                    Box(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp,
                            top = 4.dp
                        )
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("হাদিস খুঁজুন...", color = TextGray, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BgLight,
                                unfocusedContainerColor = BgLight,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PrimaryGreen
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        containerColor = BgLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (selectedHadith == null) paddingValues else PaddingValues(0.dp))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(filteredHadiths) { hadith ->
                    HadithListItem(hadith = hadith, onClick = { selectedHadith = hadith })
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (filteredHadiths.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("কোনো হাদিস পাওয়া যায়নি", color = TextGray)
                        }
                    }
                }
            }

            // Full Screen Hadith Detail
            selectedHadith?.let { hadith ->
                HadithDetailDialog(hadith = hadith, onDismiss = { selectedHadith = null })
            }
        }
    }
}

@Composable
fun HadithListItem(hadith: Hadith, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hadith.id.toString(),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = hadith.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = hadith.category,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Not the right icon but used for placeholder
                contentDescription = "Go",
                tint = TextGray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp).graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

@Composable
fun HadithDetailDialog(hadith: Hadith, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Text(
                    text = hadith.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic
                Text(
                    text = hadith.arabic,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 48.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Pronunciation Card
                if (hadith.pronunciation.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "উচ্চারণ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hadith.pronunciation,
                                fontSize = 16.sp,
                                color = TextDark,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Translation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), 
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "অনুবাদ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = hadith.translation,
                            fontSize = 16.sp,
                            color = TextDark,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Reference
                Text(
                    text = "সূত্র: ${hadith.reference}",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
