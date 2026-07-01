package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "study_schedules")
data class StudySchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val day: String, // SENIN, SELASA, etc.
    val startTime: String, // e.g. "07:00"
    val endTime: String, // e.g. "08:00"
    val subjectName: String,
    val colorHex: String = "#4f46e5"
)

@Entity(tableName = "time_slots")
data class TimeSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: String,
    val endTime: String
)

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isDone: Boolean = false,
    val subject: String = "",
    val deadline: String = "", // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis(),
    val isNotified: Boolean = false
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sportType: String,
    val durationMinutes: Int,
    val weightKg: Float = 60f,
    val caloriesBurned: Int,
    val date: Long = System.currentTimeMillis(),
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0
)

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val steps: Int,
    val target: Int = 10000
)

@Entity(tableName = "saving_goals")
data class SavingGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double
)

@Entity(tableName = "donghua_items")
data class DonghuaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalEpisodes: Int = 12,
    val currentEpisode: Int = 0,
    val status: String = "watching", // watching, finished, waiting
    val rating: Int = 5,
    val isFavorite: Boolean = false,
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_transactions")
data class BudgetTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val amount: Double,
    val type: String, // income, expense
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "bini_gweh_items")
data class BiniGwehItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val imageUrl: String,
    val sourceName: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface PlannerDao {
    // Study schedules
    @Query("SELECT * FROM study_schedules")
    fun getAllSchedules(): Flow<List<StudySchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: StudySchedule)

    @Delete
    suspend fun deleteSchedule(schedule: StudySchedule)

    @Query("DELETE FROM study_schedules")
    suspend fun clearAllSchedules()

    // Time Slots
    @Query("SELECT * FROM time_slots ORDER BY startTime ASC")
    fun getAllTimeSlots(): Flow<List<TimeSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlot)

    @Delete
    suspend fun deleteTimeSlot(timeSlot: TimeSlot)

    @Query("DELETE FROM time_slots")
    suspend fun clearAllTimeSlots()

    // Study Tasks
    @Query("SELECT * FROM study_tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask)

    @Delete
    suspend fun deleteTask(task: StudyTask)

    @Query("DELETE FROM study_tasks")
    suspend fun clearAllTasks()

    // Workout Logs
    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLog)

    @Query("DELETE FROM workout_logs")
    suspend fun clearAllWorkoutLogs()

    // Step Logs
    @Query("SELECT * FROM step_logs ORDER BY date DESC")
    fun getAllStepLogs(): Flow<List<StepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepLog(log: StepLog)

    @Delete
    suspend fun deleteStepLog(log: StepLog)

    @Query("DELETE FROM step_logs")
    suspend fun clearAllStepLogs()

    // Saving Goals
    @Query("SELECT * FROM saving_goals")
    fun getAllSavingGoals(): Flow<List<SavingGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingGoal(goal: SavingGoal)

    @Delete
    suspend fun deleteSavingGoal(goal: SavingGoal)

    @Query("DELETE FROM saving_goals")
    suspend fun clearAllSavingGoals()

    // Donghua Items
    @Query("SELECT * FROM donghua_items ORDER BY updatedAt DESC")
    fun getAllDonghuaItems(): Flow<List<DonghuaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonghua(item: DonghuaItem)

    @Delete
    suspend fun deleteDonghua(item: DonghuaItem)

    @Query("DELETE FROM donghua_items")
    suspend fun clearAllDonghua()

    // Budget Transactions
    @Query("SELECT * FROM budget_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<BudgetTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BudgetTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: BudgetTransaction)

    @Query("DELETE FROM budget_transactions")
    suspend fun clearAllTransactions()

    // Bini Gweh (Waifu) Items
    @Query("SELECT * FROM bini_gweh_items ORDER BY addedAt DESC")
    fun getAllBiniGwehItems(): Flow<List<BiniGwehItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiniGweh(item: BiniGwehItem)

    @Delete
    suspend fun deleteBiniGweh(item: BiniGwehItem)

    @Query("DELETE FROM bini_gweh_items")
    suspend fun clearAllBiniGweh()
}

@Database(
    entities = [
        StudySchedule::class,
        TimeSlot::class,
        StudyTask::class,
        WorkoutLog::class,
        SavingGoal::class,
        DonghuaItem::class,
        BudgetTransaction::class,
        StepLog::class,
        BiniGwehItem::class
    ],
    version = 7,
    exportSchema = false
)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile
        private var INSTANCE: PlannerDatabase? = null

        fun getDatabase(context: android.content.Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "planner_pro_database"
                )
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
