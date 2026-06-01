package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class FirebaseUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val isTypingTo: String = "", // uid of person this user is typing to
    val avatarUrl: String = ""
)

data class FirebaseMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // "SENT", "DELIVERED", "READ"
    val isReport: Boolean = false
)

data class RecentChat(
    val otherUser: FirebaseUser,
    val lastMessage: FirebaseMessage?,
    val unreadCount: Int = 0
)

class FirebaseService(private val application: Application) {
    companion object {
        val mockSessions = mutableListOf<Map<String, Any>>(
            mapOf(
                "id" to "s_mock_1",
                "userId" to "user_gaurav",
                "userName" to "Gaurav (AI Designer)",
                "email" to "gaurav@think.com",
                "session_start" to System.currentTimeMillis() - 10800000,
                "session_end" to System.currentTimeMillis() - 9600000
            ),
            mapOf(
                "id" to "s_mock_2",
                "userId" to "user_arjun",
                "userName" to "Arjun (Team Lead)",
                "email" to "arjun@think.com",
                "session_start" to System.currentTimeMillis() - 7200000,
                "session_end" to System.currentTimeMillis() - 5400000
            ),
            mapOf(
                "id" to "s_mock_3",
                "userId" to "user_priya",
                "userName" to "Priya (Finance Planner)",
                "email" to "priya@think.com",
                "session_start" to System.currentTimeMillis() - 3600000,
                "session_end" to 0L
            )
        )
        val mockGlobalReports = mutableListOf<Map<String, Any>>(
            mapOf(
                "id" to "r_mock_1",
                "userId" to "user_arjun",
                "userName" to "Arjun (Team Lead)",
                "reportText" to "*DAILY REPORT - 2026-05-27*\n\n*Agenda Items Today:*\n• [High] ✓ [Done] Team Sync on Bento Aesthetics (10:00 AM)\n• [Medium] ☐ [Pending] Code Review with Gaurav (02:00 PM)\n\n*Total Expenses Today:*\n• ₹1,550.00\n  - Catered Lunch: ₹1,200.00\n  - Taxi Transit: ₹350.00",
                "timestamp" to System.currentTimeMillis() - 7200000
            ),
            mapOf(
                "id" to "r_mock_2",
                "userId" to "user_priya",
                "userName" to "Priya (Finance Planner)",
                "reportText" to "*DAILY REPORT - 2026-05-27*\n\n*Agenda Items Today:*\n• [High] ✓ [Done] Personal Budget Mapping (09:00 AM)\n\n*Total Expenses Today:*\n• ₹4,800.00\n  - Office Materials: ₹4,800.00",
                "timestamp" to System.currentTimeMillis() - 3600000
            )
        )
    }

    private var isUsingRealFirebase = false
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var currentSessionId: String? = null
    private var isSigningInAnonymously = false

    private fun signInAnonymouslyInBackground() {
        if (!isUsingRealFirebase || isSigningInAnonymously) return
        isSigningInAnonymously = true
        Log.d("FirebaseService", "Attempting anonymous sign-in in the background...")
        auth?.signInAnonymously()
            ?.addOnCompleteListener {
                isSigningInAnonymously = false
            }
            ?.addOnSuccessListener { authResult ->
                val fbUser = authResult.user
                if (fbUser != null) {
                    Log.d("FirebaseService", "Anonymous Auth succeeded: ${fbUser.uid}")
                    fetchRealUserProfile(fbUser.uid)
                }
            }
            ?.addOnFailureListener {
                Log.e("FirebaseService", "Anonymous Auth failed: ${it.message}. Falling back to Guest environment.")
                // Set fallback guest profile so user is never stuck on a blank loading screen
                val fallbackProfile = FirebaseUser("guest_fallback", "Ashu", "anonymous@thinkuu.com", true, "", "")
                _currentUser.value = fallbackProfile
                logSessionStart()
            }
    }

    fun uploadGlobalReport(userName: String, userId: String, reportText: String) {
        val reportId = UUID.randomUUID().toString()
        val reportData = hashMapOf(
            "id" to reportId,
            "userId" to userId,
            "userName" to userName,
            "reportText" to reportText,
            "timestamp" to System.currentTimeMillis()
        )
        if (isUsingRealFirebase) {
            firestore?.collection("global_reports")?.document(reportId)?.set(reportData)
                ?.addOnSuccessListener {
                    Log.d("FirebaseService", "Global report auto-synced securely: $reportId")
                }
                ?.addOnFailureListener {
                    Log.e("FirebaseService", "Error syncing global report: ${it.message}")
                }
        } else {
            mockGlobalReports.add(0, reportData)
            Log.d("FirebaseService", "Mock Global Report logged: $reportId")
        }
    }

    fun logSessionStart() {
        val myUser = currentUser.value ?: return
        if (currentSessionId != null) return // Already has an active session

        val sId = UUID.randomUUID().toString()
        currentSessionId = sId
        val startTime = System.currentTimeMillis()

        val sessionData = hashMapOf(
            "id" to sId,
            "userId" to myUser.uid,
            "userName" to myUser.name,
            "email" to myUser.email,
            "session_start" to startTime,
            "session_end" to 0L
        )

        if (isUsingRealFirebase) {
            firestore?.collection("user_sessions")?.document(sId)?.set(sessionData)
                ?.addOnSuccessListener {
                    Log.d("FirebaseService", "Session started securely: $sId")
                }
                ?.addOnFailureListener {
                    Log.e("FirebaseService", "Failed to start session: ${it.message}")
                }
        } else {
            mockSessions.add(0, sessionData)
            Log.d("FirebaseService", "Mock Session started: $sId")
        }
    }

    fun logSessionEnd() {
        val sId = currentSessionId ?: return
        val endTime = System.currentTimeMillis()

        if (isUsingRealFirebase) {
            firestore?.collection("user_sessions")?.document(sId)?.update("session_end", endTime)
                ?.addOnSuccessListener {
                    Log.d("FirebaseService", "Session ended securely: $sId")
                }
        } else {
            val idx = mockSessions.indexOfFirst { it["id"] == sId }
            if (idx != -1) {
                val oldMap = mockSessions[idx].toMutableMap()
                oldMap["session_end"] = endTime
                mockSessions[idx] = oldMap
            }
            Log.d("FirebaseService", "Mock Session ended: $sId")
        }
        currentSessionId = null
    }

    // Fallback simulation state
    private val mockUsers = mutableListOf<FirebaseUser>()
    private val mockMessages = mutableListOf<FirebaseMessage>()
    private var currentMockUser: FirebaseUser? = null

    // Reactive states
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _usersList = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val usersList: StateFlow<List<FirebaseUser>> = _usersList.asStateFlow()

    private val _messagesList = MutableStateFlow<List<FirebaseMessage>>(emptyList())
    val messagesList: StateFlow<List<FirebaseMessage>> = _messagesList.asStateFlow()

    private val _recentChats = MutableStateFlow<List<RecentChat>>(emptyList())
    val recentChats: StateFlow<List<RecentChat>> = _recentChats.asStateFlow()

    // Firestore listener registrations
    private var usersListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        try {
            val context = application.applicationContext
            val apiKey = "AIzaSyDummyKey-ThinkuuApp"
            val isDummyKey = apiKey.contains("DummyKey")

            if (FirebaseApp.getApps(context).isEmpty() && !isDummyKey) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:548329487532:android:9d4fbe1b3a2cd7de")
                    .setApiKey(apiKey)
                    .setProjectId("thinkuu-app-firebase")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }

            if (!isDummyKey) {
                auth = FirebaseAuth.getInstance()
                firestore = FirebaseFirestore.getInstance()
                isUsingRealFirebase = true
                Log.d("FirebaseService", "Successfully initialized Real Firebase integration!")
            } else {
                isUsingRealFirebase = false
                Log.d("FirebaseService", "Running with robust Dynamic Local Fallback: Google Play Services or Firebase configurations are placeholder.")
            }
        } catch (e: Throwable) {
            Log.w("FirebaseService", "Running with robust Dynamic Local Fallback: ${e.message}")
            isUsingRealFirebase = false
        }

        if (!isUsingRealFirebase) {
            setupMockEnvironment()
        } else {
            // Monitor real auth state
            auth?.addAuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null) {
                    fetchRealUserProfile(fbUser.uid)
                } else {
                    _currentUser.value = null
                    stopRealListeners()
                    // Re-try/trigger background anonymous sign-in to keep app connected
                    signInAnonymouslyInBackground()
                }
            }
        }
    }

    private fun setupMockEnvironment() {
        // Prepopulate interesting users to chat with
        mockUsers.add(FirebaseUser("user_gaurav", "Gaurav (AI Designer)", "gaurav@think Active", false, "", ""))
        mockUsers.add(FirebaseUser("user_arjun", "Arjun (Team Lead)", "arjun@think Active", false, "", ""))
        mockUsers.add(FirebaseUser("user_priya", "Priya (Finance Planner)", "priya@think Active", false, "", ""))
        
        // Add some initial mock messages
        mockMessages.add(FirebaseMessage("m1", "user_gaurav", "user_me", "Hey mate! Have you reviewed the new dark mode aesthetics for Thinkuu?", System.currentTimeMillis() - 7200000, "READ"))
        mockMessages.add(FirebaseMessage("m2", "user_me", "user_gaurav", "Yes, did a full audit! Looks extremely beautiful and sleek.", System.currentTimeMillis() - 3600000, "READ"))
        mockMessages.add(FirebaseMessage("m3", "user_gaurav", "user_me", "Awesome! Let me know if you need help with custom visual components.", System.currentTimeMillis() - 1800000, "DELIVERED"))
        
        mockMessages.add(FirebaseMessage("m4", "user_arjun", "user_me", "Great progress on the INR formatter! Can you share todays expense report?", System.currentTimeMillis() - 600000, "DELIVERED"))
        mockMessages.add(FirebaseMessage("m5", "user_priya", "user_me", "Remember to set your monthly categories budget to stay within limits!", System.currentTimeMillis() - 1200000, "READ"))

        _usersList.value = mockUsers.toList()
        updateRecentChats()

        // Bypassing authorization completely in Sandbox mode: immediately log in guest user
        currentMockUser = FirebaseUser("user_me", "Ashu", "demo@thinkuu.com", true, "", "")
        _currentUser.value = currentMockUser
        updateUserPresence(true)
        logSessionStart()
    }

    fun isRealFirebaseEnabled(): Boolean = isUsingRealFirebase

    // --- Authentication ---
    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (isUsingRealFirebase) {
            auth?.signInWithEmailAndPassword(email, password)
                ?.addOnSuccessListener {
                    onSuccess()
                }
                ?.addOnFailureListener {
                    onFailure(it.localizedMessage ?: "Login failed")
                }
        } else {
            // Mock Login
            if (email.lowercase() == "demo@thinkuu.com" || mockUsers.any { it.email.lowercase() == email.lowercase() } || email.contains("@")) {
                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                currentMockUser = FirebaseUser("user_me", name, email, true, "", "")
                _currentUser.value = currentMockUser
                updateUserPresence(true)
                logSessionStart()
                onSuccess()
            } else {
                onFailure("Invalid email or password format for mock login.")
            }
        }
    }

    fun signup(name: String, email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (isUsingRealFirebase) {
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: ""
                    val profile = FirebaseUser(uid, name, email, true, "", "")
                    saveRealUserProfile(profile, onSuccess, onFailure)
                }
                ?.addOnFailureListener {
                    onFailure(it.localizedMessage ?: "Registration failed")
                }
        } else {
            // Mock Sign Up
            currentMockUser = FirebaseUser("user_me", name, email, true, "", "")
            _currentUser.value = currentMockUser
            updateUserPresence(true)
            logSessionStart()
            onSuccess()
        }
    }

    fun logout() {
        logSessionEnd()
        updateUserPresence(false)
        if (isUsingRealFirebase) {
            auth?.signOut()
        } else {
            // Mock logout: reset and immediately set up mock environment to stay logged in
            currentMockUser = null
            _currentUser.value = null
            setupMockEnvironment()
        }
    }

    // --- Profiles ---
    private fun saveRealUserProfile(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firestore?.collection("users")?.document(user.uid)?.set(user)
            ?.addOnSuccessListener {
                _currentUser.value = user
                logSessionStart()
                startRealUsersListener()
                onSuccess()
            }
            ?.addOnFailureListener {
                onFailure(it.localizedMessage ?: "Failed to save profile")
            }
    }

    private fun fetchRealUserProfile(uid: String) {
        firestore?.collection("users")?.document(uid)?.get()
            ?.addOnSuccessListener { snapshot ->
                var profile = snapshot.toObject(FirebaseUser::class.java)
                if (profile == null) {
                    // Profile does not exist yet (e.g. newly created anonymous user or missing record), establish one
                    val defaultProfile = FirebaseUser(uid, "Ashu", "anonymous@thinkuu.com", true, "", "")
                    firestore?.collection("users")?.document(uid)?.set(defaultProfile)
                        ?.addOnSuccessListener {
                            _currentUser.value = defaultProfile
                            updateUserPresence(true)
                            logSessionStart()
                            startRealUsersListener()
                        }
                        ?.addOnFailureListener {
                            // Offline or permissions temporary error, proceed locally
                            _currentUser.value = defaultProfile
                            logSessionStart()
                        }
                } else {
                    _currentUser.value = profile
                    updateUserPresence(true)
                    logSessionStart()
                    startRealUsersListener()
                }
            }
            ?.addOnFailureListener {
                // If fetching fails completely (e.g., offline/permission), set a fallback guest profile
                val fallbackProfile = FirebaseUser(uid, "Ashu", "anonymous@thinkuu.com", true, "", "")
                _currentUser.value = fallbackProfile
                logSessionStart()
            }
    }

    fun updateUserPresence(online: Boolean) {
        if (isUsingRealFirebase) {
            val uid = auth?.currentUser?.uid ?: return
            firestore?.collection("users")?.document(uid)?.update("online", online)
        } else {
            currentMockUser = currentMockUser?.copy(isOnline = online)
            _currentUser.value = currentMockUser
        }
    }

    fun updateTypingStatus(otherUserUid: String) {
        if (isUsingRealFirebase) {
            val uid = auth?.currentUser?.uid ?: return
            firestore?.collection("users")?.document(uid)?.update("isTypingTo", otherUserUid)
        } else {
            currentMockUser = currentMockUser?.copy(isTypingTo = otherUserUid)
            _currentUser.value = currentMockUser
        }
    }

    // --- Messages Sync ---
    fun loadUsersList() {
        if (!isUsingRealFirebase) {
            _usersList.value = mockUsers.toList()
            updateRecentChats()
        }
    }

    private fun startRealUsersListener() {
        usersListener?.remove()
        val uid = auth?.currentUser?.uid ?: return
        usersListener = firestore?.collection("users")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = mutableListOf<FirebaseUser>()
                snapshot?.documents?.forEach { doc ->
                    val user = doc.toObject(FirebaseUser::class.java)
                    if (user != null && user.uid != uid) {
                        list.add(user)
                    }
                }
                _usersList.value = list
                updateRecentChats()
            }
    }

    fun startMessagesListener(otherUid: String) {
        if (isUsingRealFirebase) {
            messagesListener?.remove()
            val myUid = auth?.currentUser?.uid ?: return

            // Query messages involving current and secondary user
            messagesListener = firestore?.collection("messages")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseService", "Error reading messages: ${error.message}")
                        return@addSnapshotListener
                    }
                    val list = mutableListOf<FirebaseMessage>()
                    snapshot?.documents?.forEach { doc ->
                        val msg = doc.toObject(FirebaseMessage::class.java)
                        if (msg != null && (
                            (msg.senderId == myUid && msg.receiverId == otherUid) || 
                            (msg.senderId == otherUid && msg.receiverId == myUid)
                        )) {
                            list.add(msg.copy(id = doc.id))
                            
                            // Mark unread incoming messages as READ
                            if (msg.receiverId == myUid && msg.senderId == otherUid && msg.status != "READ") {
                                firestore?.collection("messages")?.document(doc.id)?.update("status", "READ")
                            }
                        }
                    }
                    _messagesList.value = list
                    updateRecentChats()
                }
        } else {
            // Mock Listener Action
            val list = mockMessages.filter {
                (it.senderId == "user_me" && it.receiverId == otherUid) ||
                (it.senderId == otherUid && it.receiverId == "user_me")
            }.sortedBy { it.timestamp }

            // Mark income messages as READ
            mockMessages.forEachIndexed { i, msg ->
                if (msg.receiverId == "user_me" && msg.senderId == otherUid && msg.status != "READ") {
                    mockMessages[i] = msg.copy(status = "READ")
                }
            }

            _messagesList.value = list
            updateRecentChats()
        }
    }

    fun stopMessagesListener() {
        messagesListener?.remove()
        messagesListener = null
        _messagesList.value = emptyList()
    }

    private fun stopRealListeners() {
        usersListener?.remove()
        usersListener = null
        messagesListener?.remove()
        messagesListener = null
    }

    fun sendMessage(receiverId: String, text: String, isReport: Boolean = false) {
        val myUid = if (isUsingRealFirebase) auth?.currentUser?.uid ?: "" else "user_me"
        if (myUid.isEmpty()) return

        val msgId = UUID.randomUUID().toString()
        val comment = FirebaseMessage(
            id = msgId,
            senderId = myUid,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT",
            isReport = isReport
        )

        if (isUsingRealFirebase) {
            firestore?.collection("messages")?.document(msgId)?.set(comment)
                ?.addOnSuccessListener {
                    // Update recent state
                    updateRecentChats()
                }
        } else {
            // Mock message lifecycle with delay to represent sent -> delivered -> read ticks!
            mockMessages.add(comment)
            val updatedList = mockMessages.filter {
                (it.senderId == "user_me" && it.receiverId == receiverId) ||
                (it.senderId == receiverId && it.receiverId == "user_me")
            }.sortedBy { it.timestamp }
            _messagesList.value = updatedList
            updateRecentChats()

            // Step 1: Delivered Tick simulation (after 1000ms)
            mainHandler.postDelayed({
                val idx = mockMessages.indexOfFirst { it.id == msgId }
                if (idx != -1) {
                    mockMessages[idx] = mockMessages[idx].copy(status = "DELIVERED")
                    _messagesList.value = mockMessages.filter {
                        (it.senderId == "user_me" && it.receiverId == receiverId) ||
                        (it.senderId == receiverId && it.receiverId == "user_me")
                    }.sortedBy { it.timestamp }
                    updateRecentChats()
                }
            }, 1000)

            // Step 2: Read Tick simulation (after 2500ms)
            mainHandler.postDelayed({
                val idx = mockMessages.indexOfFirst { it.id == msgId }
                if (idx != -1) {
                    mockMessages[idx] = mockMessages[idx].copy(status = "READ")
                    _messagesList.value = mockMessages.filter {
                        (it.senderId == "user_me" && it.receiverId == receiverId) ||
                        (it.senderId == receiverId && it.receiverId == "user_me")
                    }.sortedBy { it.timestamp }
                    updateRecentChats()
                }
                
                // Trigger contact user typing and reply simulation!
                simulateBotResponse(receiverId)
            }, 2500)
        }
    }

    private fun simulateBotResponse(botId: String) {
        val bot = mockUsers.find { it.uid == botId } ?: return

        // Step 1: Set typing status to true
        mainHandler.postDelayed({
            val idx = mockUsers.indexOfFirst { it.uid == botId }
            if (idx != -1) {
                mockUsers[idx] = bot.copy(isOnline = true, isTypingTo = "user_me")
                _usersList.value = mockUsers.toList()
            }
        }, 800)

        // Step 2: Bot sends message
        mainHandler.postDelayed({
            // Set typing to default, set online to true
            val idx = mockUsers.indexOfFirst { it.uid == botId }
            if (idx != -1) {
                mockUsers[idx] = bot.copy(isOnline = true, isTypingTo = "")
                _usersList.value = mockUsers.toList()
            }

            val botReply = when (botId) {
                "user_gaurav" -> "Superb! Let me know if you want custom shapes or gradients added too. That Rupee glyph '₹' sits so clean with the typography."
                "user_arjun" -> "Thanks, let's keep an eye on these budget metrics during local tests. Clean reports!"
                "user_priya" -> "Wow, perfect. Looks like the expenses are categorized correctly. Outstanding!"
                else -> "Understood, nice update!"
            }

            val replyMsg = FirebaseMessage(
                id = UUID.randomUUID().toString(),
                senderId = botId,
                receiverId = "user_me",
                text = botReply,
                timestamp = System.currentTimeMillis(),
                status = "READ"
            )

            mockMessages.add(replyMsg)
            _messagesList.value = mockMessages.filter {
                (it.senderId == "user_me" && it.receiverId == botId) ||
                (it.senderId == botId && it.receiverId == "user_me")
            }.sortedBy { it.timestamp }
            updateRecentChats()
        }, 3200)
    }

    private fun updateRecentChats() {
        val myUid = if (isUsingRealFirebase) auth?.currentUser?.uid ?: "" else "user_me"
        if (myUid.isEmpty()) return

        val recentList = _usersList.value.map { otherUser ->
            val chatHistory = if (isUsingRealFirebase) {
                _messagesList.value.filter {
                    (it.senderId == myUid && it.receiverId == otherUser.uid) ||
                    (it.senderId == otherUser.uid && it.receiverId == myUid)
                }
            } else {
                mockMessages.filter {
                    (it.senderId == myUid && it.receiverId == otherUser.uid) ||
                    (it.senderId == otherUser.uid && it.receiverId == myUid)
                }
            }
            val lastMsg = chatHistory.maxByOrNull { it.timestamp }
            val unreadCount = chatHistory.count { it.receiverId == myUid && it.status != "READ" }

            RecentChat(otherUser, lastMsg, unreadCount)
        }.sortedByDescending { it.lastMessage?.timestamp ?: 0L }

        _recentChats.value = recentList
    }
}
