package com.example.ui

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeadlineWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = PlannerDatabase.getDatabase(applicationContext)
        val repository = PlannerRepository(database.plannerDao())
        val notificationHelper = NotificationHelper(applicationContext)

        try {
            val tasks = repository.allTasks.first()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            tasks.forEach { task ->
                if (!task.isDone && !task.isNotified && task.deadline == today) {
                    notificationHelper.showDeadlineNotification(
                        title = "Deadline Tugas: ${task.subject}",
                        message = "Tugas '${task.text}' harus selesai hari ini! (Pengecekan Background)"
                    )
                    // Mark as notified in DB
                    repository.insertTask(task.copy(isNotified = true))
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
