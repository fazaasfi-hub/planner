package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

// Helper for currency formatting
val rpFormat = DecimalFormat("#,###")
fun formatRupiah(amount: Long): String {
    return "Rp " + rpFormat.format(amount).replace(",", ".")
}

// Map for Sport MET calculations
val SPORT_MET = mapOf(
    "Lari (8 km/jam)" to 8.3,
    "Lari (10 km/jam)" to 9.8,
    "Lari (12 km/jam)" to 11.5,
    "Jogging (6 km/jam)" to 6.0,
    "Jalan cepat (5 km/jam)" to 5.0,
    "Jalan santai (3 km/jam)" to 3.5,
    "Sepeda santai (16 km/jam)" to 5.5,
    "Sepeda sedang (20 km/jam)" to 7.5,
    "Sepeda statis (100 watt)" to 8.5,
    "Renang santai" to 6.0,
    "Renang sedang" to 8.0,
    "Yoga ringan" to 2.5,
    "Yoga power" to 4.0,
    "Pilates" to 3.5,
    "Zumba" to 6.5,
    "Bodyweight (Push-up/Squat)" to 5.5,
    "Angkat beban ringan" to 3.5,
    "Angkat beban sedang" to 5.0,
    "Angkat beban berat" to 6.5,
    "Circuit training" to 8.0,
    "Bola basket" to 8.0,
    "Sepak bola" to 7.5,
    "Badminton" to 6.0,
    "Tenis meja" to 4.5,
    "Lompat tali" to 12.0,
    "Mendaki gunung" to 6.0,
    "CrossFit" to 9.0
)

val WEIGHT_SPORTS = listOf("Angkat beban ringan", "Angkat beban sedang", "Angkat beban berat", "Circuit training", "CrossFit")

val DAYS_OF_WEEK = listOf("SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU", "MINGGU")

// GLASSMORPHISM UI COMPONENTS
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderAlpha: Float = 0.15f,
    containerColor: Color? = null,
    borderStroke: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface == SlateSurfaceDark || MaterialTheme.colorScheme.background == SlateBackgroundDark
    val baseColor = containerColor ?: if (isDark) {
        Color(0xFF1E293B).copy(alpha = 0.65f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.85f)
    }

    val finalBorder = borderStroke ?: BorderStroke(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.35f),
                accentColor.copy(alpha = borderAlpha)
            )
        )
    )

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = finalBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun ClickableGlassyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    accentColor: Color = MaterialTheme.colorScheme.primary,
    borderAlpha: Float = 0.15f,
    containerColor: Color? = null,
    borderStroke: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface == SlateSurfaceDark || MaterialTheme.colorScheme.background == SlateBackgroundDark
    val baseColor = containerColor ?: if (isDark) {
        Color(0xFF1E293B).copy(alpha = 0.65f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.85f)
    }

    val finalBorder = borderStroke ?: BorderStroke(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.35f),
                accentColor.copy(alpha = borderAlpha)
            )
        )
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = finalBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

fun Modifier.glassBackground(
    isDarkTheme: Boolean,
    primaryColor: Color,
    secondaryColor: Color
): Modifier = this.drawBehind {
    val baseColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    drawRect(color = baseColor)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = if (isDarkTheme) 0.18f else 0.15f),
                Color.Transparent
            ),
            center = Offset(0f, 0f),
            radius = size.width * 0.85f
        ),
        radius = size.width * 0.85f,
        center = Offset(0f, 0f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                secondaryColor.copy(alpha = if (isDarkTheme) 0.15f else 0.12f),
                Color.Transparent
            ),
            center = Offset(size.width, size.height),
            radius = size.width * 0.9f
        ),
        radius = size.width * 0.9f,
        center = Offset(size.width, size.height)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = if (isDarkTheme) 0.08f else 0.06f),
                Color.Transparent
            ),
            center = Offset(0f, size.height * 0.5f),
            radius = size.width * 0.6f
        ),
        radius = size.width * 0.6f,
        center = Offset(0f, size.height * 0.5f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerApp(viewModel: PlannerViewModel, isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe Toast from ViewModel
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { msg: String ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // Header with elegant Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "App Logo",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Planner Pro",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Productivity Hub",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drawer Navigation Items
                val menuItems = listOf(
                    Triple(Screen.Dashboard, "Dashboard", Icons.Default.Home),
                    Triple(Screen.Study, "Belajar & Tugas", Icons.Default.List),
                    Triple(Screen.Workout, "Olahraga", Icons.Default.PlayArrow),
                    Triple(Screen.Saving, "Nabung", Icons.Default.Star),
                    Triple(Screen.Donghua, "Donghua Tracker", Icons.Default.PlayArrow),
                    Triple(Screen.Chart, "Grafik Statistik", Icons.Default.Info),
                    Triple(Screen.Budget, "Budget Tracker", Icons.Default.List)
                )

                LazyColumn(modifier = Modifier.padding(horizontal = 12.dp)) {
                    items(menuItems) { (screen, label, icon) ->
                        NavigationDrawerItem(
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontWeight = FontWeight.SemiBold) },
                            selected = currentScreen == screen,
                            onClick = {
                                viewModel.navigateTo(screen)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Theme Toggle and App Settings in Footer
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode Gelap",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.Refresh else Icons.Default.Settings,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.Dashboard -> "Dashboard"
                                Screen.Study -> "Belajar & Tugas"
                                Screen.Workout -> "Olahraga"
                                Screen.Saving -> "Nabung"
                                Screen.Donghua -> "Donghua Tracker"
                                Screen.Chart -> "Grafik"
                                Screen.Budget -> "Budget Tracker"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Buka Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onThemeToggle) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.Refresh else Icons.Default.Settings,
                                contentDescription = "Ubah Tema"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.glassBackground(
                isDarkTheme = isDarkTheme,
                primaryColor = MaterialTheme.colorScheme.primary,
                secondaryColor = MaterialTheme.colorScheme.secondary
            )
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Premium physics-based sliding, scaling, and fading transitions for screen navigation
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val duration = 280
                        (slideInHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)) { width -> width / 4 } +
                         fadeIn(animationSpec = tween(duration, easing = LinearOutSlowInEasing)) +
                         scaleIn(initialScale = 0.96f, animationSpec = tween(duration, easing = LinearOutSlowInEasing))) togetherWith
                        (slideOutHorizontally(animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)) { width -> -width / 4 } +
                         fadeOut(animationSpec = tween(duration, easing = FastOutLinearInEasing)) +
                         scaleOut(targetScale = 0.96f, animationSpec = tween(duration, easing = FastOutLinearInEasing)))
                    },
                    label = "ScreenTransition"
                ) { target ->
                    when (target) {
                        Screen.Dashboard -> DashboardScreen(viewModel)
                        Screen.Study -> StudyScreen(viewModel)
                        Screen.Workout -> WorkoutScreen(viewModel)
                        Screen.Saving -> SavingScreen(viewModel)
                        Screen.Donghua -> DonghuaScreen(viewModel)
                        Screen.Chart -> ChartScreen(viewModel)
                        Screen.Budget -> BudgetScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ============================================================
// DASHBOARD SCREEN
// ============================================================
@Composable
fun DashboardScreen(viewModel: PlannerViewModel) {
    val scrollState = rememberScrollState()

    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val donghuaItems by viewModel.donghuaItems.collectAsStateWithLifecycle()
    val savingGoals by viewModel.savingGoals.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val weatherInfo by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val weatherLoading by viewModel.weatherLoading.collectAsStateWithLifecycle()
    var cityQuery by remember { mutableStateOf("") }

    // 20 NEW FEATURES STATES & COLLECTORS
    val waterIntake by viewModel.waterIntake.collectAsStateWithLifecycle()
    val lastBackupTime by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val profileName by viewModel.profileName.collectAsStateWithLifecycle()
    val profileBio by viewModel.profileBio.collectAsStateWithLifecycle()
    val avatarIndex by viewModel.avatarIndex.collectAsStateWithLifecycle()
    val scratchpad by viewModel.scratchpad.collectAsStateWithLifecycle()
    val themeAccent by viewModel.themeAccent.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Statistics calculations
    val totalSubjects = remember(schedules) { schedules.map { it.subjectName }.distinct().size }
    val totalTasks = remember(tasks) { tasks.size }
    val doneTasks = remember(tasks) { tasks.count { it.isDone } }
    val tasksPct = if (totalTasks > 0) (doneTasks * 100) / totalTasks else 0

    val totalWorkout = remember(workoutLogs) { workoutLogs.size }
    val totalCalories = remember(workoutLogs) { workoutLogs.sumOf { it.caloriesBurned } }

    val totalDonghua = remember(donghuaItems) { donghuaItems.size }
    val watchingDonghua = remember(donghuaItems) { donghuaItems.count { it.status == "watching" } }
    val finishedDonghua = remember(donghuaItems) { donghuaItems.count { it.status == "finished" } }

    val totalSavingsAmount = remember(savingGoals) { savingGoals.sumOf { it.currentAmount } }
    val targetSavingsAmount = remember(savingGoals) { savingGoals.sumOf { it.targetAmount } }
    val savingsPct = if (targetSavingsAmount > 0) (totalSavingsAmount * 100) / targetSavingsAmount else 0

    val totalNotes = remember(notes) { notes.size }

    // Calculate total study hours (difference in minutes)
    val studyHours = remember(schedules) {
        var minutes = 0
        schedules.forEach { s ->
            try {
                val startParts = s.startTime.split(":").map { it.toInt() }
                val endParts = s.endTime.split(":").map { it.toInt() }
                val startMin = startParts[0] * 60 + startParts[1]
                val endMin = endParts[0] * 60 + endParts[1]
                if (endMin > startMin) minutes += (endMin - startMin)
            } catch (e: Exception) { /* ignore */ }
        }
        Math.round((minutes / 60.0) * 10) / 10.0
    }

    // Dynamic greeting and date
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 4..10 -> "Selamat Pagi \uD83C\uDF05"
        hour in 11..14 -> "Selamat Siang ☀️"
        hour in 15..18 -> "Selamat Sore \uD83C\uDF07"
        else -> "Selamat Malam \uD83C\uDF19"
    }
    val dateFormatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    val todayDate = dateFormatter.format(calendar.time)

    val quotesList = remember {
        listOf(
            "Satu langkah kecil tetap lebih baik daripada tidak sama sekali.",
            "Kesuksesan adalah akumulasi dari usaha kecil yang dilakukan setiap hari.",
            "Jangan bandingkan dirimu dengan orang lain, bandingkan dengan dirimu kemarin.",
            "Disiplin adalah jembatan antara tujuan dan pencapaian.",
            "Lakukan sekarang juga, besok mungkin terlambat.",
            "Fokus pada proses, hasil akan mengikuti dengan indah.",
            "Keberhasilan tidak mengkhianati usaha keras yang konsisten."
        )
    }
    var currentQuoteIndex by remember { mutableStateOf(0) }
    val quote = quotesList[currentQuoteIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // GREETING BANNER WITH PROFILE CUSTOMIZATION
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            accentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar circle
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val avatarEmoji = when(avatarIndex) {
                                1 -> "🧠" // Scholar
                                2 -> "🏃‍♂️" // Athlete
                                3 -> "💰" // Financial Guru
                                4 -> "🐉" // Otaku
                                else -> "💻" // Programmer
                            }
                            Text(text = avatarEmoji, fontSize = 32.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$greeting, $profileName!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Edit Profil",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = profileBio,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Kalender", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = todayDate,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // EDIT PROFILE DIALOG
        if (showEditProfileDialog) {
            var tempName by remember { mutableStateOf(profileName) }
            var tempBio by remember { mutableStateOf(profileBio) }
            var tempAvatar by remember { mutableStateOf(avatarIndex) }

            Dialog(onDismissRequest = { showEditProfileDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Edit Profil Pengguna", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Nama Pengguna") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = tempBio,
                            onValueChange = { tempBio = it },
                            label = { Text("Bio / Slogan") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(text = "Pilih Avatar Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                0 to "💻", // Programmer
                                1 to "🧠", // Scholar
                                2 to "🏃‍♂️", // Athlete
                                3 to "💰", // Finance
                                4 to "🐉"  // Otaku
                            ).forEach { (idx, emoji) ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (tempAvatar == idx) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { tempAvatar = idx }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (tempAvatar == idx) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditProfileDialog = false }) {
                                Text("Batal")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateProfile(tempName, tempBio, tempAvatar)
                                    showEditProfileDialog = false
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Simpan")
                            }
                        }
                    }
                }
            }
        }

        // QUICK ACTIONS
        Text(text = "Akses Cepat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { viewModel.navigateTo(Screen.Study) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tugas")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tugas Baru")
            }
            Button(onClick = { viewModel.navigateTo(Screen.Workout) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Olahraga")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Olahraga")
            }
            Button(onClick = { viewModel.navigateTo(Screen.Donghua) }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Donghua")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Donghua")
            }
            Button(onClick = { viewModel.navigateTo(Screen.Saving) }, colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)) {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Nabung")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tabungan")
            }
        }

        // WEATHER CARD
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Weather Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    OutlinedTextField(
                        value = cityQuery,
                        onValueChange = { cityQuery = it },
                        placeholder = { Text("Masukkan kota...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Button(
                        onClick = { viewModel.searchWeather(cityQuery) },
                        enabled = !weatherLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (weatherLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Cari")
                        }
                    }
                }

                weatherInfo?.let { info ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${info.cityName}, ${info.country}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = when (info.weatherCode) {
                                    0 -> "Cerah ☀️"
                                    1 -> "Cerah Berawan 🌤️"
                                    2 -> "Berawan ⛅"
                                    3 -> "Berawan Tebal ☁️"
                                    in 45..48 -> "Kabut 🌫️"
                                    in 51..55 -> "Gerimis 🌧️"
                                    in 61..65 -> "Hujan 🌧️"
                                    in 71..77 -> "Salju ❄️"
                                    in 80..82 -> "Hujan Lebat ⛈️"
                                    else -> "Badai ⚡"
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${info.temperature}°C",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "💧 ${info.humidity}%", fontSize = 11.sp)
                                Text(text = "💨 ${info.windspeed} km/j", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // STATS SUMMARY CARDS
        Text(text = "Rangkuman Aktivitas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardPremium(
                    title = "Mata Pelajaran",
                    value = "$totalSubjects",
                    subtitle = "$studyHours jam/minggu",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f),
                    badgeBg = MaterialTheme.colorScheme.primary
                )
                StatCardPremium(
                    title = "Total Tugas",
                    value = "$totalTasks",
                    subtitle = "$doneTasks selesai ($tasksPct%)",
                    icon = Icons.Default.List,
                    modifier = Modifier.weight(1f),
                    badgeBg = MaterialTheme.colorScheme.secondary
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardPremium(
                    title = "Olahraga",
                    value = "$totalWorkout",
                    subtitle = "${totalCalories} kalori",
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    badgeBg = SuccessGreen
                )
                StatCardPremium(
                    title = "Donghua",
                    value = "$totalDonghua",
                    subtitle = "$watchingDonghua tonton · $finishedDonghua selesai",
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    badgeBg = WarningAmber
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCardPremium(
                    title = "Tabungan",
                    value = formatRupiah(totalSavingsAmount),
                    subtitle = "$savingsPct% dari target",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f),
                    badgeBg = IndigoTertiary
                )
                StatCardPremium(
                    title = "Catatan",
                    value = "$totalNotes",
                    subtitle = "Ditulis sejauh ini",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f),
                    badgeBg = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        // PROGRESS TARGET CARDS
        Text(text = "Target Minggu Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TargetBar(label = "Belajar", current = "$studyHours jam", target = "10 jam", pct = (studyHours / 10.0).toFloat(), color = MaterialTheme.colorScheme.primary)
                TargetBar(label = "Kalori Dibakar", current = "$totalCalories kal", target = "2.000 kal", pct = (totalCalories / 2000f), color = SuccessGreen)
                TargetBar(label = "Tabungan", current = formatRupiah(totalSavingsAmount), target = formatRupiah(targetSavingsAmount), pct = (totalSavingsAmount.toFloat() / Math.max(1f, targetSavingsAmount.toFloat())), color = WarningAmber)
            }
        }

        // WATER INTAKE TRACKER CARD (FEATURE 1)
        Text(text = "Kebutuhan Cairan Harian (Pelacak Air Minum)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🥤", fontSize = 24.sp)
                        Column {
                            Text(text = "Hidrasi Tubuh Anda", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Target: 8 Gelas / Hari (${waterIntake * 250}ml / 2000ml)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Text(
                        text = if (waterIntake >= 8) "Terpenuhi! 🎉" else "${waterIntake}/8 Gelas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (waterIntake >= 8) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                }

                // Smooth linear progress indicator
                LinearProgressIndicator(
                    progress = { Math.min(1.0f, waterIntake.toFloat() / 8.0f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.subtractWaterGlass() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = waterIntake > 0
                    ) {
                        Text("- Kurangi Air", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.addWaterGlass() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+ Minum Air", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // SCRATCHPAD QUICK NOTES (FEATURE 19)
        Text(text = "Scratchpad / Catatan Cepat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tulis coretan pikiran atau daftar belanja sementara di sini. Tersimpan otomatis dalam sesi.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = scratchpad,
                    onValueChange = { viewModel.updateScratchpad(it) },
                    placeholder = { Text("Mulai menulis catatan cepat...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }

        // DATABASE LOCAL BACKUP (FEATURE 16)
        Text(text = "Keamanan Data & Ekspor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Pencadangan Database Room", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Status cadangan: $lastBackupTime", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { viewModel.triggerLocalBackup() },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Backup", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cadangkan", fontSize = 11.sp)
                }
            }
        }

        // QUOTES CARD (FEATURE 18)
        Text(text = "Motivasi Hari Ini", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        ClickableGlassyCard(
            onClick = {
                currentQuoteIndex = (currentQuoteIndex + 1) % quotesList.size
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            accentColor = MaterialTheme.colorScheme.secondary
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Quote",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Klik kartu untuk memutar motivasi lain",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "“$quote”",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // THEME ACCENT CHIPS SELECTOR (FEATURE 15)
        Text(text = "Pilihan Aksen Warna Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ubah aksen warna primer dan sekunder aplikasi secara instan sesuai kenyamanan Anda.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Indigo" to Color(0xFF4F46E5),
                        "Emerald" to Color(0xFF059669),
                        "Amber" to Color(0xFFD97706),
                        "Teal" to Color(0xFF0D9488)
                    ).forEach { (name, dotColor) ->
                        val isSelected = themeAccent == name
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) dotColor.copy(alpha = 0.12f) else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setThemeAccent(name) }
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) dotColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(dotColor, shape = CircleShape)
                                )
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) dotColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // RESET ALL APP DATA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pembersihan Data", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DangerRed)
                    Text("Hapus semua jadwal, tugas, catatan, dan riwayat.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Button(
                    onClick = { viewModel.resetAllData() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCardPremium(title: String, value: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, badgeBg: Color) {
    GlassyCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun TargetBar(label: String, current: String, target: String, pct: Float, color: Color) {
    val progress = pct.coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "$current / $target", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

// ============================================================
// STUDY SCREEN
// ============================================================
@Composable
fun StudyScreen(viewModel: PlannerViewModel) {
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val timeSlots by viewModel.timeSlots.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var showAddSlotDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Dialog state variables
    var slotStart by remember { mutableStateOf("07:00") }
    var slotEnd by remember { mutableStateOf("08:00") }

    var schedDay by remember { mutableStateOf("SENIN") }
    var schedStart by remember { mutableStateOf("07:00") }
    var schedEnd by remember { mutableStateOf("08:00") }
    var schedSubject by remember { mutableStateOf("") }
    var schedColorHex by remember { mutableStateOf("#4F46E5") }

    var taskText by remember { mutableStateOf("") }
    var taskSubject by remember { mutableStateOf("") }
    var taskDeadline by remember { mutableStateOf("") }
    var taskFilter by remember { mutableStateOf("all") }
    var taskSortBy by remember { mutableStateOf("newest") } // newest, deadline, alpha

    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteTags by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Pomodoro Timer States from ViewModel
    val pomodoroTimeLeft by viewModel.pomodoroTimeLeft.collectAsStateWithLifecycle()
    val pomodoroRunning by viewModel.pomodoroRunning.collectAsStateWithLifecycle()
    val pomodoroCompleted by viewModel.pomodoroCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(pomodoroRunning, pomodoroTimeLeft) {
        if (pomodoroRunning && pomodoroTimeLeft > 0) {
            kotlinx.coroutines.delay(1000)
            viewModel.updatePomodoroTime(pomodoroTimeLeft - 1)
            if (pomodoroTimeLeft - 1 == 0) {
                viewModel.togglePomodoro()
                viewModel.incrementPomodoroCount()
                viewModel.showToast("Sesi Pomodoro Selesai! Saatnya istirahat \uD83C\uDF89")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // POMODORO FOCUS TIMER CARD (FEATURE 8)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏱️", fontSize = 22.sp)
                        Column {
                            Text(text = "Fokus Pomodoro", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Selesaikan tugas dengan fokus tinggi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "SESI Selesai: $pomodoroCompleted",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Digital Timer Clock Visuals
                val mins = pomodoroTimeLeft / 60
                val secs = pomodoroTimeLeft % 60
                val timeStr = String.format("%02d:%02d", mins, secs)

                Text(
                    text = timeStr,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )

                // Quick sessions selectors
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "Fokus (25m)" to 1500,
                        "Istirahat (5m)" to 300,
                        "Istirahat Lama (15m)" to 900
                    ).forEach { (label, seconds) ->
                        val isCurrent = pomodoroTimeLeft == seconds
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!pomodoroRunning) {
                                        viewModel.updatePomodoroTime(seconds)
                                    } else {
                                        viewModel.showToast("Hentikan timer dulu untuk ganti sesi!")
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.togglePomodoro() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pomodoroRunning) DangerRed else MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = if (pomodoroRunning) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                            contentDescription = if (pomodoroRunning) "Pause" else "Mulai"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (pomodoroRunning) "Pause" else "Mulai Fokus", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            if (pomodoroRunning) viewModel.togglePomodoro()
                            viewModel.updatePomodoroTime(1500) // reset to 25m
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Timer")
                    }
                }
            }
        }

        // STUDY SCHEDULE TABLE CARD
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📅", fontSize = 20.sp)
                        Text(text = "Jadwal Pelajaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { showAddSlotDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Slot Waktu")
                        }
                        IconButton(onClick = { showAddScheduleDialog = true }) {
                            Icon(imageVector = Icons.Default.List, contentDescription = "Tambah Pelajaran")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule grid representation
                if (timeSlots.isEmpty()) {
                    Text(
                        text = "Belum ada slot waktu.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    val days = remember { DAYS_OF_WEEK.take(5) }
                    val timeColWidth = 90.dp
                    val dayColWidth = 115.dp
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                    ) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // First Column Header: Jam
                            Box(
                                modifier = Modifier.width(timeColWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Jam",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Day Column Headers
                            days.forEach { day ->
                                Box(
                                    modifier = Modifier.width(dayColWidth),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Table Data Rows
                        val sortedSlots = remember(timeSlots) {
                            timeSlots.sortedBy { it.startTime }
                        }

                        sortedSlots.forEachIndexed { rowIndex, slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time Slot Column
                                Column(
                                    modifier = Modifier.width(timeColWidth),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${slot.startTime}\n-\n${slot.endTime}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.primary,
                                        lineHeight = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteTimeSlot(slot) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Slot",
                                            tint = DangerRed.copy(alpha = 0.7f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }

                                // Day Columns
                                days.forEach { day ->
                                    val match = schedules.find {
                                        it.day == day && it.startTime < slot.endTime && it.endTime > slot.startTime
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(dayColWidth)
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (match != null) {
                                            val colorHex = match.colorHex
                                            val itemColor = remember(colorHex) {
                                                try {
                                                    Color(android.graphics.Color.parseColor(colorHex))
                                                } catch (e: Exception) {
                                                    Color(0xFF6200EE)
                                                }
                                            }

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(68.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = itemColor.copy(alpha = 0.12f)),
                                                border = BorderStroke(1.dp, itemColor.copy(alpha = 0.4f))
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    IconButton(
                                                        onClick = { viewModel.deleteSchedule(match) },
                                                        modifier = Modifier
                                                            .align(Alignment.End)
                                                            .size(14.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Hapus",
                                                            tint = DangerRed,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }

                                                    Text(
                                                        text = match.subjectName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center,
                                                        color = itemColor,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier
                                                            .padding(bottom = 6.dp)
                                                            .align(Alignment.CenterHorizontally)
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                }
                                            }
                                        } else {
                                            // Empty cell
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(68.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "-",
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }

        // TASKS CHECKLIST CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Tugas / PR", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // Input form
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("Deskripsi Tugas...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = taskSubject,
                        onValueChange = { taskSubject = it },
                        label = { Text("Mata Pelajaran") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = taskDeadline,
                        onValueChange = { taskDeadline = it },
                        label = { Text("Deadline (e.g. 2026-06-30)") },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.addTask(taskText, taskSubject, taskDeadline)
                        taskText = ""
                        taskSubject = ""
                        taskDeadline = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Tugas")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Tugas")
                }

                Divider()

                // Filter Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("all" to "Semua", "pending" to "Belum", "done" to "Selesai").forEach { (filterVal, label) ->
                        FilterChip(
                            selected = taskFilter == filterVal,
                            onClick = { taskFilter = filterVal },
                            label = { Text(filterVal.uppercase() + " (" + (if (filterVal == "pending") tasks.count { !it.isDone } else if (filterVal == "done") tasks.count { it.isDone } else tasks.size) + ")", fontSize = 11.sp) }
                        )
                    }
                }

                // Sorting Buttons (FEATURE 7)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Urutan:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    listOf("newest" to "Terbaru", "deadline" to "Tenggat", "alpha" to "Nama A-Z").forEach { (sortVal, label) ->
                        FilterChip(
                            selected = taskSortBy == sortVal,
                            onClick = { taskSortBy = sortVal },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                // Tasks List
                val filteredTasks = remember(tasks, taskFilter, taskSortBy) {
                    val base = when (taskFilter) {
                        "pending" -> tasks.filter { !it.isDone }
                        "done" -> tasks.filter { it.isDone }
                        else -> tasks
                    }
                    when (taskSortBy) {
                        "deadline" -> base.sortedBy { if (it.deadline.isBlank()) "9999-99-99" else it.deadline }
                        "alpha" -> base.sortedBy { it.text.lowercase() }
                        else -> base.sortedByDescending { it.createdAt }
                    }
                }

                if (filteredTasks.isEmpty()) {
                    Text(
                        text = "Tidak ada tugas",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(
                        modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isDone,
                                    onCheckedChange = { viewModel.toggleTask(task) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.text,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                                        color = if (task.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (task.subject.isNotBlank()) {
                                            Text(text = "📚 ${task.subject}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                        if (task.deadline.isNotBlank()) {
                                            Text(text = "📅 ${task.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteTask(task) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Tugas", tint = DangerRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // STUDY NOTES CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Catatan Belajar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { showAddNoteDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Catatan")
                    }
                }

                if (notes.isEmpty()) {
                    Text(
                        text = "Belum ada catatan",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        notes.forEach { note ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(onClick = { viewModel.deleteNote(note) }, modifier = Modifier.size(24.dp)) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = note.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    if (note.tags.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            note.tags.split(",").forEach { tag ->
                                                if (tag.trim().isNotBlank()) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(text = "#${tag.trim()}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD TIME SLOT
    if (showAddSlotDialog) {
        Dialog(onDismissRequest = { showAddSlotDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Slot Waktu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = slotStart, onValueChange = { slotStart = it }, label = { Text("Jam Mulai (HH:MM)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = slotEnd, onValueChange = { slotEnd = it }, label = { Text("Jam Selesai (HH:MM)") }, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddSlotDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            viewModel.addTimeSlot(slotStart, slotEnd)
                            showAddSlotDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }

    // DIALOG: ADD SCHEDULE LESSON
    if (showAddScheduleDialog) {
        Dialog(onDismissRequest = { showAddScheduleDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Jadwal Pelajaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = schedSubject, onValueChange = { schedSubject = it }, label = { Text("Nama Pelajaran") }, modifier = Modifier.fillMaxWidth())

                    // Day picker dropdown (basic Column layout)
                    Text("Hari", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DAYS_OF_WEEK.take(5).forEach { day ->
                            FilterChip(
                                selected = schedDay == day,
                                onClick = { schedDay = day },
                                label = { Text(day, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(value = schedStart, onValueChange = { schedStart = it }, label = { Text("Mulai") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = schedEnd, onValueChange = { schedEnd = it }, label = { Text("Selesai") }, modifier = Modifier.weight(1f))
                    }

                    // Color selection list
                    Text("Pilih Warna", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    val colors = listOf("#4F46E5", "#3B82F6", "#10B981", "#EF4444", "#F59E0B", "#8B5CF6")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colors.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(col)))
                                    .clickable { schedColorHex = col }
                                    .border(if (schedColorHex == col) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else BorderStroke(0.dp, Color.Transparent), CircleShape)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddScheduleDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            viewModel.addSchedule(schedDay, schedStart, schedEnd, schedSubject, schedColorHex)
                            schedSubject = ""
                            showAddScheduleDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }

    // DIALOG: ADD NOTE
    if (showAddNoteDialog) {
        Dialog(onDismissRequest = { showAddNoteDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Catatan Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Judul Catatan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Isi Catatan") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = noteTags, onValueChange = { noteTags = it }, label = { Text("Tag (pisahkan dengan koma)") }, modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddNoteDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            viewModel.addNote(noteTitle, noteContent, noteTags)
                            noteTitle = ""
                            noteContent = ""
                            noteTags = ""
                            showAddNoteDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// ============================================================
// WORKOUT SCREEN
// ============================================================
@Composable
fun WorkoutScreen(viewModel: PlannerViewModel) {
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val stepLogs by viewModel.stepLogs.collectAsStateWithLifecycle()
    val todaySteps by viewModel.todaySteps.collectAsStateWithLifecycle()
    val isTrackingSteps by viewModel.isTrackingSteps.collectAsStateWithLifecycle()
    val todayStepGoal by viewModel.todayStepGoal.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var hasStepPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStepPermission = isGranted
    }

    // Step Detector / Counter sensor registration (Auto Track)
    DisposableEffect(hasStepPermission) {
        if (hasStepPermission) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

            var initialSteps = -1

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                        viewModel.incrementActiveSteps(1)
                    } else if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                        val sensorSteps = event.values[0].toInt()
                        if (initialSteps < 0) {
                            initialSteps = sensorSteps
                        } else {
                            val diff = sensorSteps - initialSteps
                            if (diff > 0) {
                                viewModel.incrementActiveSteps(diff)
                                initialSteps = sensorSteps
                            }
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (stepDetector != null) {
                sensorManager.registerListener(listener, stepDetector, SensorManager.SENSOR_DELAY_UI)
            } else if (stepCounter != null) {
                sensorManager.registerListener(listener, stepCounter, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        } else {
            onDispose {}
        }
    }

    var showAddWorkoutDialog by remember { mutableStateOf(false) }

    var logName by remember { mutableStateOf("") }
    var logSportType by remember { mutableStateOf("Lari (8 km/jam)") }
    var logDuration by remember { mutableStateOf("") }
    var logWeight by remember { mutableStateOf("60") }
    var logExerciseName by remember { mutableStateOf("") }
    var logSets by remember { mutableStateOf("") }
    var logReps by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Calculated stats
    val totalSessions = remember(workoutLogs) { workoutLogs.size }
    val totalDuration = remember(workoutLogs) { workoutLogs.sumOf { it.durationMinutes } }
    val totalCalories = remember(workoutLogs) { workoutLogs.sumOf { it.caloriesBurned } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SUMMARY CARDS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCardPremium(
                title = "Total Sesi",
                value = "$totalSessions",
                subtitle = "Sesi Olahraga",
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f),
                badgeBg = MaterialTheme.colorScheme.primary
            )
            StatCardPremium(
                title = "Total Durasi",
                value = "$totalDuration mnt",
                subtitle = "Waktu Aktif",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                badgeBg = MaterialTheme.colorScheme.secondary
            )
            StatCardPremium(
                title = "Total Kalori",
                value = "$totalCalories",
                subtitle = "Kalori Dibakar",
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f),
                badgeBg = SuccessGreen
            )
        }

        // CARD DETEKSI LANGKAH (STEP COUNTER) (NEW FEATURE)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            accentColor = MaterialTheme.colorScheme.primary
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("👣", fontSize = 24.sp)
                        Column {
                            Text(text = "Penghitung Langkah", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Target Harian: $todayStepGoal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                            Text(
                                text = "Lacak Otomatis",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                Divider()

                // Circular/Progress indicator row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle Progress Visualizer
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = (todaySteps.toFloat() / todayStepGoal.toFloat()).coerceIn(0f, 1f)

                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            strokeWidth = 8.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$todaySteps",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "langkah",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Stats and Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val calories = (todaySteps * 0.04f).toInt()
                        val distanceKm = String.format(Locale.US, "%.2f", todaySteps * 0.00075f)
                        val pct = (todaySteps.toFloat() / todayStepGoal.toFloat() * 100).toInt()

                        Text("🎯 Pencapaian Hari Ini", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Progress target: $pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("🔥 Kalori: $calories kcal", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        Text("📍 Jarak: $distanceKm km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasStepPermission) {
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Izin")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Izinkan Akses Sensor Gerak (Deteksi Langkah)", fontSize = 12.sp)
                        }
                    } else {
                        // Edit target and quick manually log steps
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var showEditGoalDialog by remember { mutableStateOf(false) }
                            Button(
                                onClick = { showEditGoalDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Edit Target")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Target", fontSize = 12.sp)
                            }

                            if (showEditGoalDialog) {
                                var tempGoalInput by remember { mutableStateOf(todayStepGoal.toString()) }
                                Dialog(onDismissRequest = { showEditGoalDialog = false }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Perbarui Target Langkah Harian", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            OutlinedTextField(
                                                value = tempGoalInput,
                                                onValueChange = { tempGoalInput = it },
                                                label = { Text("Target Langkah") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { showEditGoalDialog = false }) { Text("Batal") }
                                                Button(onClick = {
                                                    val newGoal = tempGoalInput.toIntOrNull() ?: todayStepGoal
                                                    if (newGoal > 0) {
                                                        viewModel.updateStepGoal(newGoal)
                                                        showEditGoalDialog = false
                                                    }
                                                }) { Text("Simpan") }
                                            }
                                        }
                                    }
                                }
                            }

                            // Quick Log Manual Button
                            var showManualLogDialog by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showManualLogDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Manual")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Manual", fontSize = 12.sp)
                            }

                            if (showManualLogDialog) {
                                var manualStepsInput by remember { mutableStateOf("") }
                                Dialog(onDismissRequest = { showManualLogDialog = false }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Tambah Langkah Secara Manual", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            OutlinedTextField(
                                                value = manualStepsInput,
                                                onValueChange = { manualStepsInput = it },
                                                label = { Text("Jumlah Langkah (cth: 5000)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(onClick = { showManualLogDialog = false }) { Text("Batal") }
                                                Button(onClick = {
                                                    val steps = manualStepsInput.toIntOrNull() ?: 0
                                                    if (steps > 0) {
                                                        viewModel.addStepLog(steps, todayStepGoal)
                                                        showManualLogDialog = false
                                                    }
                                                }) { Text("Tambah") }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Simulation options (especially for Emulator/Browser environments)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Simulasi Langkah Nyata (Untuk Emulator & Cloud):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.incrementActiveSteps(100) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+100", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.incrementActiveSteps(500) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+500", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.incrementActiveSteps(1000) },
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+1.000", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // History of steps
                if (stepLogs.isNotEmpty()) {
                    Divider()
                    Text("Riwayat Langkah Terakhir:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        stepLogs.take(3).forEach { log ->
                            val logDateStr = remember(log.date) {
                                val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))
                                sdf.format(Date(log.date))
                            }
                            val isGoalMet = log.steps >= log.target
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "👣 ${log.steps} / ${log.target} langkah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = logDateStr, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isGoalMet) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Goal Met", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteStepLog(log) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // LOG OLAHRAGA CARD
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Log Aktivitas Olahraga", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(onClick = { showAddWorkoutDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Log Baru")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Baru")
                    }
                }

                Divider()

                if (workoutLogs.isEmpty()) {
                    Text(
                        text = "Belum ada riwayat olahraga",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        workoutLogs.forEach { log ->
                            val dateStr = remember(log.date) {
                                val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))
                                sdf.format(Date(log.date))
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = log.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Tipe: ${log.sportType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        if (log.exerciseName.isNotBlank()) {
                                            Text(text = "🏋️ ${log.exerciseName} - ${log.sets} Set x ${log.reps} Reps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(text = "⏱️ ${log.durationMinutes} mnt", fontSize = 11.sp)
                                            Text(text = "🔥 ${log.caloriesBurned} kal", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                        }
                                        Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    IconButton(onClick = { viewModel.deleteWorkoutLog(log) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Log", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // HISTOGRAM VISUALIZATION (CUSTOM CANVAS RENDERING)
        Text(text = "Statistik 7 Hari Terakhir", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Get list of last 7 calendar dates
                val last7DaysData = remember(workoutLogs) {
                    val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val list = mutableListOf<Pair<String, Int>>()
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -6)
                    for (i in 0..6) {
                        val dayStr = sdf.format(calendar.time)
                        val startOfDay = calendar.clone() as Calendar
                        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                        startOfDay.set(Calendar.MINUTE, 0)
                        startOfDay.set(Calendar.SECOND, 0)
                        val endOfDay = calendar.clone() as Calendar
                        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
                        endOfDay.set(Calendar.MINUTE, 59)
                        endOfDay.set(Calendar.SECOND, 59)

                        val dayCal = workoutLogs.filter {
                            it.date in startOfDay.timeInMillis..endOfDay.timeInMillis
                        }.sumOf { it.caloriesBurned }

                        list.add(Pair(dayStr, dayCal))
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    list
                }

                // Draw gorgeous custom bar graph with rounded corners and primary gradient color
                val barColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.outlineVariant
                val textColor = MaterialTheme.colorScheme.onSurface

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val maxVal = Math.max(1, last7DaysData.maxOf { it.second })
                    val spacing = size.width / 7.5f
                    val barWidth = spacing * 0.6f

                    last7DaysData.forEachIndexed { idx, pair ->
                        val x = (idx + 0.5f) * spacing
                        val barHeight = (pair.second.toFloat() / maxVal.toFloat()) * (size.height - 40.dp.toPx())
                        val y = size.height - 25.dp.toPx()

                        // Draw background track line
                        drawLine(
                            color = trackColor.copy(alpha = 0.3f),
                            start = Offset(x, y),
                            end = Offset(x, 10.dp.toPx()),
                            strokeWidth = barWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        // Draw active column
                        if (barHeight > 0) {
                            drawLine(
                                color = barColor,
                                start = Offset(x, y),
                                end = Offset(x, y - barHeight),
                                strokeWidth = barWidth,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }

                // Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    last7DaysData.forEach { pair ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = pair.first, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${pair.second}", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD WORKOUT ENTRY
    if (showAddWorkoutDialog) {
        Dialog(onDismissRequest = { showAddWorkoutDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Log Olahraga", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = logName,
                        onValueChange = { logName = it },
                        label = { Text("Nama Olahraga (cth: Lari Sore)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pilih Jenis Olahraga", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SPORT_MET.keys.forEach { sport ->
                            FilterChip(
                                selected = logSportType == sport,
                                onClick = { logSportType = sport },
                                label = { Text(sport, fontSize = 10.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = logDuration,
                            onValueChange = { logDuration = it },
                            label = { Text("Durasi (menit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = logWeight,
                            onValueChange = { logWeight = it },
                            label = { Text("Beban (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Gym Details if weights sports are selected
                    if (WEIGHT_SPORTS.contains(logSportType)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Detail Latihan Gym", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                OutlinedTextField(
                                    value = logExerciseName,
                                    onValueChange = { logExerciseName = it },
                                    label = { Text("Gerakan (cth: Bench Press)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = logSets,
                                        onValueChange = { logSets = it },
                                        label = { Text("Set") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = logReps,
                                        onValueChange = { logReps = it },
                                        label = { Text("Repetisi") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddWorkoutDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            val dur = logDuration.toIntOrNull() ?: 0
                            val wgh = logWeight.toFloatOrNull() ?: 60f
                            val met = SPORT_MET[logSportType] ?: 5.0
                            val calculatedCalories = Math.round(met * wgh * (dur / 60.0)).toInt()

                            viewModel.addWorkoutLog(
                                name = logName,
                                sportType = logSportType,
                                duration = dur,
                                weight = wgh,
                                calories = calculatedCalories,
                                exercise = logExerciseName,
                                sets = logSets.toIntOrNull() ?: 0,
                                reps = logReps.toIntOrNull() ?: 0
                            )

                            // Clear dialogue form
                            logName = ""
                            logDuration = ""
                            logExerciseName = ""
                            logSets = ""
                            logReps = ""
                            showAddWorkoutDialog = false
                        }) { Text("Log Sekarang") }
                    }
                }
            }
        }
    }
}

// ============================================================
// SAVINGS SCREEN
// ============================================================
@Composable
fun SavingScreen(viewModel: PlannerViewModel) {
    val savingGoals by viewModel.savingGoals.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var editTargetGoal by remember { mutableStateOf<SavingGoal?>(null) }

    var goalName by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalCurrent by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Target Tabungan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(onClick = { showAddGoalDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                }

                Divider()

                if (savingGoals.isEmpty()) {
                    Text(
                        text = "Belum ada target tabungan",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(
                        modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        savingGoals.forEach { goal ->
                            val pct = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount.toFloat()) else 0f
                            val pctText = Math.round(pct * 100)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = goal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = {
                                                editTargetGoal = goal
                                                goalName = goal.name
                                                goalTarget = goal.targetAmount.toString()
                                                goalCurrent = goal.currentAmount.toString()
                                            }, modifier = Modifier.size(32.dp)) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { viewModel.deleteSavingGoal(goal) }, modifier = Modifier.size(32.dp)) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    LinearProgressIndicator(
                                        progress = pct.coerceIn(0f, 1f),
                                        color = if (pct >= 1f) SuccessGreen else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(CircleShape)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${formatRupiah(goal.currentAmount)} / ${formatRupiah(goal.targetAmount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$pctText%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (pct >= 1f) SuccessGreen else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD SAVING GOAL
    if (showAddGoalDialog) {
        Dialog(onDismissRequest = { showAddGoalDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tambah Target Tabungan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = goalName, onValueChange = { goalName = it }, label = { Text("Nama Target (cth: Beli Laptop)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = goalTarget, onValueChange = { goalTarget = it }, label = { Text("Nominal Target (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = goalCurrent, onValueChange = { goalCurrent = it }, label = { Text("Terkumpul (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddGoalDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            val trg = goalTarget.toLongOrNull() ?: 0L
                            val cur = goalCurrent.toLongOrNull() ?: 0L
                            viewModel.addSavingGoal(goalName, trg, cur)
                            goalName = ""
                            goalTarget = ""
                            goalCurrent = ""
                            showAddGoalDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }

    // DIALOG: EDIT SAVING GOAL
    if (editTargetGoal != null) {
        Dialog(onDismissRequest = { editTargetGoal = null }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Edit Target Tabungan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = goalName, onValueChange = { goalName = it }, label = { Text("Nama Target") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = goalTarget, onValueChange = { goalTarget = it }, label = { Text("Nominal Target (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = goalCurrent, onValueChange = { goalCurrent = it }, label = { Text("Terkumpul (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editTargetGoal = null }) { Text("Batal") }
                        Button(onClick = {
                            val trg = goalTarget.toLongOrNull() ?: 0L
                            val cur = goalCurrent.toLongOrNull() ?: 0L
                            editTargetGoal?.let {
                                viewModel.editSavingGoal(it, goalName, trg, cur)
                            }
                            goalName = ""
                            goalTarget = ""
                            goalCurrent = ""
                            editTargetGoal = null
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// ============================================================
// DONGHUA SCREEN
// ============================================================
@Composable
fun DonghuaScreen(viewModel: PlannerViewModel) {
    val donghuaItems by viewModel.donghuaItems.collectAsStateWithLifecycle()

    var showAddDonghuaDialog by remember { mutableStateOf(false) }
    var editDonghuaItem by remember { mutableStateOf<DonghuaItem?>(null) }

    var donghuaTitle by remember { mutableStateOf("") }
    var donghuaTotal by remember { mutableStateOf("12") }
    var donghuaCurrent by remember { mutableStateOf("0") }
    var donghuaStatus by remember { mutableStateOf("watching") }
    var donghuaRating by remember { mutableStateOf(5) }
    var donghuaFav by remember { mutableStateOf(false) }

    var filterValue by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("title") }

    val scrollState = rememberScrollState()

    // Calculated points
    val totalPoints = remember(donghuaItems) {
        val count = donghuaItems.size
        val finished = donghuaItems.count { it.status == "finished" }
        count * 2 + finished * 5
    }

    val statsTotal = remember(donghuaItems) { donghuaItems.size }
    val statsWatching = remember(donghuaItems) { donghuaItems.count { it.status == "watching" } }
    val statsFinished = remember(donghuaItems) { donghuaItems.count { it.status == "finished" } }
    val statsWaiting = remember(donghuaItems) { donghuaItems.count { it.status == "waiting" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // POINTS BAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total Poin Otaku", fontWeight = FontWeight.Bold, color = Color.White)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "⭐ $totalPoints Poin", color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // STATS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$statsTotal", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$statsWatching", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "Tonton", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$statsFinished", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = SuccessGreen)
                    Text(text = "Selesai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$statsWaiting", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = WarningAmber)
                    Text(text = "Tunggu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        // TOOLBAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari donghua...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") }
            )
            Button(
                onClick = { showAddDonghuaDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah")
            }
        }

        // FILTER CHIPS & SORT
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("all" to "Semua", "watching" to "Tonton", "finished" to "Selesai", "waiting" to "Tunggu", "favorites" to "Favorit").forEach { (filterVal, label) ->
                FilterChip(
                    selected = filterValue == filterVal,
                    onClick = { filterValue = filterVal },
                    label = { Text(label) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Urutkan Berdasarkan:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("title" to "Judul", "latest" to "Terbaru", "rating" to "Rating").forEach { (sortVal, label) ->
                    FilterChip(
                        selected = sortBy == sortVal,
                        onClick = { sortBy = sortVal },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }
        }

        // ITEMS GRID / COLUMN
        val processedItems = remember(donghuaItems, filterValue, searchQuery, sortBy) {
            var items = donghuaItems
            // Filter
            if (filterValue == "watching") items = items.filter { it.status == "watching" }
            if (filterValue == "finished") items = items.filter { it.status == "finished" }
            if (filterValue == "waiting") items = items.filter { it.status == "waiting" }
            if (filterValue == "favorites") items = items.filter { it.isFavorite }

            // Search
            if (searchQuery.isNotBlank()) {
                items = items.filter { it.title.lowercase(Locale.getDefault()).contains(searchQuery.lowercase(Locale.getDefault())) }
            }

            // Sort
            when (sortBy) {
                "latest" -> items.sortedByDescending { it.updatedAt }
                "rating" -> items.sortedByDescending { it.rating }
                else -> items.sortedBy { it.title.lowercase(Locale.getDefault()) }
            }
        }

        if (processedItems.isEmpty()) {
            Text(
                text = "Belum ada donghua yang sesuai.",
                modifier = Modifier.fillMaxWidth().padding(48.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                processedItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (item.status) {
                                                    "finished" -> SuccessGreen.copy(alpha = 0.15f)
                                                    "waiting" -> WarningAmber.copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = when (item.status) {
                                                "finished" -> "Selesai"
                                                "waiting" -> "Tunggu"
                                                else -> "Tonton"
                                            },
                                            color = when (item.status) {
                                                "finished" -> SuccessGreen
                                                "waiting" -> WarningAmber
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.toggleDonghuaFavorite(item) }) {
                                        Icon(
                                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.Star,
                                            contentDescription = "Favorit",
                                            tint = if (item.isFavorite) WarningAmber else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(onClick = {
                                        editDonghuaItem = item
                                        donghuaTitle = item.title
                                        donghuaTotal = item.totalEpisodes.toString()
                                        donghuaCurrent = item.currentEpisode.toString()
                                        donghuaStatus = item.status
                                        donghuaRating = item.rating
                                        donghuaFav = item.isFavorite
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteDonghua(item) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                                    }
                                }
                            }

                            // Progress
                            val pct = if (item.totalEpisodes > 0) (item.currentEpisode.toFloat() / item.totalEpisodes.toFloat()) else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Eps ${item.currentEpisode} / ${item.totalEpisodes}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${Math.round(pct * 100)}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = pct.coerceIn(0f, 1f),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )

                            // Rating stars
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating Star",
                                        tint = if (i <= item.rating) WarningAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Episode increment button
                            Button(
                                onClick = { viewModel.incrementDonghua(item) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), contentColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Episode")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nonton 1 Episode Lagi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD DONGHUA
    if (showAddDonghuaDialog) {
        Dialog(onDismissRequest = { showAddDonghuaDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Donghua Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = donghuaTitle, onValueChange = { donghuaTitle = it }, label = { Text("Judul Donghua") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = donghuaTotal, onValueChange = { donghuaTotal = it }, label = { Text("Total Eps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = donghuaCurrent, onValueChange = { donghuaCurrent = it }, label = { Text("Eps Sekarang") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }

                    Text("Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("watching" to "Tonton", "finished" to "Selesai", "waiting" to "Tunggu").forEach { (statVal, label) ->
                            FilterChip(
                                selected = donghuaStatus == statVal,
                                onClick = { donghuaStatus = statVal },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Rating (1-5)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { donghuaRating = i }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating $i",
                                    tint = if (i <= donghuaRating) WarningAmber else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = donghuaFav, onCheckedChange = { donghuaFav = it })
                        Text("Simpan Sebagai Favorit", fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDonghuaDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            val tot = donghuaTotal.toIntOrNull() ?: 12
                            val cur = donghuaCurrent.toIntOrNull() ?: 0
                            viewModel.addDonghua(donghuaTitle, tot, cur, donghuaStatus, donghuaRating, donghuaFav)
                            donghuaTitle = ""
                            donghuaTotal = "12"
                            donghuaCurrent = "0"
                            donghuaStatus = "watching"
                            donghuaRating = 5
                            donghuaFav = false
                            showAddDonghuaDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }

    // DIALOG: EDIT DONGHUA
    if (editDonghuaItem != null) {
        Dialog(onDismissRequest = { editDonghuaItem = null }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Edit Donghua", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = donghuaTitle, onValueChange = { donghuaTitle = it }, label = { Text("Judul Donghua") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = donghuaTotal, onValueChange = { donghuaTotal = it }, label = { Text("Total Eps") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = donghuaCurrent, onValueChange = { donghuaCurrent = it }, label = { Text("Eps Sekarang") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }

                    Text("Status", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("watching" to "Tonton", "finished" to "Selesai", "waiting" to "Tunggu").forEach { (statVal, label) ->
                            FilterChip(
                                selected = donghuaStatus == statVal,
                                onClick = { donghuaStatus = statVal },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Rating (1-5)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { donghuaRating = i }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating $i",
                                    tint = if (i <= donghuaRating) WarningAmber else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = donghuaFav, onCheckedChange = { donghuaFav = it })
                        Text("Simpan Sebagai Favorit", fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editDonghuaItem = null }) { Text("Batal") }
                        Button(onClick = {
                            val tot = donghuaTotal.toIntOrNull() ?: 12
                            val cur = donghuaCurrent.toIntOrNull() ?: 0
                            editDonghuaItem?.let {
                                viewModel.editDonghua(it, donghuaTitle, tot, cur, donghuaStatus, donghuaRating, donghuaFav)
                            }
                            donghuaTitle = ""
                            donghuaTotal = "12"
                            donghuaCurrent = "0"
                            donghuaStatus = "watching"
                            donghuaRating = 5
                            donghuaFav = false
                            editDonghuaItem = null
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// ============================================================
// BUDGET SCREEN WITH DANA SYNC INTEGRATION
// ============================================================
fun sendTestDanaNotification(context: Context, text: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "dana_sync_test"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "DANA Sync Test", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel untuk menguji integrasi otomatis DANA"
        }
        notificationManager.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("DANA")
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
    
    notificationManager.notify(99, builder.build())
}

@Composable
fun BudgetScreen(viewModel: PlannerViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgetLimit by viewModel.budgetLimit.collectAsStateWithLifecycle()

    var showAddTxDialog by remember { mutableStateOf(false) }
    var showEditLimitDialog by remember { mutableStateOf(false) }

    var txDesc by remember { mutableStateOf("") }
    var txAmount by remember { mutableStateOf("") }
    var txType by remember { mutableStateOf("income") }

    var txSearchQuery by remember { mutableStateOf("") }
    var txFilterType by remember { mutableStateOf("all") } // all, income, expense

    val scrollState = rememberScrollState()

    // Calculations
    val incomeSum = remember(transactions) { transactions.filter { it.type == "income" }.sumOf { it.amount } }
    val expenseSum = remember(transactions) { transactions.filter { it.type == "expense" }.sumOf { it.amount } }
    val balance = incomeSum - expenseSum

    // Dynamic verification of Notification Listener permission status
    val context = LocalContext.current
    var isDanaIntegrated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            isDanaIntegrated = flat != null && flat.contains(context.packageName)
            kotlinx.coroutines.delay(1500) // check every 1.5 seconds dynamically
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BALANCE CARD
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            accentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = "Total Saldo Anda", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupiah(balance),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Pemasukan", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(text = formatRupiah(incomeSum), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Pengeluaran", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(text = formatRupiah(expenseSum), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // MONTHLY BUDGET LIMIT CARD (FEATURE 4)
        GlassyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎯", fontSize = 20.sp)
                        Column {
                            Text(text = "Limit Anggaran Bulanan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Pengeluaran: ${formatRupiah(expenseSum)} / ${formatRupiah(budgetLimit)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(
                        onClick = { showEditLimitDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Set Limit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Progress Bar for Limit
                val pct = if (budgetLimit > 0) (expenseSum.toFloat() / budgetLimit.toFloat()) else 0f
                val progressColor = when {
                    pct >= 1.0f -> DangerRed
                    pct >= 0.8f -> WarningAmber
                    else -> MaterialTheme.colorScheme.primary
                }

                LinearProgressIndicator(
                    progress = { Math.min(1.0f, pct) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Alerts based on budget threshold
                if (pct >= 1.0f) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚨", fontSize = 16.sp)
                            Text(
                                text = "Peringatan: Pengeluaran bulanan Anda telah melebihi limit anggaran!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                    }
                } else if (pct >= 0.8f) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                            Text(
                                text = "Perhatian: Pengeluaran bulanan Anda telah mencapai 80% dari limit anggaran!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }
                }
            }
        }

        // EDIT BUDGET LIMIT DIALOG
        if (showEditLimitDialog) {
            var tempLimitText by remember { mutableStateOf(budgetLimit.toString()) }

            Dialog(onDismissRequest = { showEditLimitDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Ubah Limit Pengeluaran", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(text = "Masukkan batas maksimal pengeluaran bulanan untuk memonitor keuangan Anda dengan waspada.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = tempLimitText,
                            onValueChange = { tempLimitText = it },
                            label = { Text("Batas Limit (IDR)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditLimitDialog = false }) {
                                Text("Batal")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val amount = tempLimitText.toLongOrNull() ?: 0L
                                    viewModel.updateBudgetLimit(amount)
                                    showEditLimitDialog = false
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Simpan")
                            }
                        }
                    }
                }
            }
        }

        // DANA SYNC INTEGRATION CARD
        val syncContainerColor = if (isDanaIntegrated) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        }
        val syncBorderColor = if (isDanaIntegrated) SuccessGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)),
            shape = RoundedCornerShape(20.dp),
            containerColor = syncContainerColor,
            borderStroke = BorderStroke(1.dp, syncBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDanaIntegrated) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Wallet",
                                    tint = if (isDanaIntegrated) SuccessGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(text = "Otomatisasi Dana (DANA)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(
                                text = if (isDanaIntegrated) "Status: Terhubung & Aktif" else "Status: Belum Terhubung",
                                fontSize = 11.sp,
                                color = if (isDanaIntegrated) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isDanaIntegrated) SuccessGreen else WarningAmber,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (isDanaIntegrated) "REALTIME" else "MANUAL",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = if (isDanaIntegrated) {
                        "Setiap ada notifikasi transaksi masuk/keluar dari DANA di HP Anda, sistem akan langsung mendeteksi, mengekstrak nominal, dan mencatat transaksi secara otomatis di bawah secara real-time!"
                    } else {
                        "Integrasikan dengan sistem notifikasi Android untuk melacak pengeluaran dan pemasukan otomatis setiap kali Anda bertransaksi menggunakan DANA."
                    },
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isDanaIntegrated) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Aktifkan")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aktifkan Sinkronisasi DANA")
                    }
                } else {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Sandbox Pengujian (Kirim Notifikasi Demo):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    sendTestDanaNotification(
                                        context = context,
                                        text = "Pembayaran berhasil sebesar Rp 50.000 ke Tokopedia menggunakan saldo DANA."
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test Keluar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Rp50K Keluar", fontSize = 11.sp, color = DangerRed)
                            }

                            Button(
                                onClick = {
                                    sendTestDanaNotification(
                                        context = context,
                                        text = "Kamu menerima uang sebesar Rp 150.000 dari BUDI UTOMO melalui transfer DANA."
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Test Masuk", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Rp150K Masuk", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // TRANSACTIONS LIST CARD WITH SMOOTH SIZE TRANSITIONS
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Riwayat Transaksi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(onClick = { showAddTxDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Transaksi")
                    }
                }

                Divider()

                // SEARCH & FILTER CHIPS (FEATURES 5 & 6)
                OutlinedTextField(
                    value = txSearchQuery,
                    onValueChange = { txSearchQuery = it },
                    placeholder = { Text("Cari transaksi...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Cari") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("all" to "Semua", "income" to "Pemasukan", "expense" to "Pengeluaran").forEach { (typeVal, label) ->
                        FilterChip(
                            selected = txFilterType == typeVal,
                            onClick = { txFilterType = typeVal },
                            label = { Text(label + " (" + (if (typeVal == "income") transactions.count { it.type == "income" } else if (typeVal == "expense") transactions.count { it.type == "expense" } else transactions.size) + ")", fontSize = 11.sp) }
                        )
                    }
                }

                val filteredTx = remember(transactions, txSearchQuery, txFilterType) {
                    transactions.filter { tx ->
                        val matchesSearch = tx.description.contains(txSearchQuery, ignoreCase = true)
                        val matchesFilter = when (txFilterType) {
                            "income" -> tx.type == "income"
                            "expense" -> tx.type == "expense"
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }
                }

                if (filteredTx.isEmpty()) {
                    Text(
                        text = if (transactions.isEmpty()) "Belum ada transaksi" else "Transaksi tidak ditemukan",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                } else {
                    Column(
                        modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow)),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredTx.forEach { tx ->
                            val dateStr = remember(tx.date) {
                                val sdf = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))
                                sdf.format(Date(tx.date))
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = tx.description, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "${if (tx.type == "income") "+" else "-"} ${formatRupiah(tx.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (tx.type == "income") SuccessGreen else DangerRed
                                        )
                                        IconButton(onClick = { viewModel.deleteTransaction(tx) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG: ADD TRANSACTION
    if (showAddTxDialog) {
        Dialog(onDismissRequest = { showAddTxDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tambah Transaksi Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutlinedTextField(value = txDesc, onValueChange = { txDesc = it }, label = { Text("Keterangan (cth: Gaji Bulanan)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = txAmount, onValueChange = { txAmount = it }, label = { Text("Jumlah Nominal (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                    Text("Pilih Tipe Transaksi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = txType == "income",
                            onClick = { txType = "income" },
                            label = { Text("Pemasukan (Masuk)", color = if (txType == "income") SuccessGreen else MaterialTheme.colorScheme.onSurface) }
                        )
                        FilterChip(
                            selected = txType == "expense",
                            onClick = { txType = "expense" },
                            label = { Text("Pengeluaran (Keluar)", color = if (txType == "expense") DangerRed else MaterialTheme.colorScheme.onSurface) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddTxDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            val amt = txAmount.toLongOrNull() ?: 0L
                            viewModel.addTransaction(txDesc, amt, txType)
                            txDesc = ""
                            txAmount = ""
                            txType = "income"
                            showAddTxDialog = false
                        }) { Text("Simpan") }
                    }
                }
            }
        }
    }
}

// ============================================================
// CHART SCREEN
// ============================================================
@Composable
fun ChartScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()

    var activeChartTab by remember { mutableStateOf("tasks") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Headers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("tasks" to "Tugas PR", "subjects" to "Mata Pelajaran", "workouts" to "Olahraga").forEach { (tabVal, label) ->
                    FilterChip(
                        selected = activeChartTab == tabVal,
                        onClick = { activeChartTab = tabVal },
                        label = { Text(label, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // CHART DRAWING WINDOW
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = when (activeChartTab) {
                        "tasks" -> "Penyelesaian Tugas / PR"
                        "subjects" -> "Distribusi Mata Pelajaran"
                        else -> "Volume Olahraga & Kalori"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Divider()

                when (activeChartTab) {
                    "tasks" -> {
                        val total = tasks.size
                        val done = tasks.count { it.isDone }
                        val pending = total - done

                        if (total == 0) {
                            Text(
                                text = "Belum ada tugas untuk diredistribusikan.",
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            // Doughnut Chart using custom canvas
                            val doneColor = SuccessGreen
                            val pendingColor = DangerRed
                            val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Canvas(modifier = Modifier.size(200.dp)) {
                                    val strokeWidth = 30.dp.toPx()
                                    val doneAngle = (done.toFloat() / total.toFloat()) * 360f
                                    val pendingAngle = 360f - doneAngle

                                    drawArc(
                                        color = doneColor,
                                        startAngle = -90f,
                                        sweepAngle = doneAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                    drawArc(
                                        color = pendingColor,
                                        startAngle = -90f + doneAngle,
                                        sweepAngle = pendingAngle,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(12.dp).background(doneColor, CircleShape))
                                        Text("Selesai: $done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(12.dp).background(pendingColor, CircleShape))
                                        Text("Belum: $pending", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "subjects" -> {
                        val subjectsCount = remember(schedules) {
                            val map = mutableMapOf<String, Int>()
                            schedules.forEach {
                                map[it.subjectName] = (map[it.subjectName] ?: 0) + 1
                            }
                            map
                        }

                        if (subjectsCount.isEmpty()) {
                            Text(
                                text = "Belum ada mata pelajaran di jadwal.",
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            // Display Bar charts of Subjects count
                            val totalLessons = subjectsCount.values.sum()
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                subjectsCount.forEach { (sub, count) ->
                                    val pct = count.toFloat() / totalLessons.toFloat()
                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = sub, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "$count sesi (${Math.round(pct * 100)}%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = pct,
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Workouts Line/Bar Graph
                        val calPerDay = remember(workoutLogs) {
                            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                            val list = mutableListOf<Pair<String, Int>>()
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, -6)
                            for (i in 0..6) {
                                val dayStr = sdf.format(calendar.time)
                                val startOfDay = calendar.clone() as Calendar
                                startOfDay.set(Calendar.HOUR_OF_DAY, 0)
                                startOfDay.set(Calendar.MINUTE, 0)
                                startOfDay.set(Calendar.SECOND, 0)
                                val endOfDay = calendar.clone() as Calendar
                                endOfDay.set(Calendar.HOUR_OF_DAY, 23)
                                endOfDay.set(Calendar.MINUTE, 59)
                                endOfDay.set(Calendar.SECOND, 59)

                                val dayCal = workoutLogs.filter {
                                    it.date in startOfDay.timeInMillis..endOfDay.timeInMillis
                                }.sumOf { it.caloriesBurned }

                                list.add(Pair(dayStr, dayCal))
                                calendar.add(Calendar.DAY_OF_YEAR, 1)
                            }
                            list
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Total Kalori Dibakar Per Hari", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

                            // Custom Bar Column Draw
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val maxVal = Math.max(1, calPerDay.maxOf { it.second })
                                val spacing = size.width / 7.5f
                                val barWidth = spacing * 0.5f

                                calPerDay.forEachIndexed { idx, pair ->
                                    val x = (idx + 0.5f) * spacing
                                    val barHeight = (pair.second.toFloat() / maxVal.toFloat()) * (size.height - 20.dp.toPx())
                                    val y = size.height

                                    // Draw bar rounded line
                                    drawLine(
                                        color = SuccessGreen,
                                        start = Offset(x, y),
                                        end = Offset(x, y - barHeight),
                                        strokeWidth = barWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                }
                            }

                            // Day markers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                calPerDay.forEach { pair ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text(text = pair.first, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "${pair.second} kal", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
