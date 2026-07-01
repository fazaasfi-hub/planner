package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val plannerDao: PlannerDao) {
    // Schedules
    val allSchedules: Flow<List<StudySchedule>> = plannerDao.getAllSchedules()
    suspend fun insertSchedule(schedule: StudySchedule) = plannerDao.insertSchedule(schedule)
    suspend fun deleteSchedule(schedule: StudySchedule) = plannerDao.deleteSchedule(schedule)
    suspend fun clearSchedules() = plannerDao.clearAllSchedules()

    // Time Slots
    val allTimeSlots: Flow<List<TimeSlot>> = plannerDao.getAllTimeSlots()
    suspend fun insertTimeSlot(timeSlot: TimeSlot) = plannerDao.insertTimeSlot(timeSlot)
    suspend fun deleteTimeSlot(timeSlot: TimeSlot) = plannerDao.deleteTimeSlot(timeSlot)
    suspend fun clearTimeSlots() = plannerDao.clearAllTimeSlots()

    // Tasks
    val allTasks: Flow<List<StudyTask>> = plannerDao.getAllTasks()
    suspend fun insertTask(task: StudyTask) = plannerDao.insertTask(task)
    suspend fun deleteTask(task: StudyTask) = plannerDao.deleteTask(task)
    suspend fun clearTasks() = plannerDao.clearAllTasks()

    // Workout logs
    val allWorkoutLogs: Flow<List<WorkoutLog>> = plannerDao.getAllWorkoutLogs()
    suspend fun insertWorkoutLog(log: WorkoutLog) = plannerDao.insertWorkoutLog(log)
    suspend fun deleteWorkoutLog(log: WorkoutLog) = plannerDao.deleteWorkoutLog(log)
    suspend fun clearWorkoutLogs() = plannerDao.clearAllWorkoutLogs()

    // Step logs
    val allStepLogs: Flow<List<StepLog>> = plannerDao.getAllStepLogs()
    suspend fun insertStepLog(log: StepLog) = plannerDao.insertStepLog(log)
    suspend fun deleteStepLog(log: StepLog) = plannerDao.deleteStepLog(log)
    suspend fun clearStepLogs() = plannerDao.clearAllStepLogs()

    // Saving goals
    val allSavingGoals: Flow<List<SavingGoal>> = plannerDao.getAllSavingGoals()
    suspend fun insertSavingGoal(goal: SavingGoal) = plannerDao.insertSavingGoal(goal)
    suspend fun deleteSavingGoal(goal: SavingGoal) = plannerDao.deleteSavingGoal(goal)
    suspend fun clearSavingGoals() = plannerDao.clearAllSavingGoals()

    // Donghua Items
    val allDonghuaItems: Flow<List<DonghuaItem>> = plannerDao.getAllDonghuaItems()
    suspend fun insertDonghua(item: DonghuaItem) = plannerDao.insertDonghua(item)
    suspend fun deleteDonghua(item: DonghuaItem) = plannerDao.deleteDonghua(item)
    suspend fun clearDonghua() = plannerDao.clearAllDonghua()

    // Budget transactions
    val allTransactions: Flow<List<BudgetTransaction>> = plannerDao.getAllTransactions()
    suspend fun insertTransaction(transaction: BudgetTransaction) = plannerDao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: BudgetTransaction) = plannerDao.deleteTransaction(transaction)
    suspend fun clearTransactions() = plannerDao.clearAllTransactions()

    // Bini Gweh (Waifu) Items
    val allBiniGwehItems: Flow<List<BiniGwehItem>> = plannerDao.getAllBiniGwehItems()
    suspend fun insertBiniGweh(item: BiniGwehItem) = plannerDao.insertBiniGweh(item)
    suspend fun deleteBiniGweh(item: BiniGwehItem) = plannerDao.deleteBiniGweh(item)
    suspend fun clearBiniGweh() = plannerDao.clearAllBiniGweh()
}
