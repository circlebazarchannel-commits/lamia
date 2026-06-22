package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextGray
import com.example.ui.theme.BgLight
import android.widget.Toast
import android.content.Context
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.GlobalLanguage
import com.example.viewmodel.toBengali

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoundationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // App state
    val isEnglish = GlobalLanguage.isEnglish
    
    // Shared preferences to save donation histories
    val prefs = remember(context) { context.getSharedPreferences("foundation_prefs", Context.MODE_PRIVATE) }
    var totalDonatedAmount by remember { mutableStateOf(prefs.getInt("total_donated", 0)) }
    var donationListString by remember { mutableStateOf(prefs.getString("donations_list", "") ?: "") }

    // Form states
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var selectedAmountIndex by remember { mutableStateOf(1) } // Default to index 1 (500)
    var customAmount by remember { mutableStateOf("") }
    var selectedPaymentIndex by remember { mutableStateOf(0) } // 0: bKash, 1: Nagad, 2: Rocket
    var donorName by remember { mutableStateOf("") }
    var donorPhone by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }

    // Dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var lastDonationDetails by remember { mutableStateOf<DonationRecord?>(null) }

    // Categories
    val categoriesEn = listOf("General Sadaqah", "Flood & Disaster Relief", "Orphan & Education Support", "Masjid & Madrasah")
    val categoriesBn = listOf("সাধারণ সদাকাহ", "বন্যা ও দুর্যোগ ত্রাণ", "এতিম ও শিক্ষা সহায়তা", "মসজিদ ও মাদ্রাসা উন্নয়ন")
    val categories = if (isEnglish) categoriesEn else categoriesBn

    // Preset Amounts
    val presetAmounts = listOf(100, 500, 1000, 5000)

    // Payment Methods
    val paymentMethods = listOf(
        PaymentMethod("bKash Merchant", "01782050201", Color(0xFFE2125F)),
        PaymentMethod("Nagad Merchant", "01944112211", Color(0xFFF15922)),
        PaymentMethod("Rocket Personal", "01511223344", Color(0xFF8C3494))
    )

    // Entrance Animation State
    var animateIntro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIntro = true
    }

    val scaffoldAlpha by animateFloatAsState(
        targetValue = if (animateIntro) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEnglish) "Halal Circle Foundation" else "হালাল সার্কেল ফাউন্ডেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        },
        containerColor = BgLight,
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            
            // 1. Beautiful Hero Banner with overlay text & gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_foundation_banner),
                    contentDescription = "Halal Circle Foundation Cover Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dark elegant overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Humanity, Support, Charity" else "মানবতার সেবায় উৎসর্গীকৃত",
                        color = Color(0xFFF59E0B), // Warm Amber/Gold
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.5.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) "Extend Your Compassionate Hands" else "মানুষের তরে বাড়িয়ে দিন আপনার স্নেহের হাত",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Stats row (Beneficiaries, total raised)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stat 1: Beneficiaries
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    number = if (isEnglish) "10,000+" else "১০,০০০+",
                    label = if (isEnglish) "Families Supported" else "উপকারভোগী পরিবার",
                    icon = Icons.Outlined.People,
                    color = Color(0xFF3B82F6)
                )
                
                // Stat 2: Total Raised
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    number = if (isEnglish) "৳15.5 Lakh+" else "৳১৫.৫ লাখ+",
                    label = if (isEnglish) "Distributed Relief" else "বিতরণকৃত অনুদান",
                    icon = Icons.Outlined.MonetizationOn,
                    color = PrimaryGreen
                )

                // Stat 3: Active Campaigns
                StatItemCard(
                    modifier = Modifier.weight(1f),
                    number = if (isEnglish) "24 Districts" else "২৪টি জেলা",
                    label = if (isEnglish) "Areas Reached" else "আক্রান্ত অঞ্চল",
                    icon = Icons.Outlined.Campaign,
                    color = Color(0xFFF59E0B)
                )
            }

            // 2. Foundation Mission Overview Text
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECEFF1))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VolunteerActivism,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEnglish) "About Halal Circle Foundation" else "হালাল সার্কেল ফাউন্ডেশন সম্পর্কে",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = if (isEnglish) {
                            "We actively distribute food, clean drinking water, rescue services, medical support, and winter clothing to vulnerable people in remote regions. We focus on providing emergency relief during severe floods, river erosion, cold waves, and supporting orphans with sustained education grants."
                        } else {
                            "আমাদের মূল লক্ষ্য রিমোট বা প্রত্যন্ত অঞ্চলের বন্যা, শৈতপ্রবাহ ও প্রাকৃতিক দুর্যোগে আক্রান্ত অসহায় ও গরিব মানুষের কাছে খাদ্য সামগ্রী, সুপেয় পানি, শীতবস্ত্র এবং নগদ চিকিৎসা সহায়তা পৌঁছে দেয়া। পাশাপাশি এতিম শিশুদের দীর্ঘমেয়াদী শিক্ষা ও ভরনপোষন নিশ্চিত করা।"
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF555F6D),
                        lineHeight = 19.sp
                    )
                }
            }

            // 3. Welfare Activities Highlight Grid (Horizontal row / sliders)
            Text(
                text = if (isEnglish) "Humanitarian Missions" else "আমাদের মানবিক কার্যক্রমসমূহ",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActivityCard(
                    title = if (isEnglish) "Flood Rehabilitation" else "বন্যা ও দুর্যোগ পুনর্বাসন",
                    description = if (isEnglish) "Rebuilding destroyed houses and distributing raw dry rations & pure water." else "সদাকাহর অর্থে বিনষ্ট ঘরবাড়ি মেরামত ও শুকনো খাবার বিতরণ।",
                    icon = Icons.Outlined.Home,
                    iconBg = Color(0xFF0284C7)
                )
                
                ActivityCard(
                    title = if (isEnglish) "Winter Blanket Delivery" else "শীতবস্ত্র ও কম্বল বিতরণ",
                    description = if (isEnglish) "Distributing premium heavy blankets to shivering elders in Northern regions." else "উত্তরবঙ্গের তীব্র শীতে কাঁপতে থাকা পরিবারদের মাঝে কম্বল বিতরণ।",
                    icon = Icons.Outlined.CheckCircle,
                    iconBg = Color(0xFF8B5CF6)
                )

                ActivityCard(
                    title = if (isEnglish) "Orphan Sponsoring" else "এতিম শিশু ও শিক্ষা সহায়তা",
                    description = if (isEnglish) "Taking responsibility for shelter, nutritious food, and Islamic education structure." else "গরিব ও অসহায় এতিম শিশুদের পড়ালেখা এবং বাসস্থানের সামগ্রিক দায়িত্ব গ্রহণ।",
                    icon = Icons.Outlined.School,
                    iconBg = PrimaryGreen
                )

                ActivityCard(
                    title = if (isEnglish) "Livelihood Sewing Machines" else "অসহায় বিধবাদের স্বাবলম্বীকরণ",
                    description = if (isEnglish) "Gifting high quality sewing machines to widows and helpless sisters to generate income." else "অসহায় মা-বোনদের আয়ের কর্মসংস্থান সৃষ্টিতে নতুন সেলাই মেশিন প্রদান।",
                    icon = Icons.Outlined.SelfImprovement,
                    iconBg = Color(0xFFE11D48)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Compact donation dashboard if user has donated before
            if (totalDonatedAmount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.06f)),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) "Your Noble Donations" else "আপনার মহৎ অনুদানের পরিমাণ",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            Text(
                                text = "৳${if (isEnglish) totalDonatedAmount.toString() else totalDonatedAmount.toString().toBengali()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                        }
                        Text(
                            text = if (isEnglish) "JazakAllah! ❤️" else "জাজাকাল্লাহ! ❤️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5. Interactive Donation Panel (Modern Form)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFECEFF1))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isEnglish) "Extend Your Help (Quick Donation)" else "সহযোগিতার হাত বাড়িয়ে দিন (সহজ অনুদান)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextDark
                    )
                    
                    Text(
                        text = if (isEnglish) "Select charity sector:" else "অনুদানের ক্ষেত্র নির্বাচন করুন:",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )

                    // 1. Selector for Fund Category
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEachIndexed { idx, title ->
                            val isSelected = selectedCategoryIndex == idx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryGreen.copy(alpha = 0.08f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryGreen else Color(0xFFECEFF1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategoryIndex = idx }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedCategoryIndex = idx },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryGreen else TextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Donation Amount Selection
                    Text(
                        text = if (isEnglish) "Select donation amount (Taka):" else "অনুদানের পরিমাণ নির্বাচন করুন (টাকা):",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAmounts.forEachIndexed { index, amt ->
                            val isSelected = selectedAmountIndex == index && customAmount.isEmpty()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryGreen else Color(0xFFF3F4F6))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryGreen else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedAmountIndex = index
                                        customAmount = ""
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "৳" + (if (isEnglish) amt.toString() else amt.toString().toBengali()),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom Amount Text Field
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = {
                            customAmount = it
                            selectedAmountIndex = -1 // clear preset choice
                        },
                        label = { Text(if (isEnglish) "Or custom amount (৳)" else "অথবা কাস্টম পরিমাণ লিখুন (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Payment Methods (bKash/Nagad/Rocket Merchant accounts)
                    Text(
                        text = if (isEnglish) "Choose transaction channel:" else "অনুদান পাঠানোর মাধ্যম:",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentMethods.forEachIndexed { index, pm ->
                            val isSelected = selectedPaymentIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) pm.logoColor.copy(alpha = 0.1f) else Color(0xFFF9FAFB))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) pm.logoColor else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedPaymentIndex = index }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pm.name.split(" ")[0], // bKash, Nagad, Rocket
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = pm.logoColor
                                )
                            }
                        }
                    }

                    // Payment instructions card
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = paymentMethods[selectedPaymentIndex].logoColor.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, paymentMethods[selectedPaymentIndex].logoColor.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = if (isEnglish) {
                                    "Payment Instructions: Please send your intended money/Sadaqah to the following merchant/number first."
                                } else {
                                    "অনুদান পাঠাবোধনী: অনুগ্রহ করে আপনার অনুদান বা সাদাকাহর টাকা নিচের নাম্বারে প্রদান করুন।"
                                },
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = paymentMethods[selectedPaymentIndex].name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = paymentMethods[selectedPaymentIndex].logoColor
                                    )
                                    Text(
                                        text = paymentMethods[selectedPaymentIndex].number,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = TextDark
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = android.content.ClipData.newPlainText("Account Number", paymentMethods[selectedPaymentIndex].number)
                                        clipboardManager.setPrimaryClip(clipData)
                                        Toast.makeText(context, if (isEnglish) "Number Copied!" else "নাম্বার কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = paymentMethods[selectedPaymentIndex].logoColor),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(if (isEnglish) "Copy" else "কপি", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Input Fields (User/Sender info)
                    Text(
                        text = if (isEnglish) "Sender Verification (Mandatory):" else "প্রেরক নিশ্চিতকরণ তথ্য (আবশ্যকীয়):",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Donor Name
                    OutlinedTextField(
                        value = donorName,
                        onValueChange = { donorName = it },
                        label = { Text(if (isEnglish) "Your Name (Optional)" else "আপনার নাম (ঐচ্ছিক)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sender Phone Number
                    OutlinedTextField(
                        value = donorPhone,
                        onValueChange = { donorPhone = it },
                        label = { Text(if (isEnglish) "Sender Phone Number" else "প্রেরক মোবাইল নাম্বার") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transaction ID
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text(if (isEnglish) "Transaction ID (TrxID)" else "ট্রানজেকশন আইডি (TrxID)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            
                            val resolvedAmount = if (customAmount.isNotEmpty()) {
                                customAmount.toIntOrNull() ?: 0
                            } else {
                                presetAmounts.getOrNull(selectedAmountIndex) ?: 0
                            }

                            if (resolvedAmount <= 0) {
                                Toast.makeText(context, if (isEnglish) "Please select a valid amount!" else "অনুগ্রহ করে সঠীক অনুদানের পরিমাণ নির্বাচন করুন!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (donorPhone.trim().isEmpty() || transactionId.trim().isEmpty()) {
                                Toast.makeText(context, if (isEnglish) "Sender phone & TrxID are mandatory!" else "প্রেরক নাম্বার ও ট্রানজেকশন আইডি আবশ্যক!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Successful simulation
                            val actualName = donorName.ifEmpty { if (isEnglish) "Anonymous Donor" else "নাম প্রকাশে অনিচ্ছুক দানকারী" }
                            val newRecord = DonationRecord(
                                id = System.currentTimeMillis(),
                                donorName = actualName,
                                donorPhone = donorPhone,
                                trxId = transactionId,
                                amount = resolvedAmount,
                                category = categories[selectedCategoryIndex],
                                method = paymentMethods[selectedPaymentIndex].name.split(" ")[0],
                                date = if (isEnglish) "Just now" else "এইমাত্র"
                            )

                            // Save inside preferences
                            totalDonatedAmount += resolvedAmount
                            prefs.edit().putInt("total_donated", totalDonatedAmount).apply()

                            // Store in history list
                            val newHistoryLine = "${resolvedAmount}|${actualName}|${paymentMethods[selectedPaymentIndex].name.split(" ")[0]}|${transactionId}"
                            val updatedHistoryList = if (donationListString.isEmpty()) newHistoryLine else "$donationListString#$newHistoryLine"
                            donationListString = updatedHistoryList
                            prefs.edit().putString("donations_list", updatedHistoryList).apply()

                            lastDonationDetails = newRecord
                            showSuccessDialog = true

                            // Clear transaction form fields
                            customAmount = ""
                            donorName = ""
                            donorPhone = ""
                            transactionId = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Confirm Donation" else "অনুদান নিশ্চিত করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // 6. Display past donation receipt history if present
            if (donationListString.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isEnglish) "Your Donation History" else "আপনার পূর্ববর্তী অনুদানসমূহ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                val donationsArray = donationListString.split("#").reversed()
                donationsArray.forEach { donationRecord ->
                    val parts = donationRecord.split("|")
                    if (parts.size >= 4) {
                        val amtStr = parts[0]
                        val dName = parts[1]
                        val method = parts[2]
                        val trx = parts[3]
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, Color(0xFFECEFF1))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryGreen.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "TrxID: $trx • $method",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                                Text(
                                    text = "৳${if (isEnglish) amtStr else amtStr.toBengali()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Beautiful Donation Success Dialog with pristine visual elements
        if (showSuccessDialog && lastDonationDetails != null) {
            Dialog(onDismissRequest = { showSuccessDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pulsing heart ripple
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolunteerActivism,
                                contentDescription = "Success",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isEnglish) "Donation Submitted!" else "অনুদান জমা হয়েছে!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = PrimaryGreen,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isEnglish) {
                                "May Allah bless your generosity and accept your Sadaqah. Ameen."
                            } else {
                                "আল্লাহ আপনার দান কবুল করুন এবং দুনিয়া ও আখিরাতে এর সর্বোত্তম প্রতিফল দিন। আমীন।"
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color(0xFF555F6D),
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Brief Receipt details card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BgLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(if (isEnglish) "Category:" else "অনুদানের ক্ষেত্র:", fontSize = 11.sp, color = TextGray)
                                    Text(lastDonationDetails!!.category, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(if (isEnglish) "Amount:" else "পরিমাণ:", fontSize = 11.sp, color = TextGray)
                                    Text("৳${if (isEnglish) lastDonationDetails!!.amount.toString() else lastDonationDetails!!.amount.toString().toBengali()}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = PrimaryGreen)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(if (isEnglish) "Channel:" else " মাধ্যম:", fontSize = 11.sp, color = TextGray)
                                    Text(lastDonationDetails!!.method, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TrxID:", fontSize = 11.sp, color = TextGray)
                                    Text(lastDonationDetails!!.trxId, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showSuccessDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isEnglish) "Ameen / Close" else "আমীন / বন্ধ করুন", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Stats widget
@Composable
fun StatItemCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFECEFF1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = number,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Activity details card
@Composable
fun ActivityCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBg: Color
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(140.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(iconBg.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextDark,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = TextGray,
                lineHeight = 14.sp
            )
        }
    }
}

data class PaymentMethod(
    val name: String,
    val number: String,
    val logoColor: Color
)

data class DonationRecord(
    val id: Long,
    val donorName: String,
    val donorPhone: String,
    val trxId: String,
    val amount: Int,
    val category: String,
    val method: String,
    val date: String
)
