package com.example.cst438project1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.ui.theme.CST438Project1Theme
import java.util.Locale

enum class Tab(val label: String) {
    TODAY("Today"), FAVORITES("Favorites"), PROFILE("Profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CST438Project1Theme {
                // Three tabs and one detail screen, so state values beat pulling
                // in Navigation Compose.
                var tab by remember { mutableStateOf(Tab.TODAY) }
                var openMeal by remember { mutableStateOf<Meal?>(null) }
                val log = rememberMealLog()
                val favorites = rememberFavorites()
                val profile = rememberProfile()

                BackHandler(enabled = openMeal != null) { openMeal = null }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (openMeal == null) TabBar(tab) { tab = it }
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        when {
                            openMeal == Meal.BREAKFAST ->
                                BreakfastScreen(onBack = { openMeal = null })

                            tab == Tab.FAVORITES -> FavoritesScreen(
                                favorites = favorites,
                                onAddTo = { meal, entry ->
                                    log.getValue(meal).add(entry)
                                    tab = Tab.TODAY
                                }
                            )

                            tab == Tab.PROFILE -> ProfileScreen(
                                profile = profile,
                                today = log.values.flatten().macros()
                            )

                            else -> LandingScreen(
                                log = log,
                                goal = profile.value.calorieGoal,
                                // Only breakfast has a detail screen so far.
                                onOpenMeal = { if (it == Meal.BREAKFAST) openMeal = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabBar(current: Tab, onSelect: (Tab) -> Unit) {
    val line = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                    .clickable { onSelect(entry) }
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
