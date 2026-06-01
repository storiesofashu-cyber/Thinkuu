package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    // --- Schedule Queries ---
    @Query("SELECT * FROM schedule_items ORDER BY dateStr ASC, timeStr ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem)

    @Update
    suspend fun updateScheduleItem(item: ScheduleItem)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)

    @Query("DELETE FROM schedule_items WHERE id = :id")
    suspend fun deleteScheduleItemById(id: Int)

    // --- Finance Queries ---
    @Query("SELECT * FROM finance_items ORDER BY timestamp DESC")
    fun getAllFinanceItems(): Flow<List<FinanceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceItem(item: FinanceItem)

    @Delete
    suspend fun deleteFinanceItem(item: FinanceItem)

    @Query("DELETE FROM finance_items WHERE id = :id")
    suspend fun deleteFinanceItemById(id: Int)

    // --- Budget Queries ---
    @Query("SELECT * FROM budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)
}
