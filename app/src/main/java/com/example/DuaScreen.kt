package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.DuaData
import com.example.model.Dua
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val allDuas = DuaData.duaList
    val filteredDuas = if (searchQuery.isEmpty()) {
        allDuas
    } else {
        allDuas.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val groupedDuas = filteredDuas.groupBy { it.category }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "দোয়া",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BgLight)
                )
                // Search Bar
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("দোয়া খুঁজুন...", color = TextGray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PrimaryGreen
                        ),
                        singleLine = true
                    )
                }
            }
        },
        containerColor = BgLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedDuas.forEach { (category, duas) ->
                item {
                    Text(
                        text = category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                }

                items(duas) { dua ->
                    DuaListItem(dua = dua)
                }
            }
            
            if (filteredDuas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("কোনো দোয়া পাওয়া যায়নি", color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun DuaListItem(dua: Dua) {
    var expanded by remember { mutableStateOf(false) }
    var bookmarked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dua.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { bookmarked = !bookmarked },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (bookmarked) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                    contentDescription = "Bookmark",
                    tint = if (bookmarked) PrimaryGreen else TextGray
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgLight.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                // Arabic
                Text(
                    text = dua.arabic,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Pronunciation
                Text(
                    text = "উচ্চারণ: ${dua.pronunciation}",
                    fontSize = 14.sp,
                    color = TextGray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Translation
                Text(
                    text = "অর্থ: ${dua.translation}",
                    fontSize = 14.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Reference
                Text(
                    text = "সূত্র: ${dua.reference}",
                    fontSize = 12.sp,
                    color = PrimaryGreen
                )
            }
        }
        Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
    }
}
