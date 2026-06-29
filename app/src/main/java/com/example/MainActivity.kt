package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.room.Room
import com.example.data.PlannerDatabase
import com.example.data.PlannerRepository
import com.example.ui.PlannerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.PlannerViewModelFactory

class MainActivity : ComponentActivity() {

  private lateinit var database: PlannerDatabase
  private lateinit var repository: PlannerRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Room Database offline-first local storage
    database = Room.databaseBuilder(
      applicationContext,
      PlannerDatabase::class.java,
      "planner_pro_database"
    ).fallbackToDestructiveMigration().build()

    repository = PlannerRepository(database.plannerDao())

    enableEdgeToEdge()

    setContent {
      var isDarkTheme by rememberSaveable { mutableStateOf(true) }

      // Instantiate ViewModel with Simple Factory Injection
      val viewModel: PlannerViewModel by viewModels {
        PlannerViewModelFactory(repository)
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
}
