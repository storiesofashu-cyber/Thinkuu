package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- Auth screen (Login & Sign up) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseLoginSignupScreen(
    viewModel: AssistantViewModel,
    onAuthSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Hero
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BentoAccentPurple.copy(alpha = 0.15f))
                    .border(1.dp, BentoAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Thinkuu Icon",
                    tint = BentoAccentPurple,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Thinkuu Messenger",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Text(
                text = if (isSignUp) "Create your personal secured profile" else "Login to sync schedules & real-time chat",
                fontSize = 12.sp,
                color = BentoTextGray,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Sub-card panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BentoAccentPurple,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoAccentPurple,
                                unfocusedLabelColor = BentoTextGray
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BentoAccentPurple,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoAccentPurple,
                            unfocusedLabelColor = BentoTextGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BentoAccentPurple,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoAccentPurple,
                            unfocusedLabelColor = BentoTextGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = BentoRedIndicator,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp).testTag("auth_error_text")
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty() || (isSignUp && name.isEmpty())) {
                                errorMessage = "Please fill in all details."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            focusManager.clearFocus()
                            if (isSignUp) {
                                viewModel.firebaseSignup(name, email, password, {
                                    isLoading = false
                                    onAuthSuccess()
                                }, { err ->
                                    isLoading = false
                                    errorMessage = err
                                })
                            } else {
                                viewModel.firebaseLogin(email, password, {
                                    isLoading = false
                                    onAuthSuccess()
                                }, { err ->
                                    isLoading = false
                                    errorMessage = err
                                })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoAccentPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_btn"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BentoBg)
                        } else {
                            Text(
                                text = if (isSignUp) "Register Account" else "Sign In Securely",
                                fontWeight = FontWeight.Bold,
                                color = BentoBg,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "New to Thinkuu? Sign Up now",
                color = BentoAccentPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { 
                        isSignUp = !isSignUp 
                        errorMessage = null
                    }
                    .testTag("auth_toggle_mode")
            )

            // Dynamic Sandbox Environment Helper Badge
            if (!viewModel.firebaseService.isRealFirebaseEnabled()) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoPurpleBadge)
                        .border(1.dp, BentoAccentPurple.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = "Active Sandbox",
                                tint = BentoAccentPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI Sandbox Environment",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoAccentPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Type any test email & password to login immediately! Sandbox simulates active chatting bots for fully responsive design testing.",
                            fontSize = 10.sp,
                            color = BentoTextGray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}


// --- Main Chats List Screen ---
@Composable
fun MessagesTab(
    viewModel: AssistantViewModel,
    onOpenChat: (FirebaseUser) -> Unit
) {
    val currentUser by viewModel.fbCurrentUser.collectAsState()
    val recentChats by viewModel.fbRecentChats.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.firebaseLoadUsers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // Custom Dashboard-style Header with user details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Conversations",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Inbox • ${currentUser?.name ?: "User"}",
                    fontSize = 12.sp,
                    color = BentoTextGray
                )
            }
            
            // Logout button
            IconButton(
                onClick = { viewModel.firebaseLogout() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BentoNeutralCard)
                    .border(1.dp, BentoBorder, CircleShape)
                    .testTag("chat_logout_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = BentoRedIndicator
                )
            }
        }

        // Recent Chats listings
        if (recentChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "No chats",
                        tint = BentoTextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No conversation channels found.",
                        color = BentoTextGray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentChats) { chat ->
                    RecentChatRow(
                        chat = chat,
                        onClick = { onOpenChat(chat.otherUser) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentChatRow(
    chat: RecentChat,
    onClick: () -> Unit
) {
    val user = chat.otherUser
    val lastMsg = chat.lastMessage
    val initials = user.name.take(2).uppercase()
    
    val timeFormatted = remember(lastMsg?.timestamp) {
        if (lastMsg == null) "" else {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            sdf.format(Date(lastMsg.timestamp))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recent_chat_row_${user.uid}"),
        colors = CardDefaults.cardColors(containerColor = BentoNeutralCard),
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stack avatar with status dot
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BentoPurpleBadge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = BentoAccentPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                // Presence dot
                if (user.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(BentoBg)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BentoGreenText)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (timeFormatted.isNotEmpty()) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = BentoTextGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkmarks for sender's last msg
                    if (lastMsg != null && lastMsg.senderId == "user_me") {
                        val tickColor = if (lastMsg.status == "READ") BentoAccentPurple else BentoTextGray
                        val tickSymbol = if (lastMsg.status == "SENT") "✓" else "✓✓"
                        Text(
                            text = "$tickSymbol ",
                            color = tickColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    val textPreview = when {
                        user.isTypingTo == "user_me" -> "typing..."
                        lastMsg == null -> "Start a secure conversation..."
                        lastMsg.isReport -> "📊 Sent daily summary report"
                        else -> lastMsg.text
                    }
                    
                    val textColor = if (user.isTypingTo == "user_me") BentoGreenText else BentoTextGray
                    val textWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    
                    Text(
                        text = textPreview,
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = textWeight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unread badge
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(BentoAccentPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${chat.unreadCount}",
                                color = BentoBg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- 1-to-1 Realtime Chat screen ---
@Composable
fun ActiveChatScreen(
    viewModel: AssistantViewModel,
    otherUser: FirebaseUser,
    onBack: () -> Unit
) {
    val messages by viewModel.fbMessagesList.collectAsState()
    val usersList by viewModel.fbUsersList.collectAsState()
    
    // Find live user update for presence
    val liveUser = remember(usersList, otherUser) {
        usersList.find { it.uid == otherUser.uid } ?: otherUser
    }
    
    var currentMessageText by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    val currentContext = LocalContext.current

    LaunchedEffect(otherUser.uid) {
        viewModel.firebaseStartChat(otherUser.uid)
    }
    
    // Scroll to bottom when message list expands
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    DisposableEffect(otherUser.uid) {
        onDispose {
            viewModel.firebaseStopChat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        // App bar top header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoNeutralCard)
                .border(width = 1.dp, color = BentoBorder, shape = RoundedCornerShape(0.dp))
                .statusBarsPadding()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("chat_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            // Avatar
            val initials = liveUser.name.take(2).uppercase()
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BentoPurpleBadge),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initials,
                    color = BentoAccentPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = liveUser.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Presence label
                val presenceText = when {
                    liveUser.isTypingTo == "user_me" -> "typing..."
                    liveUser.isOnline -> "Online"
                    else -> "Offline"
                }
                Text(
                    text = presenceText,
                    fontSize = 10.sp,
                    color = if (presenceText == "typing...") BentoGreenText else BentoTextGray
                )
            }
        }

        // Messages layout list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == "user_me"
                ChatBubble(msg = msg, isMe = isMe)
            }
        }

        // WhatsApp style bottom Input Bar with attachment options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(BentoNeutralCard)
                    .border(1.dp, BentoBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Daily Report Button
                IconButton(
                    onClick = {
                        val reportText = viewModel.getDailySummaryReportText()
                        currentMessageText += reportText
                        Toast.makeText(currentContext, "DAILY REPORT template attached!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("chat_attach_report_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = "Attach Report",
                        tint = BentoAccentPurple
                    )
                }
                
                // Photo Icon simulation
                IconButton(
                    onClick = {
                        currentMessageText += "📸 [Attached Photo] "
                    },
                    modifier = Modifier.testTag("chat_camera_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = BentoTextGray
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // Message text input
                TextField(
                    value = currentMessageText,
                    onValueChange = {
                        currentMessageText = it
                        // Trigger dynamic simulated typing notifier to bot
                        if (it.isNotEmpty()) {
                            viewModel.firebaseUpdateTyping(otherUser.uid)
                        } else {
                            viewModel.firebaseUpdateTyping("")
                        }
                    },
                    placeholder = { Text("Type a message...", fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedPlaceholderColor = BentoTextGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field")
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button circle
            IconButton(
                onClick = {
                    if (currentMessageText.trim().isNotEmpty()) {
                        val trimmedText = currentMessageText.trim()
                        val isReport = trimmedText.contains("DAILY REPORT")
                        
                        viewModel.firebaseSendMessage(otherUser.uid, trimmedText, isReport)
                        currentMessageText = ""
                        viewModel.firebaseUpdateTyping("")
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BentoAccentPurple)
                    .testTag("chat_send_message_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = BentoBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun ChatBubble(
    msg: FirebaseMessage,
    isMe: Boolean
) {
    val formatter = remember(msg.timestamp) {
        SimpleDateFormat("h:mm a", Locale.US)
    }
    
    val timeStr = formatter.format(Date(msg.timestamp))

    // Asymmetric shapes to represent speech bubble tails
    val bubbleShape = if (isMe) {
        RoundedCornerShape(
            topStart = 16.dp, 
            topEnd = 16.dp, 
            bottomEnd = 0.dp, // Tail corner pointing down-right
            bottomStart = 16.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp, 
            topEnd = 16.dp, 
            bottomEnd = 16.dp, 
            bottomStart = 0.dp // Tail corner pointing down-left
        )
    }

    val bubbleBg = if (isMe) BentoPurpleBadge else BentoNeutralCard
    val bubbleBorderColor = if (isMe) BentoAccentPurple.copy(alpha = 0.3f) else BentoBorder

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = bubbleShape,
                color = bubbleBg,
                border = BorderStroke(1.dp, bubbleBorderColor),
                modifier = Modifier.testTag("chat_bubble_${msg.id}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = msg.text,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(3.dp))
                    
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            fontSize = 9.sp,
                            color = BentoTextGray
                        )
                        
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val tickColor = if (msg.status == "READ") BentoAccentPurple else BentoTextGray
                            val tickStr = if (msg.status == "SENT") "✓" else "✓✓"
                            Text(
                                text = tickStr,
                                color = tickColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
