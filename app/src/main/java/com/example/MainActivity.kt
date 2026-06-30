package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import com.example.data.PreferenceManager
import com.example.ui.DeadlineWorker
import com.example.ui.PlannerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.PlannerViewModelFactory

class MainActivity : ComponentActivity() {

  private lateinit var database: PlannerDatabase
  private lateinit var repository: PlannerRepository
  private lateinit var prefManager: PreferenceManager

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    // Handle permission result if needed
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Request Notification Permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }

    // Initialize Room Database offline-first local storage
    database = PlannerDatabase.getDatabase(applicationContext)

    repository = PlannerRepository(database.plannerDao())
    prefManager = com.example.data.PreferenceManager(applicationContext)
    
    scheduleBackgroundWork()

    enableEdgeToEdge()

    setContent {
      var isDarkTheme by rememberSaveable { mutableStateOf(true) }

      // Instantiate ViewModel with Simple Factory Injection
      val viewModel: PlannerViewModel by viewModels {
        PlannerViewModelFactory(repository, prefManager)
      }

      val themeAccent by viewModel.themeAccent.collectAsState(initial = "Indigo")

      MyApplicationTheme(darkTheme = isDarkTheme, themeAccent = themeAccent) {
        PlannerApp(
          viewModel = viewModel,
          isDarkTheme = isDarkTheme,
          onThemeToggle = { isDarkTheme = !isDarkTheme }
        )
      }
    }
  }

  private fun scheduleBackgroundWork() {
    val workRequest = PeriodicWorkRequestBuilder<DeadlineWorker>(1, java.util.concurrent.TimeUnit.HOURS)
      .build()
      
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
      "DeadlineChecker",
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )
  }
}
