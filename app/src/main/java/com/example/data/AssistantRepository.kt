package com.example.data

import kotlinx.coroutines.flow.Flow

class AssistantRepository(private val assistantDao: AssistantDao) {
    val allScheduleItems: Flow<List<ScheduleItem>> = assistantDao.getAllScheduleItems()
    val allFinanceItems: Flow<List<FinanceItem>> = assistantDao.getAllFinanceItems()
    val allBudgets: Flow<List<Budget>> = assistantDao.getAllBudgets()

    suspend fun insertScheduleItem(item: ScheduleItem) {
        assistantDao.insertScheduleItem(item)
    }

    suspend fun updateScheduleItem(item: ScheduleItem) {
        assistantDao.updateScheduleItem(item)
    }

    suspend fun deleteScheduleItem(item: ScheduleItem) {
        assistantDao.deleteScheduleItem(item)
    }

    suspend fun deleteScheduleItemById(id: Int) {
        assistantDao.deleteScheduleItemById(id)
    }

    suspend fun insertFinanceItem(item: FinanceItem) {
        assistantDao.insertFinanceItem(item)
    }

    suspend fun deleteFinanceItem(item: FinanceItem) {
        assistantDao.deleteFinanceItem(item)
    }

    suspend fun deleteFinanceItemById(id: Int) {
        assistantDao.deleteFinanceItemById(id)
    }

    suspend fun insertBudget(budget: Budget) {
        assistantDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        assistantDao.deleteBudget(budget)
    }
}
