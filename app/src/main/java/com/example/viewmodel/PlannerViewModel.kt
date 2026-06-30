package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    Dashboard, Study, Workout, Saving, Donghua, Chart, Budget
}

class PlannerViewModel(
    private val repository: PlannerRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {

    private val weatherService = WeatherService()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Weather state
    private val _weatherInfo = MutableStateFlow<WeatherInfo?>(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo.asStateFlow()

    private val _weatherLoading = MutableStateFlow(false)
    val weatherLoading: StateFlow<Boolean> = _weatherLoading.asStateFlow()

    private val _weatherError = MutableStateFlow<String?>(null)
    val weatherError: StateFlow<String?> = _weatherError.asStateFlow()

    // Toast state
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Notification Event
    private val _deadlineEvent = MutableSharedFlow<StudyTask>()
    val deadlineEvent = _deadlineEvent.asSharedFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun stopLoading() {
        _isLoading.value = false
        startDeadlineChecker()
    }

    private fun startDeadlineChecker() {
        viewModelScope.launch {
            tasks.collect { taskList ->
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                taskList.forEach { task ->
                    if (!task.isDone && !task.isNotified && task.deadline == today) {
                        _deadlineEvent.emit(task)
                        // Mark as notified in DB
                        repository.insertTask(task.copy(isNotified = true))
                    }
                }
            }
        }
    }

    // Database flows
    val schedules: StateFlow<List<StudySchedule>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeSlots: StateFlow<List<TimeSlot>> = repository.allTimeSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<StudyTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stepLogs: StateFlow<List<StepLog>> = repository.allStepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingGoals: StateFlow<List<SavingGoal>> = repository.allSavingGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donghuaItems: StateFlow<List<DonghuaItem>> = repository.allDonghuaItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<BudgetTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ============================================================
    // STATE FLOW FOR 20 EXCITING FEATURES
    // ============================================================
    // 1. Water Intake (Pelacak Air Minum Harian)
    private val _waterIntake = MutableStateFlow(0)
    val waterIntake: StateFlow<Int> = _waterIntake.asStateFlow()

    private val _selectedCardId = MutableStateFlow<Any?>(null)
    val selectedCardId: StateFlow<Any?> = _selectedCardId.asStateFlow()

    fun selectCard(id: Any?) {
        _selectedCardId.value = id
    }

    fun addWaterGlass() {
        if (_waterIntake.value < 12) {
            _waterIntake.value += 1
            showToast("Minum air +1 gelas! Semangat hidrasi \uD83E\uDD62")
        }
    }

    fun subtractWaterGlass() {
        if (_waterIntake.value > 0) {
            _waterIntake.value -= 1
        }
    }

    // 2. Budget Thresholds/Limit (Limit Pengeluaran Bulanan)
    private val _budgetLimit = MutableStateFlow(1500000.0) // Default 1.5M IDR
    val budgetLimit: StateFlow<Double> = _budgetLimit.asStateFlow()

    fun updateBudgetLimit(newLimit: Double) {
        if (newLimit >= 0) {
            _budgetLimit.value = newLimit
            showToast("Limit anggaran diperbarui ke " + formatRupiahViewModel(newLimit))
        }
    }

    private fun formatRupiahViewModel(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,###.00")
        val symbols = java.text.DecimalFormatSymbols(java.util.Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        formatter.decimalFormatSymbols = symbols
        return "Rp " + formatter.format(amount)
    }

    // 3. Accent Colors Themes Choice
    private val _themeAccent = MutableStateFlow(prefManager.getThemeAccent())
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()
    
    val availableThemes = listOf(
        "Indigo", "Emerald", "Amber", "Teal", "Red", "Pink", "Purple", "DeepPurple",
        "Blue", "LightBlue", "Cyan", "Green", "LightGreen", "Lime", "Yellow",
        "Orange", "DeepOrange", "Brown", "BlueGrey", "Grey"
    )

    fun setThemeAccent(accent: String) {
        _themeAccent.value = accent
        prefManager.setThemeAccent(accent)
        showToast("Aksen tema diganti ke $accent \uD83C\uDFA8")
    }

    // 4. Pomodoro Focus Timer State
    private val _pomodoroTimeLeft = MutableStateFlow(1500) // 25 mins = 1500s
    val pomodoroTimeLeft: StateFlow<Int> = _pomodoroTimeLeft.asStateFlow()

    private val _pomodoroRunning = MutableStateFlow(false)
    val pomodoroRunning: StateFlow<Boolean> = _pomodoroRunning.asStateFlow()

    private val _pomodoroCompleted = MutableStateFlow(0)
    val pomodoroCompleted: StateFlow<Int> = _pomodoroCompleted.asStateFlow()

    fun updatePomodoroTime(seconds: Int) {
        _pomodoroTimeLeft.value = seconds
    }

    fun togglePomodoro() {
        _pomodoroRunning.value = !_pomodoroRunning.value
    }

    fun incrementPomodoroCount() {
        _pomodoroCompleted.value += 1
    }

    // 5. User Profile Customization
    private val _profileName = MutableStateFlow("Faza Asfi")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileBio = MutableStateFlow("Professional Full-Stack Developer | Productivity Enthusiast")
    val profileBio: StateFlow<String> = _profileBio.asStateFlow()

    private val _avatarIndex = MutableStateFlow(0)
    val avatarIndex: StateFlow<Int> = _avatarIndex.asStateFlow()

    fun updateProfile(name: String, bio: String, avatar: Int) {
        _profileName.value = name
        _profileBio.value = bio
        _avatarIndex.value = avatar
        showToast("Profil berhasil diperbarui! \uD83D\uDCBB")
    }

    // 6. Scratchpad Notes (Catatan Cepat Coret-Coret)
    private val _scratchpad = MutableStateFlow("")
    val scratchpad: StateFlow<String> = _scratchpad.asStateFlow()

    fun updateScratchpad(text: String) {
        _scratchpad.value = text
    }

    // 7. Cloud Sync and Local Backup Simulator
    private val _lastBackupTime = MutableStateFlow("Belum dicadangkan")
    val lastBackupTime: StateFlow<String> = _lastBackupTime.asStateFlow()

    fun triggerLocalBackup() {
        viewModelScope.launch {
            showToast("Menyiapkan pencadangan data offline-first...")
            kotlinx.coroutines.delay(1000)
            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            _lastBackupTime.value = "Hari ini pukul " + sdf.format(java.util.Date())
            showToast("Database Room berhasil dicadangkan ke lokal! \uD83D\uDCBE")
        }
    }

    init {
        // Populate default time slots if empty
        viewModelScope.launch {
            repository.allTimeSlots.first().let { slots ->
                if (slots.isEmpty()) {
                    repository.insertTimeSlot(TimeSlot(startTime = "07:00", endTime = "08:00"))
                    repository.insertTimeSlot(TimeSlot(startTime = "08:00", endTime = "09:00"))
                    repository.insertTimeSlot(TimeSlot(startTime = "09:00", endTime = "10:00"))
                }
            }
        }

        // Proactive deadline check on startup
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            repository.allTasks.collect { tasks ->
                tasks.forEach { task ->
                    if (!task.isDone && !task.isNotified && task.deadline == today) {
                        _deadlineEvent.emit(task)
                        // Mark as notified in DB to avoid double trigger
                        repository.insertTask(task.copy(isNotified = true))
                    }
                }
            }
        }

        // Initial weather fetch
        searchWeather(prefManager.getLastCity())
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastMessage.emit(message)
        }
    }

    // Weather search
    fun searchWeather(city: String) {
        if (city.isBlank()) {
            showToast("Nama kota tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _weatherLoading.value = true
            _weatherError.value = null
            val info = weatherService.fetchWeather(city)
            if (info != null) {
                _weatherInfo.value = info
                prefManager.setLastCity(city)
            } else {
                _weatherError.value = "Kota tidak ditemukan"
                showToast("Gagal memuat cuaca")
            }
            _weatherLoading.value = false
        }
    }

    // Study schedules
    fun addSchedule(day: String, start: String, end: String, mapel: String, color: String) {
        viewModelScope.launch {
            if (start >= end) {
                showToast("Jam selesai harus setelah jam mulai")
                return@launch
            }
            val schedule = StudySchedule(
                day = day,
                startTime = start,
                endTime = end,
                subjectName = mapel,
                colorHex = color
            )
            repository.insertSchedule(schedule)
            showToast("Jadwal $mapel disimpan")
        }
    }

    fun deleteSchedule(schedule: StudySchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            showToast("Jadwal dihapus")
        }
    }

    fun clearSchedules() {
        viewModelScope.launch {
            repository.clearSchedules()
            showToast("Semua jadwal dibersihkan")
        }
    }

    // Time Slots
    fun addTimeSlot(start: String, end: String) {
        viewModelScope.launch {
            if (start >= end) {
                showToast("Waktu selesai harus setelah mulai")
                return@launch
            }
            repository.insertTimeSlot(TimeSlot(startTime = start, endTime = end))
            showToast("Slot waktu ditambahkan")
        }
    }

    fun deleteTimeSlot(timeSlot: TimeSlot) {
        viewModelScope.launch {
            repository.deleteTimeSlot(timeSlot)
            showToast("Slot waktu dihapus")
        }
    }

    // Tasks
    fun addTask(text: String, subject: String, deadline: String) {
        viewModelScope.launch {
            if (text.isBlank()) {
                showToast("Tulis deskripsi tugas")
                return@launch
            }
            val task = StudyTask(
                text = text,
                subject = subject,
                deadline = deadline,
                isDone = false
            )
            repository.insertTask(task)
            showToast("Tugas ditambahkan!")
        }
    }

    fun toggleTask(task: StudyTask) {
        viewModelScope.launch {
            repository.insertTask(task.copy(isDone = !task.isDone))
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Tugas dihapus")
        }
    }

    fun clearTasks() {
        viewModelScope.launch {
            repository.clearTasks()
            showToast("Semua tugas dibersihkan")
        }
    }

    // Workout logs
    fun addWorkoutLog(
        name: String,
        sportType: String,
        duration: Int,
        weight: Float,
        calories: Int,
        exercise: String,
        sets: Int,
        reps: Int
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showToast("Nama olahraga wajib diisi")
                return@launch
            }
            if (duration <= 0) {
                showToast("Durasi harus lebih dari 0 menit")
                return@launch
            }
            val log = WorkoutLog(
                name = name,
                sportType = sportType,
                durationMinutes = duration,
                weightKg = weight,
                caloriesBurned = calories,
                exerciseName = exercise,
                sets = sets,
                reps = reps
            )
            repository.insertWorkoutLog(log)
            showToast("$name ditambahkan! ($calories kalori)")
        }
    }

    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(log)
            showToast("Catatan olahraga dihapus")
        }
    }

    fun clearWorkoutLogs() {
        viewModelScope.launch {
            repository.clearWorkoutLogs()
            showToast("Semua riwayat olahraga dibersihkan")
        }
    }

    // Step Tracker features
    private val _isTrackingSteps = MutableStateFlow(true)
    val isTrackingSteps: StateFlow<Boolean> = _isTrackingSteps.asStateFlow()

    private val _todayStepGoal = MutableStateFlow(10000)
    val todayStepGoal: StateFlow<Int> = _todayStepGoal.asStateFlow()

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    val todaySteps: StateFlow<Int> = stepLogs
        .map { logs ->
            logs.find { isSameDay(it.date, System.currentTimeMillis()) }?.steps ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun startStepTracking() {
        _isTrackingSteps.value = true
    }

    fun incrementActiveSteps(amount: Int) {
        viewModelScope.launch {
            val currentList = stepLogs.value
            val todayLog = currentList.find { isSameDay(it.date, System.currentTimeMillis()) }
            if (todayLog != null) {
                val updatedLog = todayLog.copy(steps = todayLog.steps + amount)
                repository.insertStepLog(updatedLog)
            } else {
                val newLog = StepLog(steps = amount, target = _todayStepGoal.value)
                repository.insertStepLog(newLog)
            }
        }
    }

    fun updateStepGoal(newGoal: Int) {
        if (newGoal > 0) {
            _todayStepGoal.value = newGoal
            viewModelScope.launch {
                val currentList = stepLogs.value
                val todayLog = currentList.find { isSameDay(it.date, System.currentTimeMillis()) }
                if (todayLog != null) {
                    val updatedLog = todayLog.copy(target = newGoal)
                    repository.insertStepLog(updatedLog)
                }
            }
            showToast("Target langkah diperbarui ke $newGoal")
        }
    }

    fun addStepLog(steps: Int, target: Int = 10000) {
        viewModelScope.launch {
            if (steps <= 0) {
                showToast("Jumlah langkah harus lebih dari 0")
                return@launch
            }
            val currentList = stepLogs.value
            val todayLog = currentList.find { isSameDay(it.date, System.currentTimeMillis()) }
            if (todayLog != null) {
                val updatedLog = todayLog.copy(steps = todayLog.steps + steps)
                repository.insertStepLog(updatedLog)
            } else {
                val newLog = StepLog(
                    steps = steps,
                    target = target
                )
                repository.insertStepLog(newLog)
            }
            showToast("Langkah berhasil ditambahkan!")
        }
    }

    fun stopStepTracking(save: Boolean) {
        // Obsoleted by auto-tracking, kept for signature compatibility
    }

    fun deleteStepLog(log: StepLog) {
        viewModelScope.launch {
            repository.deleteStepLog(log)
            showToast("Riwayat langkah dihapus")
        }
    }

    fun clearStepLogs() {
        viewModelScope.launch {
            repository.clearStepLogs()
            showToast("Semua riwayat langkah dibersihkan")
        }
    }

    // Saving goals
    fun addSavingGoal(name: String, target: Double, current: Double) {
        viewModelScope.launch {
            if (name.isBlank() || target <= 0) {
                showToast("Nama & nominal target wajib diisi")
                return@launch
            }
            val goal = SavingGoal(
                name = name,
                targetAmount = target,
                currentAmount = current
            )
            repository.insertSavingGoal(goal)
            showToast("Target tabungan disimpan")
        }
    }

    fun editSavingGoal(goal: SavingGoal, name: String, target: Double, current: Double) {
        viewModelScope.launch {
            if (name.isBlank() || target <= 0) {
                showToast("Nama & nominal target wajib diisi")
                return@launch
            }
            repository.insertSavingGoal(goal.copy(
                name = name,
                targetAmount = target,
                currentAmount = current
            ))
            showToast("Target tabungan diupdate")
        }
    }

    fun deleteSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goal)
            showToast("Target tabungan dihapus")
        }
    }

    fun clearSavingGoals() {
        viewModelScope.launch {
            repository.clearSavingGoals()
            showToast("Semua target tabungan dibersihkan")
        }
    }

    // Donghua Items
    fun addDonghua(title: String, total: Int, current: Int, status: String, rating: Int, isFav: Boolean, manualCoverUrl: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                showToast("Judul wajib diisi")
                return@launch
            }
            if (current > total && total > 0) {
                showToast("Episode saat ini tidak boleh melebihi total")
                return@launch
            }
            
            var coverUrl: String? = if (manualCoverUrl.isNotBlank()) manualCoverUrl else null
            
            if (coverUrl == null) {
                val searchTerms = mutableListOf<String>()
                searchTerms.add(title.trim())
                
                val cleanTitle = title.trim().split("(")[0].split("[")[0].trim()
                if (cleanTitle != title.trim()) {
                    searchTerms.add(cleanTitle)
                }
                
                // Add "Douluo Dalu" for "Soul Land"
                if (title.contains("Soul Land", ignoreCase = true) && !title.contains("Douluo Dalu", ignoreCase = true)) {
                    searchTerms.add("Douluo Dalu")
                }
                
                // Add "Donghua" suffix
                if (!title.contains("Donghua", ignoreCase = true)) {
                    searchTerms.add("$cleanTitle Donghua")
                }

                val uniqueTerms = searchTerms.distinct()
                android.util.Log.d("Planner", "Searching for $title with terms: $uniqueTerms")

                searchLoop@for (term in uniqueTerms) {
                    if (term.isBlank()) continue
                    
                    try {
                        val response = com.example.network.JikanClient.api.searchAnime(term)
                        if (response.data.isNotEmpty()) {
                            coverUrl = response.data.first().images.jpg?.image_url
                            if (coverUrl != null) {
                                android.util.Log.d("Planner", "Jikan found URL for '$term': $coverUrl")
                                break@searchLoop
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Planner", "Jikan error for $term: ${e.message}")
                    }

                    try {
                        val aniListQuery = """
                            query (${'$'}s: String) {
                              Media(search: ${'$'}s, type: ANIME) {
                                coverImage {
                                  large
                                }
                              }
                            }
                        """.trimIndent()
                        val request = com.example.network.AniListRequest(aniListQuery, mapOf("s" to term))
                        val response = com.example.network.AniListClient.api.searchAnime(request)
                        coverUrl = response.data.Media?.coverImage?.large
                        if (coverUrl != null) {
                            android.util.Log.d("Planner", "AniList found URL for '$term': $coverUrl")
                            break@searchLoop
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Planner", "AniList error for $term: ${e.message}")
                    }
                }
            }

            if (coverUrl != null) {
                showToast("Sampul ditemukan!")
            } else {
                showToast("Tidak dapat menemukan sampul otomatis untuk '$title'")
            }

            val item = DonghuaItem(
                title = title,
                totalEpisodes = total,
                currentEpisode = current,
                status = status,
                rating = rating,
                isFavorite = isFav,
                coverUrl = coverUrl,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertDonghua(item)
            showToast("Donghua ditambahkan")
        }
    }

    fun editDonghua(item: DonghuaItem, title: String, total: Int, current: Int, status: String, rating: Int, isFav: Boolean, manualCoverUrl: String = "") {
        viewModelScope.launch {
            if (title.isBlank()) {
                showToast("Judul wajib diisi")
                return@launch
            }
            if (current > total && total > 0) {
                showToast("Episode saat ini tidak boleh melebihi total")
                return@launch
            }

            var coverUrl = if (manualCoverUrl.isNotBlank()) manualCoverUrl else item.coverUrl
            if (manualCoverUrl.isBlank() && (coverUrl == null || title != item.title)) {
                val searchTerms = mutableListOf<String>()
                searchTerms.add(title.trim())
                val cleanTitle = title.trim().split("(")[0].split("[")[0].trim()
                if (cleanTitle != title.trim()) searchTerms.add(cleanTitle)
                if (title.contains("Soul Land", ignoreCase = true) && !title.contains("Douluo Dalu", ignoreCase = true)) searchTerms.add("Douluo Dalu")
                if (!title.contains("Donghua", ignoreCase = true)) searchTerms.add("$cleanTitle Donghua")

                val uniqueTerms = searchTerms.distinct()
                searchLoop@for (term in uniqueTerms) {
                    if (term.isBlank()) continue
                    
                    try {
                        val response = com.example.network.JikanClient.api.searchAnime(term)
                        if (response.data.isNotEmpty()) {
                            coverUrl = response.data.first().images.jpg?.image_url
                            if (coverUrl != null) break@searchLoop
                        }
                    } catch (e: Exception) {}

                    try {
                        val aniListQuery = """
                            query (${'$'}s: String) {
                              Media(search: ${'$'}s, type: ANIME) {
                                coverImage {
                                  large
                                }
                              }
                            }
                        """.trimIndent()
                        val request = com.example.network.AniListRequest(aniListQuery, mapOf("s" to term))
                        val response = com.example.network.AniListClient.api.searchAnime(request)
                        coverUrl = response.data.Media?.coverImage?.large
                        if (coverUrl != null) break@searchLoop
                    } catch (e: Exception) {}
                }
                
                if (coverUrl != item.coverUrl && coverUrl != null) {
                    showToast("Sampul diupdate!")
                }
            }

            repository.insertDonghua(item.copy(
                title = title,
                totalEpisodes = total,
                currentEpisode = current,
                status = status,
                rating = rating,
                isFavorite = isFav,
                coverUrl = coverUrl,
                updatedAt = System.currentTimeMillis()
            ))
            showToast("Donghua diupdate")
        }
    }

    fun incrementDonghua(item: DonghuaItem) {
        viewModelScope.launch {
            if (item.currentEpisode < item.totalEpisodes) {
                val nextEp = item.currentEpisode + 1
                val newStatus = if (nextEp >= item.totalEpisodes) "finished" else item.status
                repository.insertDonghua(item.copy(
                    currentEpisode = nextEp,
                    status = newStatus,
                    updatedAt = System.currentTimeMillis()
                ))
                if (nextEp >= item.totalEpisodes) {
                    showToast("Donghua selesai! 🎉")
                } else {
                    showToast("Episode ditambah ke $nextEp")
                }
            } else {
                showToast("Sudah mencapai episode terakhir")
            }
        }
    }

    fun toggleDonghuaFavorite(item: DonghuaItem) {
        viewModelScope.launch {
            repository.insertDonghua(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun deleteDonghua(item: DonghuaItem) {
        viewModelScope.launch {
            repository.deleteDonghua(item)
            showToast("Donghua dihapus")
        }
    }

    fun refreshDonghuaCover(item: DonghuaItem) {
        viewModelScope.launch {
            showToast("Mencari gambar sampul untuk: ${item.title}...")
            var coverUrl: String? = null
            val searchTerms = mutableListOf<String>()
            searchTerms.add(item.title.trim())
            val cleanTitle = item.title.trim().split("(")[0].split("[")[0].trim()
            if (cleanTitle != item.title.trim()) searchTerms.add(cleanTitle)
            if (item.title.contains("Soul Land", ignoreCase = true) && !item.title.contains("Douluo Dalu", ignoreCase = true)) searchTerms.add("Douluo Dalu")
            if (!item.title.contains("Donghua", ignoreCase = true)) searchTerms.add("$cleanTitle Donghua")

            val uniqueTerms = searchTerms.distinct()
            searchLoop@for (term in uniqueTerms) {
                if (term.isBlank()) continue
                
                try {
                    val response = com.example.network.JikanClient.api.searchAnime(term)
                    if (response.data.isNotEmpty()) {
                        coverUrl = response.data.first().images.jpg?.image_url
                        if (coverUrl != null) break@searchLoop
                    }
                } catch (e: Exception) {}

                try {
                    val aniListQuery = """
                        query (${'$'}s: String) {
                          Media(search: ${'$'}s, type: ANIME) {
                            coverImage {
                              large
                            }
                          }
                        }
                    """.trimIndent()
                    val request = com.example.network.AniListRequest(aniListQuery, mapOf("s" to term))
                    val response = com.example.network.AniListClient.api.searchAnime(request)
                    coverUrl = response.data.Media?.coverImage?.large
                    if (coverUrl != null) break@searchLoop
                } catch (e: Exception) {}
            }

            if (coverUrl != null) {
                repository.insertDonghua(item.copy(coverUrl = coverUrl, updatedAt = System.currentTimeMillis()))
                showToast("Gambar sampul ditemukan!")
            } else {
                showToast("Gambar tidak ditemukan untuk: ${item.title}")
            }
        }
    }

    fun clearDonghua() {
        viewModelScope.launch {
            repository.clearDonghua()
            showToast("Semua donghua dibersihkan")
        }
    }

    // Budget Tracker
    fun addTransaction(desc: String, amount: Double, type: String) {
        viewModelScope.launch {
            if (desc.isBlank() || amount <= 0) {
                showToast("Isi keterangan dan nominal transaksi")
                return@launch
            }
            if (amount > 200_000_000.0) {
                showToast("Maksimal transaksi Rp 200.000.000")
                return@launch
            }
            val transaction = BudgetTransaction(
                description = desc,
                amount = amount,
                type = type
            )
            repository.insertTransaction(transaction)
            showToast("Transaksi ditambahkan")
        }
    }

    fun deleteTransaction(transaction: BudgetTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            showToast("Transaksi dihapus")
        }
    }

    fun clearTransactions() {
        viewModelScope.launch {
            repository.clearTransactions()
            showToast("Semua transaksi dibersihkan")
        }
    }

    // Reset everything
    fun resetAllData() {
        viewModelScope.launch {
            repository.clearSchedules()
            repository.clearTimeSlots()
            repository.clearTasks()
            repository.clearWorkoutLogs()
            repository.clearSavingGoals()
            repository.clearDonghua()
            repository.clearTransactions()
            
            // Reinsert default time slots
            repository.insertTimeSlot(TimeSlot(startTime = "07:00", endTime = "08:00"))
            repository.insertTimeSlot(TimeSlot(startTime = "08:00", endTime = "09:00"))
            repository.insertTimeSlot(TimeSlot(startTime = "09:00", endTime = "10:00"))

            showToast("Semua data berhasil di-reset!")
        }
    }
}

class PlannerViewModelFactory(
    private val repository: PlannerRepository,
    private val prefManager: PreferenceManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(repository, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
