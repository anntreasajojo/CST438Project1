package com.example.cst438project1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.database.AppDatabase
import com.example.cst438project1.database.MealLogDao
import com.example.cst438project1.database.MealLogEntry
import com.example.cst438project1.database.User
import com.example.cst438project1.ui.theme.CST438Project1Theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.util.Locale

enum class Tab(val label: String) {
    TODAY("Today"), FAVORITES("Favorites"), CHARTS("Charts"), PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CST438Project1Theme {
                val database = remember { AppDatabase.getDatabase(applicationContext) }
                val dao = database.userDao()
                var user by remember { mutableStateOf<User?>(null) }
                var registering by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                // Nothing is remembered between launches, so every start asks
                // for a login. A new account goes straight in.
                if (user == null) {
                    if (registering) {
                        // The register screen's own back handler, for its second
                        // step, is composed later and so takes priority.
                        BackHandler { registering = false }
                        RegisterScreen(
                            dao = dao,
                            onRegistered = { id -> scope.launch { user = dao.getUserById(id) } },
                            onLogIn = { registering = false }
                        )
                    } else {
                        LoginScreen(
                            dao = dao,
                            onLoggedIn = { user = it },
                            onRegister = { registering = true }
                        )
                    }
                    return@CST438Project1Theme
                }

                user?.let { signedIn ->
                    key(signedIn.id) { SignedInApp(signedIn, database, database.mealLogDao()) }
                }
            }
        }
    }
}

// This is the existing state-based navigation, with one shared DB write path.
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
internal fun SignedInApp(user: User, database: AppDatabase, dao: MealLogDao) {
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    var openMeal by remember { mutableStateOf<Meal?>(null) }
    var showCategories by remember { mutableStateOf(false) }
    var favoritesRefreshTrigger by remember { mutableStateOf(0) }
    val favorites = rememberFavorites(user.id, database.favoriteDao(), database.foodDao(), favoritesRefreshTrigger)
    val profile = remember {
        mutableStateOf(Profile(user.calorieGoal, user.carbGoal, user.proteinGoal, user.fatGoal))
    }
    val today = rememberToday()
    var period by rememberSaveable { mutableStateOf(ChartPeriod.DAILY) }
    var chartDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var lastToday by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    LaunchedEffect(today) {
        if (chartDay == lastToday) chartDay = today.toEpochDay()
        chartDay = chartDay.coerceAtMost(today.toEpochDay())
        lastToday = today.toEpochDay()
    }
    var saving by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun changeLog(action: suspend () -> Unit) {
        if (saving) return
        saving = true
        scope.launch {
            val failure = saveLog(action)
            saving = false
            if (failure) snackbar.showSnackbar("Could not save your changes. Please try again.")
        }
    }
    BackHandler(enabled = openMeal != null || showCategories) {
        openMeal = null
        showCategories = false
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { if (openMeal == null && !showCategories) TabBar(tab) { tab = it } }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                showCategories -> CategoriesScreen(
                    onBack = { showCategories = false }, favorites = favorites
                )
                openMeal == Meal.BREAKFAST -> BreakfastScreen(
                    onBack = { openMeal = null },
                    userId = user.id,
                    favoriteDao = database.favoriteDao(),
                    foodDao = database.foodDao(),
                    onFavoriteAdded = { favoritesRefreshTrigger++ },
                    onAddFood = { food ->
                        changeLog {
                            dao.insert(
                                MealLogEntry(
                                    userId = user.id,
                                    dateEpochDay = LocalDate.now().toEpochDay(),
                                    meal = Meal.BREAKFAST,
                                    food = food
                                )
                            )
                            openMeal = null
                            tab = Tab.TODAY
                        }
                    }
                )
                openMeal == Meal.LUNCH -> LunchScreen(
                    onBack = { openMeal = null },
                    userId = user.id,
                    favoriteDao = database.favoriteDao(),
                    foodDao = database.foodDao(),
                    onFavoriteAdded = { favoritesRefreshTrigger++ },
                    onAddFood = { food ->
                        changeLog {
                            dao.insert(
                                MealLogEntry(
                                    userId = user.id,
                                    dateEpochDay = LocalDate.now().toEpochDay(),
                                    meal = Meal.LUNCH,
                                    food = food
                                )
                            )
                            openMeal = null
                            tab = Tab.TODAY
                        }
                    }
                )
                openMeal == Meal.DINNER -> DinnerScreen(
                    onBack = { openMeal = null },
                    userId = user.id,
                    favoriteDao = database.favoriteDao(),
                    foodDao = database.foodDao(),
                    onFavoriteAdded = { favoritesRefreshTrigger++ },
                    onAddFood = { food ->
                        changeLog {
                            dao.insert(
                                MealLogEntry(
                                    userId = user.id,
                                    dateEpochDay = LocalDate.now().toEpochDay(),
                                    meal = Meal.DINNER,
                                    food = food
                                )
                            )
                            openMeal = null
                            tab = Tab.TODAY
                        }
                    }
                )
                tab == Tab.FAVORITES -> FavoritesScreen(
                    favorites = favorites,
                    saving = saving,
                    userId = user.id,
                    favoriteDao = database.favoriteDao(),
                    onAddTo = { meal, food ->
                        changeLog {
                            dao.insert(MealLogEntry(userId = user.id,
                                dateEpochDay = LocalDate.now().toEpochDay(), meal = meal, food = food))
                            tab = Tab.TODAY
                        }
                    }
                )
                tab == Tab.CHARTS -> ChartScreen(
                    dao, user.id, today, period, LocalDate.ofEpochDay(chartDay),
                    onPeriodChange = { period = it }, onDateChange = { chartDay = it.toEpochDay() }
                )
                else -> MealLogContent(dao, user.id, today, today.plusDays(1)) { log ->
                    if (tab == Tab.PROFILE) {
                        ProfileScreen(profile, today = log.map { it.food }.macros())
                    } else {
                        LandingScreen(
                            log = log, goal = profile.value.calorieGoal, today = today,
                            onOpenMeal = { openMeal = it },
                            onOpenCategories = { showCategories = true }, saving = saving,
                            onRemove = { id -> changeLog { dao.delete(user.id, id) } }
                        )
                    }
                }
            }
        }
    }
}

@Suppress("TooGenericExceptionCaught")
private suspend fun saveLog(action: suspend () -> Unit): Boolean = try {
    action()
    false
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    true
}

@Composable
private fun TabBar(current: Tab, onSelect: (Tab) -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
            }
            .padding(top = 12.dp, bottom = 14.dp)
    ) {
        Tab.entries.forEach { entry ->
            val selected = entry == current
            val ink = MaterialTheme.colorScheme.onBackground
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected, role = Role.Tab, onClick = { onSelect(entry) })
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Same station dot as the day-line: filled means you are here.
                Box(
                    Modifier
                        .size(6.dp)
                        .drawBehind {
                            val c = Offset(size.width / 2, size.height / 2)
                            if (selected) {
                                drawCircle(ink, size.width / 2, c)
                            } else {
                                drawCircle(line, size.width / 2 - 0.5f, c, style = Stroke(1.dp.toPx()))
                            }
                        }
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.label.uppercase(Locale.getDefault()),
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    color = if (selected) ink else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
