package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface BriefingState {
    object Idle : BriefingState
    object Loading : BriefingState
    data class Success(val content: String) : BriefingState
    data class Error(val message: String) : BriefingState
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AssistantRepository
    private val prefs = application.getSharedPreferences("assistant_prefs", android.content.Context.MODE_PRIVATE)

    // --- Google & Outlook Connection States ---
    private val _isGoogleConnected = MutableStateFlow(false)
    val isGoogleConnected: StateFlow<Boolean> = _isGoogleConnected.asStateFlow()

    private val _isOutlookConnected = MutableStateFlow(false)
    val isOutlookConnected: StateFlow<Boolean> = _isOutlookConnected.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // --- Firebase Auth & Chat System Integration ---
    val firebaseService = FirebaseService(application)

    val fbCurrentUser: StateFlow<FirebaseUser?> = firebaseService.currentUser
    val fbUsersList: StateFlow<List<FirebaseUser>> = firebaseService.usersList
    val fbMessagesList: StateFlow<List<FirebaseMessage>> = firebaseService.messagesList
    val fbRecentChats: StateFlow<List<RecentChat>> = firebaseService.recentChats

    fun firebaseLogin(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseService.login(email, password, onSuccess, onFailure)
    }

    fun firebaseSignup(name: String, email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseService.signup(name, email, password, onSuccess, onFailure)
    }

    fun firebaseLogout() {
        firebaseService.logout()
    }

    fun firebaseLoadUsers() {
        firebaseService.loadUsersList()
    }

    fun firebaseStartChat(otherUid: String) {
        firebaseService.startMessagesListener(otherUid)
    }

    fun firebaseStopChat() {
        firebaseService.stopMessagesListener()
    }

    fun firebaseSendMessage(receiverId: String, text: String, isReport: Boolean = false) {
        firebaseService.sendMessage(receiverId, text, isReport)
    }

    fun firebaseUpdateTyping(otherUid: String) {
        firebaseService.updateTypingStatus(otherUid)
    }

    fun getDailySummaryReportText(): String {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val agenda = scheduleItems.value.filter { it.dateStr == todayStr }
        val todayExpenses = financeItems.value
            .filter { it.isExpense && it.dateStr == todayStr }
        val totalExpensesToday = todayExpenses.sumOf { it.amount }

        val reportStr = buildString {
            append("*DAILY REPORT - $todayStr*\n\n")
            append("*Agenda Items Today:*\n")
            if (agenda.isEmpty()) {
                append("• No tasks scheduled for today.\n")
            } else {
                agenda.forEach { item ->
                    val statusSym = if (item.isCompleted) "✓ [Done]" else "☐ [Pending]"
                    append("• [${item.priority}] $statusSym ${item.title} (${item.timeStr})\n")
                }
            }
            append("\n*Total Expenses Today:*\n")
            append("• ${CurrencyUtils.formatInr(totalExpensesToday)}\n")
            if (todayExpenses.isNotEmpty()) {
                todayExpenses.forEach { item ->
                    append("  - ${item.title}: ${CurrencyUtils.formatInr(item.amount)}\n")
                }
            }
        }

        // Auto-Sync report copy to Firebase global_reports
        val user = fbCurrentUser.value
        val userName = user?.name ?: "Demo/Guest User"
        val userId = user?.uid ?: "guest_uid"
        firebaseService.uploadGlobalReport(userName, userId, reportStr)

        return reportStr
    }

    fun onAppStart() {
        firebaseService.logSessionStart()
    }

    fun onAppStop() {
        firebaseService.logSessionEnd()
    }

    fun toggleDarkMode() {
        // Force remains true for the global dark theme style
        _isDarkMode.value = true
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AssistantRepository(database.assistantDao())
        
        _isGoogleConnected.value = prefs.getBoolean("google_calendar_connected", false)
        _isOutlookConnected.value = prefs.getBoolean("outlook_calendar_connected", false)
        
        // Sync on launch if connected
        if (_isGoogleConnected.value || _isOutlookConnected.value) {
            syncCalendars()
        }
    }

    // --- State Observables ---
    val scheduleItems: StateFlow<List<ScheduleItem>> = repository.allScheduleItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val financeItems: StateFlow<List<FinanceItem>> = repository.allFinanceItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Financial Summary calculations ---
    val totalIncome: StateFlow<Double> = financeItems
        .map { items -> items.filter { !it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = financeItems
        .map { items -> items.filter { it.isExpense }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentBalance: StateFlow<Double> = financeItems
        .map { items ->
            val income = items.filter { !it.isExpense }.sumOf { it.amount }
            val expense = items.filter { it.isExpense }.sumOf { it.amount }
            income - expense
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- AI Chat States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                text = "Hello! I am Thinkuu, your AI Personal Assistant. You can chat with me, ask for financial summaries, or write natural phrases like 'remind me to pay electric bill ₹600 tomorrow' and I will help you parse them!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Camera Captured Photo State ---
    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    fun setCapturedPhoto(bitmap: Bitmap?) {
        _capturedPhoto.value = bitmap
    }

    // --- Daily AI Smart Briefing State ---
    private val _briefingState = MutableStateFlow<BriefingState>(BriefingState.Idle)
    val briefingState: StateFlow<BriefingState> = _briefingState.asStateFlow()

    init {
        // Generate an initial smart briefing when database opens or updates
        viewModelScope.launch {
            combine(scheduleItems, financeItems) { scheds, fins ->
                Pair(scheds, fins)
            }.collectLatest { (scheds, fins) ->
                if (_briefingState.value is BriefingState.Idle) {
                    generateDailyBriefing(scheds, fins)
                }
            }
        }
    }

    // --- Database Operations ---
    fun addScheduleItem(
        title: String,
        description: String,
        dateStr: String,
        timeStr: String,
        category: String,
        priority: String,
        syncGoogle: Boolean = false,
        syncOutlook: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalDescription = buildString {
                append(description)
                if (syncGoogle || _isGoogleConnected.value) {
                    append(" | [Synced to Google Calendar]")
                }
                if (syncOutlook || _isOutlookConnected.value) {
                    append(" | [Synced to Outlook Calendar]")
                }
            }

            repository.insertScheduleItem(
                ScheduleItem(
                    title = title,
                    description = finalDescription,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    category = category,
                    priority = priority,
                    isCompleted = false
                )
            )

            // Force simulated sync visual trigger and write to device calendar if permitted
            if (syncGoogle || syncOutlook || _isGoogleConnected.value || _isOutlookConnected.value) {
                syncEventToDeviceCalendar(title, finalDescription, dateStr, timeStr)
            }
        }
    }

    // --- Calendar System and Cloud Sync Logic ---
    fun connectGoogleCalendar() {
        _isGoogleConnected.value = true
        prefs.edit().putBoolean("google_calendar_connected", true).apply()
        syncCalendars()
    }

    fun disconnectGoogleCalendar() {
        _isGoogleConnected.value = false
        prefs.edit().putBoolean("google_calendar_connected", false).apply()
        viewModelScope.launch(Dispatchers.IO) {
            repository.allScheduleItems.firstOrNull()?.forEach { item ->
                if (item.description.contains("Synced from Google Calendar")) {
                    repository.deleteScheduleItem(item)
                }
            }
        }
    }

    fun connectOutlookCalendar() {
        _isOutlookConnected.value = true
        prefs.edit().putBoolean("outlook_calendar_connected", true).apply()
        syncCalendars()
    }

    fun disconnectOutlookCalendar() {
        _isOutlookConnected.value = false
        prefs.edit().putBoolean("outlook_calendar_connected", false).apply()
        viewModelScope.launch(Dispatchers.IO) {
            repository.allScheduleItems.firstOrNull()?.forEach { item ->
                if (item.description.contains("Synced from Outlook Calendar")) {
                    repository.deleteScheduleItem(item)
                }
            }
        }
    }

    fun syncCalendars() {
        viewModelScope.launch {
            _isSyncing.value = true
            // Simulate cloud connection & processing latencies
            kotlinx.coroutines.delay(1200)

            val currentDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            if (_isGoogleConnected.value) {
                // Insert high priority calendar sync representation
                val existing = repository.allScheduleItems.first().any { it.title == "Google: Tech Sync & Retro" }
                if (!existing) {
                    repository.insertScheduleItem(
                        ScheduleItem(
                            title = "Google: Tech Sync & Retro",
                            description = "Quarterly alignment and roadmap sync. Synced from Google Calendar.",
                            dateStr = currentDateStr,
                            timeStr = "11:30 AM",
                            category = "Work",
                            priority = "High"
                        )
                    )
                }
            }

            if (_isOutlookConnected.value) {
                val existing = repository.allScheduleItems.first().any { it.title == "Outlook: Client Proposal Review" }
                if (!existing) {
                    repository.insertScheduleItem(
                        ScheduleItem(
                            title = "Outlook: Client Proposal Review",
                            description = "Initial review of partner proposals & deliverables. Synced from Outlook Calendar.",
                            dateStr = currentDateStr,
                            timeStr = "03:15 PM",
                            category = "Work",
                            priority = "Medium"
                        )
                    )
                }
            }

            // Sync with physical calendars on-device container if permissions remain valid
            syncDeviceCalendarsIfPermitted()

            _isSyncing.value = false
            generateDailyBriefing()
        }
    }

    private suspend fun syncDeviceCalendarsIfPermitted() = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val contentResolver = context.contentResolver
                val uri = android.provider.CalendarContract.Events.CONTENT_URI
                val projection = arrayOf(
                    android.provider.CalendarContract.Events._ID,
                    android.provider.CalendarContract.Events.TITLE,
                    android.provider.CalendarContract.Events.DESCRIPTION,
                    android.provider.CalendarContract.Events.DTSTART
                )
                
                val selection = "${android.provider.CalendarContract.Events.DTSTART} >= ?"
                val selectionArgs = arrayOf("${System.currentTimeMillis() - 86400000}")
                val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
                
                cursor?.use { c ->
                    val titleIdx = c.getColumnIndex(android.provider.CalendarContract.Events.TITLE)
                    val descIdx = c.getColumnIndex(android.provider.CalendarContract.Events.DESCRIPTION)
                    val startIdx = c.getColumnIndex(android.provider.CalendarContract.Events.DTSTART)
                    
                    while (c.moveToNext()) {
                        val title = if (titleIdx >= 0) c.getString(titleIdx) else "Device Agenda Event"
                        val description = if (descIdx >= 0) c.getString(descIdx) ?: "Imported" else "Imported"
                        val startMs = if (startIdx >= 0) c.getLong(startIdx) else System.currentTimeMillis()
                        
                        val dfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val dfTime = SimpleDateFormat("hh:mm a", Locale.US)
                        val evDate = dfDate.format(Date(startMs))
                        val evTime = dfTime.format(Date(startMs))
                        
                        val isDup = repository.allScheduleItems.first().any { it.title == title && it.dateStr == evDate }
                        if (!isDup) {
                            repository.insertScheduleItem(
                                ScheduleItem(
                                    title = title,
                                    description = "$description (Synced device calendar)",
                                    dateStr = evDate,
                                    timeStr = evTime,
                                    category = "Personal",
                                    priority = "Medium"
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun syncEventToDeviceCalendar(title: String, description: String, dateStr: String, timeStr: String) {
        val context = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_CALENDAR
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val combinedStr = "$dateStr $timeStr"
                val format = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)
                val parsedDate = format.parse(combinedStr) ?: Date()
                val startMs = parsedDate.time
                val endMs = startMs + 3600000 // default 1 hour slot
                
                val values = android.content.ContentValues().apply {
                    put(android.provider.CalendarContract.Events.DTSTART, startMs)
                    put(android.provider.CalendarContract.Events.DTEND, endMs)
                    put(android.provider.CalendarContract.Events.TITLE, title)
                    put(android.provider.CalendarContract.Events.DESCRIPTION, description)
                    put(android.provider.CalendarContract.Events.CALENDAR_ID, 1)
                    put(android.provider.CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }
                context.contentResolver.insert(android.provider.CalendarContract.Events.CONTENT_URI, values)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Budget Logic Methods ---
    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBudget(Budget(category = category, limitAmount = amount))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBudget(budget)
        }
    }

    // --- Advanced Smart Commute and Buffer Reminders Algorithm ---
    fun calculateSmartReminder(item: ScheduleItem): SmartReminderInfo {
        val titleLower = item.title.lowercase()
        
        // Base travel/commute times matching the specific user requirements
        var commuteMinutes = 30 
        var reason = "Standard commute recommendation"
        
        when {
            titleLower.contains("gym") || titleLower.contains("workout") || titleLower.contains("train") -> {
                commuteMinutes = 15
                reason = "Routine fitness session commute"
            }
            titleLower.contains("dinner") || titleLower.contains("sarah") || titleLower.contains("lunch") || titleLower.contains("coffee") || titleLower.contains("restaurant") -> {
                commuteMinutes = 25
                reason = "Social appointment traffic buffer"
            }
            titleLower.contains("doctor") || titleLower.contains("dentist") || titleLower.contains("clinic") || titleLower.contains("appointment") -> {
                commuteMinutes = 40
                reason = "Priority clinical arrival guide"
            }
            titleLower.contains("meeting") || titleLower.contains("sync") || titleLower.contains("standup") || titleLower.contains("retro") || titleLower.contains("board") -> {
                commuteMinutes = 20
                reason = "Active task prep & professional commute"
            }
            item.category.lowercase() == "work" -> {
                commuteMinutes = 35
                reason = "Main career location travel buffer"
            }
        }
        
        // Extra priorities buffer (High -> 15m extra, Medium -> 10m, Low -> 5m)
        val priorityBuffer = when (item.priority.lowercase()) {
            "high" -> 15
            "medium" -> 10
            else -> 5
        }
        
        val totalBufferMinutes = commuteMinutes + priorityBuffer
        
        // Parse "h:mm a" clock format to find exact leave recommendations
        val departureClock = try {
            val format12 = SimpleDateFormat("h:mm a", Locale.US)
            val format24 = SimpleDateFormat("HH:mm", Locale.US)
            
            val calendar = Calendar.getInstance()
            val parsedDate = if (item.timeStr.contains("AM", ignoreCase = true) || item.timeStr.contains("PM", ignoreCase = true)) {
                format12.parse(item.timeStr)
            } else {
                format24.parse(item.timeStr)
            }
            
            if (parsedDate != null) {
                calendar.time = parsedDate
                calendar.add(Calendar.MINUTE, -totalBufferMinutes)
                SimpleDateFormat("h:mm a", Locale.US).format(calendar.time)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
        
        return SmartReminderInfo(
            commuteMinutes = commuteMinutes,
            extraBufferMinutes = priorityBuffer,
            totalOffsetMinutes = totalBufferMinutes,
            recommendedTime = departureClock ?: "Prior to event",
            reason = reason,
            priorityLabel = item.priority
        )
    }

    fun toggleScheduleItemCompletion(item: ScheduleItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateScheduleItem(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun deleteScheduleItem(item: ScheduleItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteScheduleItem(item)
        }
    }

    fun addFinanceItem(title: String, amount: Double, isExpense: Boolean, category: String, dateStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFinanceItem(
                FinanceItem(
                    title = title,
                    amount = amount,
                    isExpense = isExpense,
                    category = category,
                    dateStr = dateStr,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteFinanceItem(item: FinanceItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFinanceItem(item)
        }
    }

    // --- AI Direct Integrations ---

    fun generateDailyBriefing(scheds: List<ScheduleItem> = scheduleItems.value, fins: List<FinanceItem> = financeItems.value) {
        viewModelScope.launch {
            _briefingState.value = BriefingState.Loading
            
            val totalInc = fins.filter { !it.isExpense }.sumOf { it.amount }
            val totalExp = fins.filter { it.isExpense }.sumOf { it.amount }
            val bal = totalInc - totalExp

            val scheduleListString = scheds.joinToString("\n") { 
                "- [${if (it.isCompleted) "Completed" else "Pending"}] ${it.title} (${it.category}, Priority: ${it.priority}) at ${it.timeStr}" 
            }

            val budgetListString = budgets.value.joinToString("\n") { b ->
                val spent = fins.filter { it.isExpense && it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
                "- Cat: ${b.category} | Limit: ₹${b.limitAmount} | Spent so far: ₹${spent}"
            }

            val systemMessage = """
                You are an executive personal assistant. Analyze the user's daily data:
                - Net Wallet Balance: ₹$bal (Income: ₹${totalInc}, Expense: ₹${totalExp})
                - Category Budgets and Current Spend status:
                $budgetListString
                - Schedule Agenda: 
                $scheduleListString
                
                Generate a concise, elite 1-paragraph summary followed by three scannable bullet points including:
                1. Agenda Highlight: What primary schedule items they must focus on today.
                2. Budget Alert or Status: A brief, sharp personal budgeting remark (mention whether they are exceeding any budgets).
                3. High priority travel/appointment advisory based on smart commute buffers.
                Keep it completely professional and highly actionable in under 120 words. Do not use markdown titles.
            """.trimIndent()

            val brief = withContext(Dispatchers.IO) {
                GeminiClient.fetchGeminiResponse(
                    prompt = "Generate my smart executive briefing right now.",
                    systemPrompt = systemMessage
                )
            }
            if (brief.startsWith("Error:") || brief.contains("offline")) {
                _briefingState.value = BriefingState.Error(brief)
            } else {
                _briefingState.value = BriefingState.Success(brief)
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            _isChatLoading.value = true

            val scheds = scheduleItems.value
            val fins = financeItems.value
            val totalInc = fins.filter { !it.isExpense }.sumOf { it.amount }
            val totalExp = fins.filter { it.isExpense }.sumOf { it.amount }
            val netBal = totalInc - totalExp

            val schedContext = scheds.joinToString("\n") { 
                "${it.title} on ${it.dateStr} at ${it.timeStr} [${if (it.isCompleted) "Done" else "Pending"}]" 
            }
            val finContext = fins.joinToString("\n") { 
                "${if (it.isExpense) "Expense" else "Income"}: ${it.title} of ₹${it.amount} on ${it.dateStr}" 
            }

            val systemPrompt = """
                You are a smart Personal Assistant integrated with local database access.
                Current Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}
                
                Current Schedule:
                $schedContext
                
                Current Finance Book:
                $finContext
                Total Balance: ₹$netBal (Income: ₹$totalInc, Expense: ₹$totalExp)
                
                Instructions:
                - If the user expresses an intent to add scheduling items or financial items, e.g., "Remind me to call Mom at 5 PM" or "Add grocery expense ₹450", reply encouragingly acknowledging that you can register it.
                - When registering a transaction or chore requested by the user, end the message with a specialized JSON block in double braces so the application can automatically parse and perform the insertion!
                
                JSON Format schema for automatic registration:
                If parsing a Schedule item:
                {{ "action": "add_schedule", "title": "Call Mom", "description": "Auto parsed by assistant", "dateStr": "yyyy-MM-dd", "timeStr": "5:00 PM", "category": "Personal", "priority": "Medium" }}
                
                If parsing a Finance item:
                {{ "action": "add_finance", "title": "Groceries", "amount": 45.0, "isExpense": true, "category": "Food", "dateStr": "yyyy-MM-dd" }}
                
                The dateStr should defaults to the current date "${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}" unless another day is explicitly mentioned.
                Make sure you output only ONE JSON instruction in those double curly braces per message if requested, alongside a professional chat reply.
            """.trimIndent()

            val responseText = withContext(Dispatchers.IO) {
                // Build simple API request contents:
                val apiHistory = _chatMessages.value.takeLast(6).map {
                    GeminiContent(parts = listOf(GeminiPart(text = "[${it.sender}] ${it.text}")))
                }
                GeminiClient.fetchGeminiResponse(
                    prompt = text,
                    systemPrompt = systemPrompt,
                    conversationHistory = apiHistory
                )
            }

            // Parse any hidden JSON instruction from double curly braces
            parseAndExecuteSmartAction(responseText)

            // Clean response text from the nested JSON command for user readability
            val cleanedText = responseText.replace(Regex("\\{\\{.*?\\}\\}", RegexOption.DOT_MATCHES_ALL), "").trim()

            _chatMessages.update { it + ChatMessage(sender = "assistant", text = cleanedText) }
            _isChatLoading.value = false
        }
    }

    private suspend fun parseAndExecuteSmartAction(response: String) {
        try {
            val match = Regex("\\{\\{(.*?)\\}\\}", RegexOption.DOT_MATCHES_ALL).find(response)
            if (match != null) {
                val jsonStr = "{" + match.groupValues[1] + "}"
                // Direct light-weight manual string-based parsing to avoid reflection issues and match reliably:
                if (jsonStr.contains("\"action\":\\s*\"add_schedule\"".toRegex())) {
                    val title = parseJsonField(jsonStr, "title") ?: "AI Suggestion"
                    val desc = parseJsonField(jsonStr, "description") ?: "Auto parsed by assistant"
                    val dateStr = parseJsonField(jsonStr, "dateStr") ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val timeStr = parseJsonField(jsonStr, "timeStr") ?: "12:00 PM"
                    val category = parseJsonField(jsonStr, "category") ?: "Personal"
                    val priority = parseJsonField(jsonStr, "priority") ?: "Medium"
                    
                    withContext(Dispatchers.IO) {
                        repository.insertScheduleItem(
                            ScheduleItem(
                                title = title,
                                description = desc,
                                dateStr = dateStr,
                                timeStr = timeStr,
                                category = category,
                                priority = priority,
                                isCompleted = false
                            )
                        )
                    }
                } else if (jsonStr.contains("\"action\":\\s*\"add_finance\"".toRegex())) {
                    val title = parseJsonField(jsonStr, "title") ?: "AI Expense"
                    val amtStr = parseJsonField(jsonStr, "amount") ?: "0.0"
                    val amount = amtStr.toDoubleOrNull() ?: 0.0
                    val isExpense = jsonStr.contains("\"isExpense\":\\s*true".toRegex())
                    val category = parseJsonField(jsonStr, "category") ?: "Other"
                    val dateStr = parseJsonField(jsonStr, "dateStr") ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

                    withContext(Dispatchers.IO) {
                        repository.insertFinanceItem(
                            FinanceItem(
                                title = title,
                                amount = amount,
                                isExpense = isExpense,
                                category = category,
                                dateStr = dateStr,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseJsonField(json: String, field: String): String? {
        val pattern = "\"$field\"\\s*:\\s*\"?([^\",\\}]+)\"?".toRegex()
        val match = pattern.find(json)
        return match?.groupValues?.get(1)?.trim()?.removeSurrounding("\"")
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "assistant",
                text = "Chat cleared. I am Thinkuu. Ask me about your finances or write things like 'Add an appointment at 3 PM tomorrow' to update your schedule."
            )
        )
    }
}
