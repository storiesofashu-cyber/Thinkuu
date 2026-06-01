package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateStr: String, // e.g. "2026-05-27"
    val timeStr: String, // e.g. "10:30 AM"
    val category: String, // "Work", "Personal", "Financial", "Chore", "Health"
    val priority: String, // "High", "Medium", "Low"
    val isCompleted: Boolean = false
)

@Entity(tableName = "finance_items")
data class FinanceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isExpense: Boolean, // true for expense, false for income
    val category: String, // "Salary", "Food", "Rent", "Utilities", "Shopping", "Transport", "Investment", "Other", "Groceries", "Entertainment"
    val dateStr: String, // e.g. "2026-05-27"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String,
    val limitAmount: Double
)

data class SmartReminderInfo(
    val commuteMinutes: Int,
    val extraBufferMinutes: Int,
    val totalOffsetMinutes: Int,
    val recommendedTime: String,
    val reason: String,
    val priorityLabel: String
)
