package com.example.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FinanceItem
import com.example.data.ScheduleItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantApp(viewModel: AssistantViewModel) {
    // 0: Dashboard, 1: AI Copilot, 2: Schedule, 3: Wallet
    var selectedTab by remember { mutableStateOf(0) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val scheduleList by viewModel.scheduleItems.collectAsState()
    val financeList by viewModel.financeItems.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val briefingState by viewModel.briefingState.collectAsState()

    val balance by viewModel.currentBalance.collectAsState()
    val incomeSum by viewModel.totalIncome.collectAsState()
    val expenseSum by viewModel.totalExpense.collectAsState()

    val fbUser by viewModel.fbCurrentUser.collectAsState()
    var activeChatUser by remember { mutableStateOf<FirebaseUser?>(null) }

    if (fbUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BentoBg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED),
                                    BentoAccentPurple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = BentoAccentPurple,
                    strokeWidth = 2.5.dp
                )
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("app_navigation_bar"),
                    containerColor = BentoNeutralCard,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.GridView, contentDescription = "Bento Command") },
                        label = { Text("Dashboard") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPurpleText,
                            selectedTextColor = BentoAccentPurple,
                            indicatorColor = BentoPurpleBadge
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI Copilot") },
                        label = { Text("Copilot") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPurpleText,
                            selectedTextColor = BentoAccentPurple,
                            indicatorColor = BentoPurpleBadge
                        ),
                        modifier = Modifier.testTag("tab_copilot")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Agenda Tracker") },
                        label = { Text("Schedule") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPurpleText,
                            selectedTextColor = BentoAccentPurple,
                            indicatorColor = BentoPurpleBadge
                        ),
                        modifier = Modifier.testTag("tab_schedule")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Finances Ledger") },
                        label = { Text("Wallet") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPurpleText,
                            selectedTextColor = BentoAccentPurple,
                            indicatorColor = BentoPurpleBadge
                        ),
                        modifier = Modifier.testTag("tab_wallet")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Real-time Messaging") },
                        label = { Text("Messages") },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BentoPurpleText,
                            selectedTextColor = BentoAccentPurple,
                            indicatorColor = BentoPurpleBadge
                        ),
                        modifier = Modifier.testTag("tab_messages")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BentoBg)
            ) {
                when (selectedTab) {
                    0 -> DashboardTab(
                        viewModel = viewModel,
                        scheduleList = scheduleList,
                        financeList = financeList,
                        balance = balance,
                        briefingState = briefingState,
                        onNavigateToTab = { selectedTab = it }
                    )
                    1 -> CopilotTab(
                        viewModel = viewModel,
                        chatMessages = chatMessages,
                        isChatLoading = isChatLoading
                    )
                    2 -> ScheduleTab(
                        viewModel = viewModel,
                        scheduleList = scheduleList
                    )
                    3 -> WalletTab(
                        viewModel = viewModel,
                        financeList = financeList,
                        balance = balance,
                        incomeSum = incomeSum,
                        expenseSum = expenseSum
                    )
                    4 -> {
                        if (activeChatUser != null) {
                            ActiveChatScreen(
                                viewModel = viewModel,
                                otherUser = activeChatUser!!,
                                onBack = { activeChatUser = null }
                            )
                        } else {
                            MessagesTab(
                                viewModel = viewModel,
                                onOpenChat = { activeChatUser = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SUB-TAB 0: EXTREMELY POLISHED BENTO GRID DASHBOARD ====================

@Composable
fun DashboardTab(
    viewModel: AssistantViewModel,
    scheduleList: List<ScheduleItem>,
    financeList: List<FinanceItem>,
    balance: Double,
    briefingState: BriefingState,
    onNavigateToTab: (Int) -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val capturedPhoto by viewModel.capturedPhoto.collectAsState()

    // Camera Result Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setCapturedPhoto(bitmap)
            Toast.makeText(context, "Photo logged successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Permission Requester
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to capture photos.", Toast.LENGTH_LONG).show()
        }
    }

    val onCapturePhoto = {
        val permission = android.Manifest.permission.CAMERA
        val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dashboard Title & Header Block with Dicebear Style Avatar Frame
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thinkuu",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextGray,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Hi, Ashu", // Personalized based on email context
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoTextDark,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Adjust button of the dark theme
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (isDarkMode) Color.White.copy(alpha = 0.15f) else BentoNeutralCard, CircleShape)
                        .testTag("toggle_dark_theme_btn")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme Mode",
                        tint = if (isDarkMode) Color(0xFFFBBF24) else BentoAccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Make the man's photo an icon
                ManProfileIcon(modifier = Modifier.size(48.dp))
            }
        }

        // BENTO CARD 1: WALLET BALANCE (col-span-2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .clickable { onNavigateToTab(3) } // Goes to wallet tab
                .testTag("bento_card_finance"),
            colors = CardDefaults.cardColors(containerColor = BentoPurpleCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BentoAccentPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Wallet Balance Icon",
                            tint = BentoAccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Performance badge indicating balance status / cash surplus
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BentoPurpleBadge)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Status: Healthy",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPurpleText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "WALLET CASH BALANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextGray,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = CurrencyUtils.formatInr(balance),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoPurpleText,
                    letterSpacing = (-1).sp
                )
            }
        }

        // BENTO GRID ROW: SCHEDULE TALL (col-span-1) & RIGHT SPLIT COLUMN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // LEFT CARD: SCHEDULE TALL CARD (col-span-1)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable { onNavigateToTab(2) } // Goes to agenda
                    .testTag("bento_card_schedule"),
                colors = CardDefaults.cardColors(containerColor = BentoBlueCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Upcoming Events",
                            tint = BentoBlueText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "UPCOMING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoBlueText,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val pendingTasks = scheduleList.filter { !it.isCompleted }.take(3)

                    if (pendingTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "All tasks complete",
                                    tint = BentoBlueText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "All clear!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoBlueText
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pendingTasks.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            border = BorderStroke(
                                                1.dp,
                                                BentoAccentPurple.copy(alpha = 0.15f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .background(
                                            BentoBg.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.timeStr,
                                        fontSize = 10.sp,
                                        color = BentoTextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT STACKED COLUMN (col-span-1)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // RED SQUARE REMINDERS CARD
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onNavigateToTab(2) }
                        .testTag("bento_card_reminders"),
                    colors = CardDefaults.cardColors(containerColor = BentoRedCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Active Reminders Notification Status",
                                tint = BentoRedText,
                                modifier = Modifier.size(20.dp)
                            )

                            // Pulsing Alert Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BentoRedIndicator)
                            )
                        }

                        Column {
                            val activeRemindersCount = scheduleList.count { !it.isCompleted }
                            Text(
                                text = "$activeRemindersCount",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoRedText,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = "Pending Items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoRedText,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // GREEN INSIGHTS / ECO CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onNavigateToTab(3) }
                        .testTag("bento_card_insights"),
                    colors = CardDefaults.cardColors(containerColor = BentoGreenCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoGreenText.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Savings and optimization",
                                tint = BentoGreenText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "SAVINGS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoGreenText,
                                letterSpacing = 1.sp
                            )
                            val totalReserve = financeList.filter { !it.isExpense }.sumOf { it.amount } - financeList.filter { it.isExpense }.sumOf { it.amount }
                            val displayReserve = if (totalReserve > 0) totalReserve else 0.0
                            Text(
                                text = CurrencyUtils.formatInr(displayReserve),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoGreenText
                            )
                        }
                    }
                }
            }
        }

        // BENTO CARD 5: AI SMART ADVISORY BOX (col-span-2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                .clickable { onNavigateToTab(1) } // Goes to AI copilot chat
                .testTag("bento_card_ai_advice"),
            colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BentoAccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot Helper Icon Badge",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "AI Copilot Suggestion",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoAccentPurple
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    when (briefingState) {
                        is BriefingState.Success -> {
                            Text(
                                text = "\"${briefingState.content.take(100)}...\"",
                                fontSize = 11.sp,
                                color = BentoTextGray,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        is BriefingState.Loading -> {
                            Text(
                                text = "\"AI is compiling transaction ledgers and schedules...\"",
                                fontSize = 11.sp,
                                color = BentoTextGray,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        else -> {
                            Text(
                                text = "\"Ask me to add an appointment or schedule reminders to update your custom agenda in real-time.\"",
                                fontSize = 11.sp,
                                color = BentoTextGray,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // BENTO CARD 6: CAMERA / SECURE VISUAL LOG (col-span-2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, if (isDarkMode) Color(0xFF2E2A34) else BentoBorder, RoundedCornerShape(28.dp))
                .testTag("bento_card_camera"),
            colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoAccentPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera Icon",
                                tint = BentoAccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "VISUAL LOG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAccentPurple,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Camera Hub",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkMode) Color.White else BentoTextDark
                            )
                        }
                    }

                    if (capturedPhoto != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isDarkMode) Color(0xFF4C1D95) else BentoPurpleBadge)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Active Log",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkMode) Color(0xFFEADDFF) else BentoPurpleText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (capturedPhoto != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, if (isDarkMode) Color(0xFF3C3842) else Color(0xFFE5E2E9), RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                    ) {
                        Image(
                            bitmap = capturedPhoto!!.asImageBitmap(),
                            contentDescription = "Captured Visual Record",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Clear Button to clear/retake photo
                        IconButton(
                            onClick = { viewModel.setCapturedPhoto(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(32.dp)
                                .testTag("clear_captured_photo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Photo Record",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDarkMode) Color(0xFF231E29) else Color(0xFFFBF9FD))
                            .border(
                                width = 1.dp,
                                color = if (isDarkMode) Color(0xFF352F3D) else Color(0xFFEEEBF2),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("camera_placeholder_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "No photo logged icon",
                                tint = if (isDarkMode) Color.White.copy(alpha = 0.45f) else BentoTextGray.copy(alpha = 0.45f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Take a quick photographic log or diary entry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else BentoTextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onCapturePhoto() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoAccentPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("take_photo_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Trigger Camera",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (capturedPhoto != null) "Retake Photo" else "Take Photo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Manual Synchronization Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.generateDailyBriefing() },
                colors = ButtonDefaults.buttonColors(containerColor = BentoNeutralCard, contentColor = BentoTextDark),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Manual update dashboard assets", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Dashboard Services", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ==================== SUB-TAB 1: COPILOT CONVERSATION ====================

@Composable
fun CopilotTab(
    viewModel: AssistantViewModel,
    chatMessages: List<ChatMessage>,
    isChatLoading: Boolean
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val focusManager = LocalFocusManager.current
    var chatInput by remember { mutableStateOf("") }
    var showBriefingDialog by remember { mutableStateOf(false) }

    if (showBriefingDialog) {
        SmartBriefingDialog(
            state = viewModel.briefingState.collectAsState().value,
            onDismiss = { showBriefingDialog = false },
            onRefresh = { viewModel.generateDailyBriefing() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sleek Minimal Top Header Bar (matches image in user_request)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Minimalist container for "T" logo
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED), // Premium Violet
                                    BentoAccentPurple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "Thinkuu",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) Color.White else BentoTextDark
                )
            }

            // Sleek Outlined Pill Button "Smart Briefing" (unobtrusive, matches picture)
            OutlinedButton(
                onClick = { showBriefingDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDarkMode) Color(0xFFE5E2E9) else BentoAccentPurple
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDarkMode) Color(0xFF4C4554) else BentoAccentPurple.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("smart_briefing_btn")
            ) {
                Text(
                    text = "Smart Briefing",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lower Chat Thread
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkMode) Color(0xFF1E1C22) else BentoNeutralCard)
                .border(1.dp, if (isDarkMode) Color(0xFF2E2A34) else BentoBorder, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No messages yet", color = BentoTextGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    reverseLayout = true
                ) {
                    items(chatMessages.reversed()) { message ->
                        ChatBubble(message)
                    }
                }
            }

            // Inline Loader
            if (isChatLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp)
                        .background(if (isDarkMode) Color(0xFF381572) else BentoPurpleBadge, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = if (isDarkMode) Color(0xFFEADDFF) else BentoPurpleText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI is analyzing context...",
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color(0xFFEADDFF) else BentoPurpleText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Message Sending Bar: Futuristic unified rounded input bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(if (isDarkMode) Color(0xFF252129) else Color.White)
                    .border(1.dp, if (isDarkMode) Color(0xFF3C3842) else Color(0xFFE5E2E9), RoundedCornerShape(32.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { 
                        Text(
                            text = "Ask Thinkuu anything...",
                            fontSize = 14.sp,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.45f) else Color(0xFF7C7782)
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isDarkMode) Color.White else BentoTextDark,
                        unfocusedTextColor = if (isDarkMode) Color.White else BentoTextDark
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (chatInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatInput.trim())
                                chatInput = ""
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            viewModel.sendChatMessage(chatInput.trim())
                            chatInput = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED), // Premium purple-violet gradient
                                    BentoAccentPurple
                                )
                            )
                        )
                        .testTag("chat_send_button"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Assistant Quick Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { chatInput = "Remind me to consult financial advisor tomorrow at 10 AM" },
                label = { Text("Advisory Reminder", fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(labelColor = BentoPurpleText),
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoPurpleText) }
            )
            AssistChip(
                onClick = { chatInput = "Add Rent payment of 850" },
                label = { Text("Log Housing Payment", fontSize = 11.sp) },
                colors = AssistChipDefaults.assistChipColors(labelColor = BentoPurpleText),
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoPurpleText) }
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Clear Chat", modifier = Modifier.size(20.dp))
            }
        }
    }
}


// ==================== SUB-TAB 2: SCHEDULE PLANNER ====================

@Composable
fun ScheduleTab(viewModel: AssistantViewModel, scheduleList: List<ScheduleItem>) {
    var showAddDialog by remember { mutableStateOf(false) }
    val isGoogleConnected by viewModel.isGoogleConnected.collectAsState()
    val isOutlookConnected by viewModel.isOutlookConnected.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Agenda Planner",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoTextDark,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Manage your events and chores",
                        fontSize = 13.sp,
                        color = BentoTextGray
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                    modifier = Modifier.testTag("open_add_schedule_dialog_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Agenda Item", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day Progress bar styled Bento
            val completedCount = scheduleList.count { it.isCompleted }
            val totalCount = scheduleList.size
            val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BentoBlueCard),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Day Progress Tracker", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBlueText)
                        Text("$completedCount of $totalCount completed", fontSize = 12.sp, color = BentoBlueText, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BentoAccentPurple,
                        trackColor = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = BentoAccentPurple, modifier = Modifier.size(16.dp))
                            Text("Calendar Integrations", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)
                        }
                        if (isSyncing) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = BentoAccentPurple)
                                Text("Syncing...", fontSize = 10.sp, color = BentoAccentPurple, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            TextButton(
                                onClick = { viewModel.syncCalendars() },
                                modifier = Modifier.height(28.dp).testTag("btn_sync_calendars_now")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync Now", modifier = Modifier.size(12.dp), tint = BentoAccentPurple)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", fontSize = 11.sp, color = BentoAccentPurple)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isGoogleConnected) viewModel.disconnectGoogleCalendar() else viewModel.connectGoogleCalendar()
                                }
                                .testTag("btn_connect_google"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isGoogleConnected) BentoGreenText.copy(alpha = 0.08f) else BentoNeutralCard
                            ),
                            border = BorderStroke(1.dp, if (isGoogleConnected) BentoGreenText.copy(alpha = 0.3f) else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = "Google Calendar Link Status",
                                    tint = if (isGoogleConnected) BentoGreenText else BentoTextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Google",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextDark
                                    )
                                    Text(
                                        text = if (isGoogleConnected) "Connected" else "Tap to Link",
                                        fontSize = 9.sp,
                                        color = if (isGoogleConnected) BentoGreenText else BentoTextGray
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isOutlookConnected) viewModel.disconnectOutlookCalendar() else viewModel.connectOutlookCalendar()
                                }
                                .testTag("btn_connect_outlook"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOutlookConnected) BentoAccentPurple.copy(alpha = 0.08f) else BentoNeutralCard
                            ),
                            border = BorderStroke(1.dp, if (isOutlookConnected) BentoAccentPurple.copy(alpha = 0.3f) else Color.Transparent)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Outlook Calendar Link Status",
                                    tint = if (isOutlookConnected) BentoAccentPurple else BentoTextGray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "Outlook",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextDark
                                    )
                                    Text(
                                        text = if (isOutlookConnected) "Connected" else "Tap to Link",
                                        fontSize = 9.sp,
                                        color = if (isOutlookConnected) BentoAccentPurple else BentoTextGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (scheduleList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = BentoBlueText.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Your agenda is currently empty.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextDark
                        )
                        Text(
                            "Type to Thinkuu or click 'Add' to insert items.",
                            fontSize = 12.sp,
                            color = BentoTextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scheduleList) { item ->
                        ScheduleCard(
                            item = item,
                            viewModel = viewModel,
                            onToggle = { viewModel.toggleScheduleItemCompletion(item) },
                            onDelete = { viewModel.deleteScheduleItem(item) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddScheduleItemDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, date, time, category, priority, syncGoogle, syncOutlook ->
                    viewModel.addScheduleItem(title, desc, date, time, category, priority, syncGoogle, syncOutlook)
                    showAddDialog = false
                }
            )
        }
    }
}


// ==================== SUB-TAB 3: UNIFIED WALLET LEDGER ====================

@Composable
fun WalletTab(
    viewModel: AssistantViewModel,
    financeList: List<FinanceItem>,
    balance: Double,
    incomeSum: Double,
    expenseSum: Double
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showUpiDialog by remember { mutableStateOf(false) }
    val budgets by viewModel.budgets.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unified Wallet",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoTextDark,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Balance & Budget Tracker",
                        fontSize = 13.sp,
                        color = BentoTextGray
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showUpiDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoNeutralCard,
                            contentColor = BentoAccentPurple
                        ),
                        border = BorderStroke(1.dp, BentoBorder),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("pay_with_upi_btn")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Pay with UPI", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay with UPI", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("open_add_finance_dialog_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add cash records", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Row")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bento styled financial scoreboard
            ScoreboardHero(
                balance = balance,
                income = incomeSum,
                expense = expenseSum
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Catgory Budgets Dashboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budgets",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextDark
                )
                TextButton(
                    onClick = { showBudgetDialog = true },
                    modifier = Modifier.testTag("open_set_budget_dialog_btn")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Set budget", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Define Budget", fontSize = 12.sp, color = BentoAccentPurple)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (budgets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No limits set. Click 'Define Budget' to regulate your spend!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoTextGray
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    budgets.forEach { b ->
                        val spent = financeList.filter { it.isExpense && it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
                        val progress = if (b.limitAmount > 0) (spent / b.limitAmount).toFloat() else 0f
                        
                        Card(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("budget_card_${b.category}"),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    progress >= 1f -> BentoRedIndicator.copy(alpha = 0.15f)
                                    progress >= 0.8f -> Color(0xFFE65100).copy(alpha = 0.15f)
                                    else -> BentoNeutralCard
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    progress >= 1f -> BentoRedIndicator
                                    progress >= 0.8f -> Color(0xFFE65100)
                                    else -> BentoBorder
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = b.category,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoTextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteBudget(b) },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .testTag("delete_budget_btn_${b.category}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Limit",
                                            tint = BentoTextGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "${CurrencyUtils.formatInr(spent)} / ${CurrencyUtils.formatInr(b.limitAmount)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (progress >= 1f) BentoRedIndicator else BentoTextGray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50))
                                        .testTag("budget_progress_${b.category}"),
                                    color = when {
                                        progress >= 1f -> BentoRedIndicator
                                        progress >= 0.8f -> Color(0xFFFFAB00)
                                        else -> BentoAccentPurple
                                    },
                                    trackColor = BentoBorder
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (progress >= 1f) {
                                    Text(
                                        text = "Limit exceeded!",
                                        fontSize = 9.sp,
                                        color = BentoRedIndicator,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    val left = b.limitAmount - spent
                                    Text(
                                        text = "${CurrencyUtils.formatInr(left)} left",
                                        fontSize = 9.sp,
                                        color = BentoTextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Transaction History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (financeList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = BentoPurpleText.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No financial records on file.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextDark
                        )
                        Text(
                            "Register transactions by asking AI or clicking 'Add Row'.",
                            fontSize = 12.sp,
                            color = BentoTextGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(financeList) { item ->
                        FinanceRow(
                            item = item,
                            onDelete = { viewModel.deleteFinanceItem(item) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddFinanceItemDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, amt, isExp, category, dStr ->
                    viewModel.addFinanceItem(title, amt, isExp, category, dStr)
                    showAddDialog = false
                }
            )
        }

        if (showBudgetDialog) {
            SetBudgetDialog(
                onDismiss = { showBudgetDialog = false },
                onConfirm = { cat, amt ->
                    viewModel.setBudget(cat, amt)
                    showBudgetDialog = false
                }
            )
        }

        if (showUpiDialog) {
            val context = LocalContext.current
            PayUpiDialog(
                onDismiss = { showUpiDialog = false },
                onConfirm = { upiId, amt, payeeName, note ->
                    val intentUri = Uri.Builder()
                        .scheme("upi")
                        .authority("pay")
                        .appendQueryParameter("pa", upiId)
                        .appendQueryParameter("pn", payeeName)
                        .appendQueryParameter("tn", note)
                        .appendQueryParameter("am", amt)
                        .appendQueryParameter("cu", "INR")
                        .build()

                    val intent = Intent(Intent.ACTION_VIEW, intentUri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No UPI app installed on this device", Toast.LENGTH_LONG).show()
                    }
                    showUpiDialog = false
                }
            )
        }
    }
}


// ==================== COMPOSE WIDGET DESIGN COMPONENTS SUPPORT ====================

@Composable
fun SmartBriefingDialog(
    state: BriefingState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    val isDark = true
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BentoAccentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Briefing",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else BentoTextDark,
                        fontSize = 18.sp
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dialog",
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else BentoTextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state) {
                    is BriefingState.Idle -> {
                        Text(
                            "Updating insights and agenda analysis...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else BentoTextGray
                        )
                    }
                    is BriefingState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = BentoAccentPurple
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Analyzing database context...",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else BentoTextGray
                            )
                        }
                    }
                    is BriefingState.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = state.content,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                color = if (isDark) Color(0xFFE5E2E9) else BentoTextDark,
                                modifier = Modifier.testTag("briefing_text_content")
                            )
                        }
                    }
                    is BriefingState.Error -> {
                        Column {
                            Text(
                                text = "Executive Briefing: Currently offline. Keep track of your wallet transactions and schedule items carefully today.",
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFFE5E2E9) else BentoTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Refresh Button
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("brief_dialog_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh briefing",
                        modifier = Modifier.size(16.dp),
                        tint = BentoAccentPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh", color = BentoAccentPurple)
                }

                if (state is BriefingState.Success) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("storiesofashu@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Personal Assistant: Daily Report")
                                putExtra(Intent.EXTRA_TEXT, state.content)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client found", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("share_briefing_report_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Daily Report", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        },
        containerColor = if (isDark) Color(0xFF221E26) else BentoNeutralCard,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ThinkuuAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7C3AED), // Premium Violet
                        Color(0xFFC084FC)  // Light Orchid Purple
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val cx = w / 2f
            
            // Draw minimalist cryptobot orbital rings/geometric lines
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = w * 0.42f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = w * 0.24f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
            // Inner core glowing point
            drawCircle(
                color = Color.White,
                radius = w * 0.12f
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    val isDark = true
    
    val bubbleColor = if (isUser) {
        BentoAccentPurple
    } else {
        if (isDark) Color(0xFF29252E) else Color(0xFFF1EFF5)
    }
    
    val textColor = if (isUser) {
        Color.White
    } else {
        if (isDark) Color(0xFFE0DCE4) else BentoTextDark
    }

    val bubbleBorderColor = if (isUser) {
        Color.Transparent
    } else {
        if (isDark) Color(0xFF38343D) else Color(0xFFE2DFE7)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Generous padding/margins between messages
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Elegant mini avatar next to Thinkuu's responses
            ThinkuuAvatar(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = bubbleBorderColor,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 21.sp, // Readable line spacing for premium view
                letterSpacing = 0.2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    viewModel: AssistantViewModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryIcon = when (item.category.lowercase()) {
        "work" -> Icons.Default.Work
        "personal" -> Icons.Default.Person
        "financial" -> Icons.Default.AccountBalance
        "health" -> Icons.Default.Favorite
        "chore" -> Icons.Default.Home
        else -> Icons.Default.CalendarToday
    }

    val priorityColor = when (item.priority.lowercase()) {
        "high" -> BentoRedIndicator
        "medium" -> Color(0xFFF59E0B)
        else -> BentoGreenText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("schedule_item_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                BentoNeutralCard.copy(alpha = 0.5f)
            } else {
                Color.White
            }
        ),
        border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bento left vertical priority line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task Category Icon Frame
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BentoNeutralCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = item.category,
                    tint = BentoAccentPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isCompleted) BentoTextDark.copy(alpha = 0.5f) else BentoTextDark
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = BentoTextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = BentoAccentPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.dateStr} • ${item.timeStr}",
                        fontSize = 11.sp,
                        color = BentoAccentPurple,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smart Reminder Departure Advisory Banner
                val smartReminder = viewModel.calculateSmartReminder(item)
                if (smartReminder.recommendedTime.isNotBlank() && !item.isCompleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BentoRedCard.copy(alpha = 0.6f))
                            .border(BorderStroke(1.dp, BentoRedIndicator.copy(alpha = 0.15f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TimeToLeave,
                                contentDescription = "Recommended Departure Alert Icon",
                                tint = BentoRedIndicator,
                                modifier = Modifier.size(14.dp)
                            )
                            Column {
                                Text(
                                    text = "Smart Departure Prompt",
                                    fontSize = 10.sp,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Leave by ${smartReminder.recommendedTime} • ${smartReminder.reason}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7F1D1D),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .testTag("schedule_item_checkbox_${item.id}")
                    .size(48.dp)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("schedule_item_delete_${item.id}"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = BentoRedIndicator)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete event",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ScoreboardHero(balance: Double, income: Double, expense: Double) {
    val balanceColor = if (balance >= 0) BentoGreenText else BentoRedIndicator

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .testTag("scoreboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = BentoNeutralCard
        ),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NET CASH FLOW BALANCE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextGray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (balance < 0) "-${CurrencyUtils.formatInr(Math.abs(balance))}" else CurrencyUtils.formatInr(balance),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = balanceColor,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income box
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = BentoGreenText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Income", fontSize = 11.sp, color = BentoTextGray, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = CurrencyUtils.formatInr(income),
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoGreenText,
                        fontSize = 15.sp
                    )
                }

                // Split divider bar
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(BentoBorder)
                )

                // Expense box
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = BentoRedIndicator,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expenses", fontSize = 11.sp, color = BentoTextGray, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = CurrencyUtils.formatInr(expense),
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoRedIndicator,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FinanceRow(item: FinanceItem, onDelete: () -> Unit) {
    val categoryIcon = when (item.category.lowercase()) {
        "salary", "bonus", "investment" -> Icons.Default.TrendingUp
        "food" -> Icons.Default.Restaurant
        "rent", "utilities" -> Icons.Default.HomeWork
        "shopping" -> Icons.Default.ShoppingBag
        "transport" -> Icons.Default.DirectionsCar
        else -> Icons.Default.Payment
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("finance_item_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
        border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (item.isExpense) BentoRedCard else BentoGreenCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = if (item.isExpense) BentoRedText else BentoGreenText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = BentoTextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoAccentPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.dateStr,
                        fontSize = 11.sp,
                        color = BentoTextGray
                    )
                }
            }

            Text(
                text = "${if (item.isExpense) "-" else "+"}${CurrencyUtils.formatInr(item.amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (item.isExpense) BentoRedIndicator else BentoGreenText,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("finance_item_delete_${item.id}"),
                colors = IconButtonDefaults.iconButtonColors(contentColor = BentoRedIndicator)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete transaction record",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddScheduleItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, Boolean, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var timeStr by remember { mutableStateOf("10:00 AM") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("Medium") }
    
    var syncGoogle by remember { mutableStateOf(false) }
    var syncOutlook by remember { mutableStateOf(false) }

    val categories = listOf("Work", "Personal", "Financial", "Chore", "Health")
    val priorities = listOf("High", "Medium", "Low")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Agenda Event",
                fontWeight = FontWeight.ExtraBold,
                color = BentoTextDark
            )
        },
        text = {
            val isDark = true
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) Color.White else BentoTextDark,
                unfocusedTextColor = if (isDark) Color.White else BentoTextDark,
                focusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                unfocusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                focusedLabelColor = BentoAccentPurple,
                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.6f) else BentoTextGray,
                focusedBorderColor = BentoAccentPurple,
                unfocusedBorderColor = BentoBorder,
                focusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray,
                unfocusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_schedule_input_title"),
                    singleLine = true,
                    colors = tfColors
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_schedule_input_desc"),
                    singleLine = true,
                    colors = tfColors
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_schedule_input_date"),
                        singleLine = true,
                        colors = tfColors
                    )
                    OutlinedTextField(
                        value = timeStr,
                        onValueChange = { timeStr = it },
                        label = { Text("Time") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_schedule_input_time"),
                        singleLine = true,
                        colors = tfColors
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        BentoFilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = cat
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { prio ->
                        BentoFilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = prio
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Cloud Export", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { syncGoogle = !syncGoogle },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = syncGoogle,
                            onCheckedChange = { syncGoogle = it },
                            colors = CheckboxDefaults.colors(checkedColor = BentoAccentPurple),
                            modifier = Modifier.testTag("checkbox_sync_google")
                        )
                        Text("Export to Google Calendar", fontSize = 11.sp, color = BentoTextDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { syncOutlook = !syncOutlook },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = syncOutlook,
                            onCheckedChange = { syncOutlook = it },
                            colors = CheckboxDefaults.colors(checkedColor = BentoAccentPurple),
                            modifier = Modifier.testTag("checkbox_sync_outlook")
                        )
                        Text("Export to Outlook Calendar", fontSize = 11.sp, color = BentoTextDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, desc, dateStr, timeStr, category, priority, syncGoogle, syncOutlook)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("comfirm_add_schedule_btn")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_add_schedule_btn")
            ) {
                Text("Cancel", color = BentoTextDark)
            }
        }
    )
}

@Composable
fun AddFinanceItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Boolean, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var category by remember { mutableStateOf("Food") }
    var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }

    val categories = listOf("Salary", "Groceries", "Entertainment", "Utilities", "Food", "Rent", "Shopping", "Transport", "Investment", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Cash Record",
                fontWeight = FontWeight.ExtraBold,
                color = BentoTextDark
            )
        },
        text = {
            val isDark = true
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) Color.White else BentoTextDark,
                unfocusedTextColor = if (isDark) Color.White else BentoTextDark,
                focusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                unfocusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                focusedLabelColor = BentoAccentPurple,
                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.6f) else BentoTextGray,
                focusedBorderColor = BentoAccentPurple,
                unfocusedBorderColor = BentoBorder,
                focusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray,
                unfocusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isExpense = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_select_expense"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) BentoRedIndicator else BentoNeutralCard,
                            contentColor = if (isExpense) Color.White else BentoTextGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Expense")
                    }
                    Button(
                        onClick = { isExpense = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_select_income"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) BentoGreenText else BentoNeutralCard,
                            contentColor = if (!isExpense) Color.White else BentoTextGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Income")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title or Payee") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_finance_input_title"),
                    singleLine = true,
                    colors = tfColors
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_finance_input_amount"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tfColors
                )

                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Date") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_finance_input_date"),
                    singleLine = true,
                    colors = tfColors
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Categories", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        BentoFilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = cat
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0.0) {
                        onAdd(title, amount, isExpense, category, dateStr)
                    }
                },
                enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("comfirm_add_finance_btn")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_add_finance_btn")
            ) {
                Text("Cancel", color = BentoTextDark)
            }
        }
    )
}

@Composable
fun BentoFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) BentoAccentPurple else BentoNeutralCard)
            .border(
                1.dp,
                if (selected) BentoAccentPurple else BentoBorder,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else BentoTextGray
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }
    val categories = listOf("Groceries", "Entertainment", "Utilities", "Food", "Rent", "Shopping", "Transport", "Investment", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Define Monthly Budget",
                fontWeight = FontWeight.ExtraBold,
                color = BentoTextDark
            )
        },
        text = {
            val isDark = true
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) Color.White else BentoTextDark,
                unfocusedTextColor = if (isDark) Color.White else BentoTextDark,
                focusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                unfocusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                focusedLabelColor = BentoAccentPurple,
                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.6f) else BentoTextGray,
                focusedBorderColor = BentoAccentPurple,
                unfocusedBorderColor = BentoBorder,
                focusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray,
                unfocusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Budget Limit (₹)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input_amount"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tfColors
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("Select Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextDark)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        BentoFilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = cat,
                            modifier = Modifier.testTag("budget_chip_$cat")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onConfirm(category, amount)
                    }
                },
                enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("confirm_set_budget_btn")
            ) {
                Text("Set Limit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_set_budget_btn")
            ) {
                Text("Cancel", color = BentoTextDark)
            }
        }
    )
}

@Composable
fun ManProfileIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE11D48), // Vibrant crimson red spotlight center
                        Color(0xFF991B1B), // Warm deep burgundy
                        Color(0xFF450A0A)  // Dark crimson edge
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val cx = width / 2f
            val cy = height / 2f
            val radius = width / 2f

            // 1. Draw shoulders / White Shirt
            val shirtPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - radius * 0.7f, height)
                lineTo(cx + radius * 0.7f, height)
                quadraticTo(cx + radius * 0.6f, cy + radius * 0.5f, cx + radius * 0.3f, cy + radius * 0.45f)
                lineTo(cx + radius * 0.2f, cy + radius * 0.75f) // collar line V
                lineTo(cx - radius * 0.2f, cy + radius * 0.75f) // collar line V
                lineTo(cx - radius * 0.3f, cy + radius * 0.45f)
                quadraticTo(cx - radius * 0.6f, cy + radius * 0.5f, cx - radius * 0.7f, height)
            }
            drawPath(shirtPath, Color.White)
            
            // Draw V-neck collar shadow/detail
            val collarDetail = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - radius * 0.25f, cy + radius * 0.45f)
                lineTo(cx, cy + radius * 0.75f)
                lineTo(cx + radius * 0.25f, cy + radius * 0.45f)
            }
            drawPath(collarDetail, Color(0xFFF3F4F6), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))

            // 2. Draw Neck
            val neckPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(cx - radius * 0.18f, cy + radius * 0.15f)
                lineTo(cx + radius * 0.18f, cy + radius * 0.15f)
                lineTo(cx + radius * 0.15f, cy + radius * 0.5f)
                lineTo(cx - radius * 0.15f, cy + radius * 0.5f)
                close()
            }
            drawPath(neckPath, Color(0xFFE5A687)) // Skin tone matching the photo's warm lighting

            // 3. Face shape
            val faceX = cx - radius * 0.05f
            val faceY = cy - radius * 0.05f
            val faceRadius = radius * 0.38f
            drawCircle(
                color = Color(0xFFF5B697), // Warm golden skin tone
                radius = faceRadius,
                center = androidx.compose.ui.geometry.Offset(faceX, faceY)
            )

            // 4. Ears
            drawCircle(
                color = Color(0xFFE5A687),
                radius = radius * 0.09f,
                center = androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.95f, faceY + faceRadius * 0.1f)
            )

            // 5. Curly Neat Hair
            val hairColor = Color(0xFF27130B) // Dark brown/black
            val hairPoints = listOf(
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.4f, faceY - faceRadius * 0.9f) to radius * 0.18f,
                androidx.compose.ui.geometry.Offset(faceX, faceY - faceRadius * 1.05f) to radius * 0.19f,
                androidx.compose.ui.geometry.Offset(faceX + faceRadius * 0.4f, faceY - faceRadius * 0.95f) to radius * 0.18f,
                androidx.compose.ui.geometry.Offset(faceX + faceRadius * 0.7f, faceY - faceRadius * 0.65f) to radius * 0.16f,
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.8f, faceY - faceRadius * 0.6f) to radius * 0.16f,
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.7f, faceY - faceRadius * 0.2f) to radius * 0.14f,
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 1.0f, faceY - faceRadius * 0.3f) to radius * 0.12f,
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.2f, faceY - faceRadius * 1.2f) to radius * 0.17f,
                androidx.compose.ui.geometry.Offset(faceX + faceRadius * 0.2f, faceY - faceRadius * 1.2f) to radius * 0.17f,
                androidx.compose.ui.geometry.Offset(faceX + faceRadius * 0.6f, faceY - faceRadius * 1.1f) to radius * 0.16f,
                androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.6f, faceY - faceRadius * 1.0f) to radius * 0.17f
            )
            hairPoints.forEach { (pos, r) ->
                drawCircle(color = hairColor, radius = r, center = pos)
            }

            // 6. Mustache
            val mustachePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(faceX - faceRadius * 0.4f, faceY + faceRadius * 0.42f)
                quadraticTo(faceX - faceRadius * 0.1f, faceY + faceRadius * 0.35f, faceX + faceRadius * 0.25f, faceY + faceRadius * 0.42f)
                quadraticTo(faceX + faceRadius * 0.4f, faceY + faceRadius * 0.47f, faceX + faceRadius * 0.45f, faceY + faceRadius * 0.38f)
                quadraticTo(faceX + faceRadius * 0.2f, faceY + faceRadius * 0.28f, faceX, faceY + faceRadius * 0.32f)
                quadraticTo(faceX - faceRadius * 0.18f, faceY + faceRadius * 0.28f, faceX - faceRadius * 0.48f, faceY + faceRadius * 0.35f)
                close()
            }
            drawPath(mustachePath, hairColor)

            // Smile line under mustache
            val smilePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(faceX - faceRadius * 0.18f, faceY + faceRadius * 0.60f)
                quadraticTo(faceX, faceY + faceRadius * 0.68f, faceX + faceRadius * 0.18f, faceY + faceRadius * 0.60f)
            }
            drawPath(smilePath, Color(0xFF9E5C41), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f))

            // 7. Eyes
            drawCircle(
                color = Color(0xFF1F0D05),
                radius = 3.5f,
                center = androidx.compose.ui.geometry.Offset(faceX - faceRadius * 0.15f, faceY - faceRadius * 0.05f)
            )
            drawCircle(
                color = Color(0xFF1F0D05),
                radius = 3.5f,
                center = androidx.compose.ui.geometry.Offset(faceX + faceRadius * 0.3f, faceY - faceRadius * 0.05f)
            )

            // Eyebrows
            val leftEyebrow = androidx.compose.ui.graphics.Path().apply {
                moveTo(faceX - faceRadius * 0.35f, faceY - faceRadius * 0.22f)
                quadraticTo(faceX - faceRadius * 0.15f, faceY - faceRadius * 0.28f, faceX - faceRadius * 0.02f, faceY - faceRadius * 0.18f)
            }
            drawPath(leftEyebrow, hairColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

            val rightEyebrow = androidx.compose.ui.graphics.Path().apply {
                moveTo(faceX + faceRadius * 0.12f, faceY - faceRadius * 0.18f)
                quadraticTo(faceX + faceRadius * 0.3f, faceY - faceRadius * 0.28f, faceX + faceRadius * 0.45f, faceY - faceRadius * 0.22f)
            }
            drawPath(rightEyebrow, hairColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        }
    }
}

@Composable
fun PayUpiDialog(
    onDismiss: () -> Unit,
    onConfirm: (upiId: String, amount: String, payeeName: String, note: String) -> Unit
) {
    var upiId by remember { mutableStateOf("storiesofashu@okaxis") }
    var amount by remember { mutableStateOf("1.00") }
    var payeeName by remember { mutableStateOf("Ashu") }
    var note by remember { mutableStateOf("Wallet Payment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pay via UPI",
                fontWeight = FontWeight.ExtraBold,
                color = BentoTextDark
            )
        },
        text = {
            val isDark = true
            val tfColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) Color.White else BentoTextDark,
                unfocusedTextColor = if (isDark) Color.White else BentoTextDark,
                focusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                unfocusedContainerColor = if (isDark) Color(0xFF2D2A30) else Color.White,
                focusedLabelColor = BentoAccentPurple,
                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.6f) else BentoTextGray,
                focusedBorderColor = BentoAccentPurple,
                unfocusedBorderColor = BentoBorder,
                focusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray,
                unfocusedPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.5f) else BentoTextGray
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("UPI ID (VPA)") },
                    placeholder = { Text("e.g. storiesofashu@okaxis") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_input_id"),
                    singleLine = true,
                    colors = tfColors
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 1.00") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_input_amount"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = tfColors
                )

                OutlinedTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = { Text("Payee Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_input_name"),
                    singleLine = true,
                    colors = tfColors
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Transaction Note") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_input_note"),
                    singleLine = true,
                    colors = tfColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (upiId.isNotBlank() && amount.isNotBlank()) {
                        onConfirm(upiId, amount, payeeName, note)
                    }
                },
                enabled = upiId.isNotBlank() && amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("confirm_upi_pay_btn")
            ) {
                Text("Pay Now")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_upi_pay_btn")
            ) {
                Text("Cancel", color = BentoTextDark)
            }
        }
    )
}


