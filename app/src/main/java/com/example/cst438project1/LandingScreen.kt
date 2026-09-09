package com.example.cst438project1

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cst438project1.ui.theme.AnytimeClay
import com.example.cst438project1.ui.theme.CST438Project1Theme
import com.example.cst438project1.ui.theme.EveningIndigo
import com.example.cst438project1.ui.theme.MiddaySage
import com.example.cst438project1.ui.theme.MorningAmber
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

// One logged food. Macros in grams.
data class FoodEntry(
    val name: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fat: Int
)

data class Macros(val calories: Int, val carbs: Int, val protein: Int, val fat: Int)

fun List<FoodEntry>.macros() = Macros(
    calories = sumOf { it.calories },
    carbs = sumOf { it.carbs },
    protein = sumOf { it.protein },
    fat = sumOf { it.fat }
)

// The four sections. `window` is the time of day each one belongs to - the day
// runs top to bottom, which is what the line down the left edge draws.
// `hasScreen` says whether this meal has its own add screen yet. The ones that
// do send "+ Add food" there; the rest fall back to the inline form until their
// screens land, so every meal stays loggable.
enum class Meal(
    val label: String,
    val window: String,
    val accent: Color,
    val hasScreen: Boolean
) {
    BREAKFAST("Breakfast", "Morning", MorningAmber, hasScreen = true),
    LUNCH("Lunch", "Midday", MiddaySage, hasScreen = false),
    DINNER("Dinner", "Evening", EveningIndigo, hasScreen = false),
    SNACKS("Snacks", "Anytime", AnytimeClay, hasScreen = false)
}

const val CALORIE_GOAL = 2000

internal val Mono = FontFamily.Monospace
internal val Display = FontFamily.Serif

internal fun Int.grouped() = "%,d".format(this)

// Held above the screen so it survives navigating into a meal and back.
// Swap for a repository once the database lands.
@Composable
fun rememberMealLog(): Map<Meal, SnapshotStateList<FoodEntry>> = remember {
    Meal.entries.associateWith { meal ->
        mutableStateListOf<FoodEntry>().apply {
            if (meal == Meal.BREAKFAST) {
                add(FoodEntry("Banana", 105, 27, 1, 0))
                add(FoodEntry("Oatmeal", 150, 27, 5, 3))
            }
        }
    }
}

@Composable
fun LandingScreen(
    log: Map<Meal, SnapshotStateList<FoodEntry>> = rememberMealLog(),
    goal: Int = CALORIE_GOAL,
    onOpenMeal: (Meal) -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    val day = log.values.flatten().macros()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 40.dp)
    ) {
        DayHeader(today, day, goal)
        if (day.calories > 0) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Long-press a food to remove it.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
        } else {
            Spacer(Modifier.height(36.dp))
        }
        Meal.entries.forEach { meal ->
            MealSection(
                meal = meal,
                entries = log.getValue(meal),
                isLast = meal == Meal.entries.last(),
                onOpen = { onOpenMeal(meal) }
            )
        }
    }
}

@Composable
private fun DayHeader(today: LocalDate, day: Macros, goal: Int) {
    val weekday = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val month = today.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Text(
        text = "$weekday / $month ${today.dayOfMonth}".uppercase(Locale.getDefault()),
        fontFamily = Mono,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = day.calories.grouped(),
            fontFamily = Mono,
            fontSize = 58.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-2).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "of ${goal.grouped()} kcal",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
    Spacer(Modifier.height(14.dp))
    ProgressRule(day.calories.toFloat() / goal)
    Spacer(Modifier.height(20.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        MacroStat("Carbs", day.carbs, Modifier.weight(1f))
        VerticalHairline()
        MacroStat("Protein", day.protein, Modifier.weight(1f))
        VerticalHairline()
        MacroStat("Fat", day.fat, Modifier.weight(1f))
    }
}

@Composable
internal fun ProgressRule(fraction: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(MaterialTheme.colorScheme.outline)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onBackground)
        )
    }
}

@Composable
internal fun MacroStat(label: String, grams: Int, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${grams}g",
            fontFamily = Mono,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label.uppercase(Locale.getDefault()),
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun VerticalHairline() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun MealSection(
    meal: Meal,
    entries: SnapshotStateList<FoodEntry>,
    isLast: Boolean,
    onOpen: () -> Unit
) {
    var pendingRemoval by remember { mutableStateOf<FoodEntry?>(null) }
    val totals = entries.macros()
    val line = MaterialTheme.colorScheme.outline

    pendingRemoval?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text("Remove ${entry.name}?") },
            text = {
                Text(
                    text = "Removes it from ${meal.label.lowercase(Locale.getDefault())} " +
                        "and from today's total.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    entries.remove(entry)
                    pendingRemoval = null
                }) {
                    Text("Remove", color = meal.accent, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) {
                    Text("Keep", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            // Material tints the container with `primary` unless elevation is flat.
            tonalElevation = 0.dp
        )
    }

    Row(Modifier.height(IntrinsicSize.Min)) {
        // The day-line: one station dot per meal, filled once something is logged.
        Box(
            Modifier
                .width(34.dp)
                .fillMaxHeight()
                .drawBehind {
                    val x = 5.dp.toPx()
                    val y = 10.dp.toPx()
                    val r = 5.dp.toPx()
                    if (!isLast) {
                        drawLine(
                            color = line,
                            start = Offset(x, y + r + 4.dp.toPx()),
                            end = Offset(x, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    if (entries.isEmpty()) {
                        drawCircle(meal.accent, r - 1, Offset(x, y), style = Stroke(1.5.dp.toPx()))
                    } else {
                        drawCircle(meal.accent, r, Offset(x, y))
                    }
                }
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = meal.window.uppercase(Locale.getDefault()),
                        fontSize = 10.sp,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = meal.label,
                        fontFamily = Display,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = if (entries.isEmpty()) "--" else "${totals.calories.grouped()} kcal",
                    fontFamily = Mono,
                    fontSize = 13.sp,
                    color = if (entries.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            if (entries.isEmpty()) {
                Text(
                    text = "Nothing logged yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEach { entry ->
                    EntryRow(entry, onRemove = { pendingRemoval = entry })
                }
            }

            Text(
                text = "+ Add food",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = meal.accent,
                // Meals without a screen yet have nothing to open, so this does
                // nothing until theirs lands.
                modifier = Modifier
                    .clickable(enabled = meal.hasScreen, onClick = onOpen)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryRow(entry: FoodEntry, onRemove: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRemove()
                },
                onLongClickLabel = "Remove ${entry.name}"
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.name,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "C ${entry.carbs}  P ${entry.protein}  F ${entry.fat}",
                fontFamily = Mono,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = entry.calories.grouped(),
            fontFamily = Mono,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Keeps the numeric fields numeric, so there is no error state to explain.
internal fun String.digits() = filter { it.isDigit() }.take(5)

@Composable
internal fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    numeric: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = if (numeric) Mono else FontFamily.Default,
            fontSize = 15.sp,
            textAlign = if (numeric) TextAlign.End else TextAlign.Start
        ),
        keyboardOptions = if (numeric) {
            KeyboardOptions(keyboardType = KeyboardType.Number)
        } else {
            KeyboardOptions.Default
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            focusedLabelColor = accent,
            cursorColor = accent,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
fun LandingScreenPreview() {
    CST438Project1Theme {
        LandingScreen()
    }
}
