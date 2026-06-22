package com.example

import android.Manifest
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.bangladeshDistricts
import com.example.viewmodel.District
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationSelectionScreen(
    viewModel: PrayerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var isManualSelectionOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Setup Accompanist Location Permissions State
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Trigger state changes when permissions are granted by user
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted && state.isAutoLocation) {
            viewModel.startLocationUpdates(context)
        }
    }

    if (isManualSelectionOpen) {
        // --- 1. SEARCH/MANUAL LOCATION SELECTION CHANNELS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "জেলা বা শহর খুঁজুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { isManualSelectionOpen = false }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
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
            ) {
                // Search Bar field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("জেলা বা শহরের নাম (বাংলা/ইংরেজি) লিখুন...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedLabelColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                val filteredDistricts = remember(searchQuery) {
                    if (searchQuery.isBlank()) {
                        bangladeshDistricts
                    } else {
                        bangladeshDistricts.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.englishName.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                if (filteredDistricts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "দুঃখিত, কোনো স্থান খুঁজে পাওয়া যায়নি।",
                                color = TextGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "বাংলাদেশের জেলা ও শহরসমূহ (${filteredDistricts.size} টি পাওয়া গেছে)",
                                fontWeight = FontWeight.SemiBold,
                                color = TextGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        items(filteredDistricts) { district ->
                            val isSelected = !state.isAutoLocation && state.selectedDistrict == district.name
                            Surface(
                                onClick = {
                                    viewModel.setLocationManually(context, district.name, district.lat, district.lng)
                                    isManualSelectionOpen = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else Color.White,
                                border = if (isSelected) BorderStroke(1.5.dp, PrimaryGreen) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                shadowElevation = 1.dp
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
                                            .background(
                                                color = if (isSelected) PrimaryGreen.copy(alpha = 0.15f) else Color(0xFFF3F4F6),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationCity,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryGreen else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = district.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) PrimaryGreen else TextDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = district.englishName,
                                            fontSize = 12.sp,
                                            color = TextGray
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- 2. LOCATION DASHBOARD PAGE WITH GOOGLE MAP AND CONTROLS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("লোকেশন সেট করুন", fontWeight = FontWeight.Bold, color = TextDark) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF9FAFB)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Beautiful informational header card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                                    .size(52.dp)
                                    .background(PrimaryGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "লোকেশন সেটিংস",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (state.isAutoLocation) "বর্তমান অবস্থান (জিপিএস অনুযায়ী আপডেট)" else "ম্যানুয়ালি নির্বাচিত লোকেশন",
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }

                // Interactive Dynamic Google Map Container
                item {
                    Text(
                        text = "ম্যাপ লোকেশন ভিউ",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray,
                        fontSize = 13.sp
                    )

                    val mapCenter = LatLng(state.latitude, state.longitude)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(mapCenter, 11f)
                    }

                    // Fluidly animate camera when selection modifications happen
                    LaunchedEffect(state.latitude, state.longitude) {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(mapCenter, 11f)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val playServicesAvailable = remember {
                                try {
                                    val availability = GoogleApiAvailability.getInstance()
                                    availability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
                                } catch (e: Throwable) {
                                    false
                                }
                            }

                            if (playServicesAvailable) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    onMapClick = { latLng ->
                                        // Clicking on the map places a pin and updates location manually
                                        viewModel.setLocationManually(
                                            context = context,
                                            districtName = "নির্দিষ্ট এলাকা (${String.format(java.util.Locale.US, "%.2f", latLng.latitude)}, ${String.format(java.util.Locale.US, "%.2f", latLng.longitude)})",
                                            lat = latLng.latitude,
                                            lng = latLng.longitude
                                        )
                                    },
                                    uiSettings = MapUiSettings(
                                        myLocationButtonEnabled = true,
                                        zoomControlsEnabled = false,
                                        compassEnabled = true
                                    )
                                ) {
                                    Marker(
                                        state = MarkerState(position = mapCenter),
                                        title = state.locationName,
                                        snippet = "বর্তমান নির্বাচিত জায়গা"
                                    )
                                }
                            } else {
                                // Beautiful stylized offline fallback view
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(com.example.ui.theme.BgLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Directions,
                                            contentDescription = null,
                                            tint = PrimaryGreen,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "ইন্টারেক্টিভ ম্যাপ ভিউ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextDark
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${state.locationName} (${String.format(java.util.Locale.US, "%.4f", state.latitude)}, ${String.format(java.util.Locale.US, "%.4f", state.longitude)})",
                                            fontSize = 12.sp,
                                            color = TextGray,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "প্লে সার্ভিস পাওয়া যায়নি বা অফলাইন মোডে আছে",
                                            fontSize = 10.sp,
                                            color = TextGray.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Small overlay badge inside map showing coordinates
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "Lat: ${String.format(java.util.Locale.US, "%.4f", state.latitude)}, Lng: ${String.format(java.util.Locale.US, "%.4f", state.longitude)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Current Location Badge & Info Bar
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3F4F6),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "নির্বাচিত স্থান:",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = state.locationName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryGreen,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                // The Two Main Action Paths Section
                item {
                    Text(
                        text = "লোকেশন সনাক্তকরণ অপশনসমূহ",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }

                // OPTION 1: GPS Automatic Location Detection
                item {
                    val hasGpsPermission = locationPermissionsState.allPermissionsGranted
                    Card(
                        onClick = {
                            if (hasGpsPermission) {
                                viewModel.setAutoLocation(context)
                            } else {
                                viewModel.setAutoLocation(context) // triggers flag
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.isAutoLocation) Color.White else Color.White
                        ),
                        border = if (state.isAutoLocation) BorderStroke(1.5.dp, PrimaryGreen) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (state.isAutoLocation) PrimaryGreen.copy(alpha = 0.15f) else Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = if (state.isAutoLocation) PrimaryGreen else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "জিপিএস লোকেশন সনাক্তকরণ (GPS Auto)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (hasGpsPermission) "জিপিএস ব্যবহার করে বর্তমান অবস্থান অটো খুঁজুন" else "লোকেশন পারমিশন দিয়ে অটো সনাক্ত করুন",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }

                            if (state.isAutoLocation) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // OPTION 2: Manual Location Selection
                item {
                    Card(
                        onClick = { isManualSelectionOpen = true },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = if (!state.isAutoLocation) BorderStroke(1.5.dp, PrimaryGreen) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (!state.isAutoLocation) PrimaryGreen.copy(alpha = 0.15f) else Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (!state.isAutoLocation) PrimaryGreen else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ম্যানুয়ালি লোকেশন নির্বাচন (Manual Select)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "বাংলাদেশের সকল জেলা ও শহরের তালিকা থেকে বাছুন",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }

                            if (!state.isAutoLocation) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Helpful religious location encouragement banner
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(PrimaryGreen, Color(0xFF0F766E))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "“সালাত যথারীতি মুমিনদের জন্য নির্ধারিত সময়ে আদায় করা আবশ্যক।”",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— সূরা আন-নিসা: ১০৩",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
